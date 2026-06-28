package com.nichita.myvoyage.ui.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nichita.myvoyage.data.model.Category
import com.nichita.myvoyage.data.model.Currency
import com.nichita.myvoyage.data.model.Expense
import com.nichita.myvoyage.data.repository.VoyageRepository
import com.nichita.myvoyage.ui.nav.NavArgs
import com.nichita.myvoyage.ui.voyageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Состояние формы расхода. */
data class ExpenseFormState(
    val amount: String = "",
    val currency: Currency = Currency.DEFAULT,
    val category: Category = Category.OTHER,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val isEditing: Boolean = false
) {
    val isValid: Boolean get() = (amount.toDoubleOrNull() ?: 0.0) > 0.0
}

/**
 * ViewModel добавления/редактирования расхода.
 * Аргументы навигации: tripId (обязателен) и expenseId (0 — новый).
 */
class ExpenseEditViewModel(
    private val repository: VoyageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: Long = savedStateHandle.get<Long>(NavArgs.TRIP_ID) ?: 0L
    private val expenseId: Long = savedStateHandle.get<Long>(NavArgs.ITEM_ID) ?: 0L

    private val _form = MutableStateFlow(ExpenseFormState())
    val form: StateFlow<ExpenseFormState> = _form.asStateFlow()

    init {
        viewModelScope.launch {
            if (expenseId != 0L) {
                repository.getExpense(expenseId)?.let { e ->
                    _form.value = ExpenseFormState(
                        amount = e.amount.toString(),
                        currency = e.currency,
                        category = e.category,
                        date = e.date,
                        note = e.note,
                        isEditing = true
                    )
                }
            } else {
                // Для нового расхода подставляем валюту рейса.
                repository.getTrip(tripId)?.let { t ->
                    _form.update { it.copy(currency = t.currency) }
                }
            }
        }
    }

    fun onAmountChange(v: String) =
        _form.update { it.copy(amount = v.filter { c -> c.isDigit() || c == '.' }) }
    fun onCurrencyChange(v: Currency) = _form.update { it.copy(currency = v) }
    fun onCategoryChange(v: Category) = _form.update { it.copy(category = v) }
    fun onDateChange(v: Long) = _form.update { it.copy(date = v) }
    fun onNoteChange(v: String) = _form.update { it.copy(note = v) }

    fun save(onDone: () -> Unit) {
        val s = _form.value
        if (!s.isValid) return
        viewModelScope.launch {
            repository.upsertExpense(
                Expense(
                    id = expenseId,
                    tripId = tripId,
                    amount = s.amount.toDoubleOrNull() ?: 0.0,
                    currency = s.currency,
                    category = s.category,
                    date = s.date,
                    note = s.note.trim()
                )
            )
            onDone()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ExpenseEditViewModel(voyageRepository(), createSavedStateHandle()) }
        }
    }
}
