package com.nichita.myvoyage.ui.fuel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nichita.myvoyage.data.model.FuelEntry
import com.nichita.myvoyage.data.model.FuelType
import com.nichita.myvoyage.data.repository.VoyageRepository
import com.nichita.myvoyage.ui.nav.NavArgs
import com.nichita.myvoyage.ui.voyageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Состояние формы заправки. */
data class FuelFormState(
    val date: Long = System.currentTimeMillis(),
    val cost: String = "",
    val fuelType: FuelType = FuelType.DEFAULT,
    val isEditing: Boolean = false
) {
    val isValid: Boolean
        get() = (cost.toDoubleOrNull() ?: 0.0) > 0.0
}

/**
 * ViewModel добавления/редактирования заправки.
 * Аргументы: tripId (обязателен) и fuelId (0 — новая).
 */
class FuelEditViewModel(
    private val repository: VoyageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: Long = savedStateHandle.get<Long>(NavArgs.TRIP_ID) ?: 0L
    private val fuelId: Long = savedStateHandle.get<Long>(NavArgs.ITEM_ID) ?: 0L

    private val _form = MutableStateFlow(FuelFormState())
    val form: StateFlow<FuelFormState> = _form.asStateFlow()

    init {
        if (fuelId != 0L) {
            viewModelScope.launch {
                repository.getFuelEntry(fuelId)?.let { e ->
                    _form.value = FuelFormState(
                        date = e.date,
                        cost = e.cost.toString(),
                        fuelType = e.fuelType,
                        isEditing = true
                    )
                }
            }
        }
    }

    fun onDateChange(v: Long) = _form.update { it.copy(date = v) }
    fun onCostChange(v: String) = _form.update { it.copy(cost = numeric(v)) }
    fun onFuelTypeChange(v: FuelType) = _form.update { it.copy(fuelType = v) }

    private fun numeric(v: String) = v.filter { it.isDigit() || it == '.' }

    fun save(onDone: () -> Unit) {
        val s = _form.value
        if (!s.isValid) return
        viewModelScope.launch {
            repository.upsertFuel(
                FuelEntry(
                    id = fuelId,
                    tripId = tripId,
                    date = s.date,
                    cost = s.cost.toDoubleOrNull() ?: 0.0,
                    fuelType = s.fuelType
                )
            )
            onDone()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { FuelEditViewModel(voyageRepository(), createSavedStateHandle()) }
        }
    }
}
