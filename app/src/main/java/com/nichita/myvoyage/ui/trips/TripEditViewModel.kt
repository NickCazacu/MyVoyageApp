package com.nichita.myvoyage.ui.trips

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nichita.myvoyage.data.model.Currency
import com.nichita.myvoyage.data.model.Trip
import com.nichita.myvoyage.data.repository.VoyageRepository
import com.nichita.myvoyage.ui.nav.NavArgs
import com.nichita.myvoyage.ui.voyageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Состояние формы рейса (поля как строки — удобно для TextField). */
data class TripFormState(
    /** Пункт отправления (необязателен). */
    val origin: String = "",
    /** Пункт назначения (обязателен). */
    val destination: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val currency: Currency = Currency.DEFAULT,
    val isEditing: Boolean = false
) {
    /** Можно ли сохранять: указано хотя бы место назначения. */
    val isValid: Boolean get() = destination.isNotBlank()
}

/**
 * ViewModel создания/редактирования рейса.
 * tripId берётся из аргумента навигации (0 — новый рейс).
 */
class TripEditViewModel(
    private val repository: VoyageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: Long = savedStateHandle.get<Long>(NavArgs.TRIP_ID) ?: 0L

    private val _form = MutableStateFlow(TripFormState())
    val form: StateFlow<TripFormState> = _form.asStateFlow()

    init {
        if (tripId != 0L) {
            viewModelScope.launch {
                repository.getTrip(tripId)?.let { t ->
                    // Существующее направление вида "А → Б" разбиваем на два поля.
                    val arrow = t.destination.indexOf('→')
                    val origin = if (arrow >= 0) t.destination.substring(0, arrow).trim() else ""
                    val dest =
                        if (arrow >= 0) t.destination.substring(arrow + 1).trim()
                        else t.destination.trim()
                    _form.value = TripFormState(
                        origin = origin,
                        destination = dest,
                        startDate = t.startDate,
                        endDate = t.endDate,
                        currency = t.currency,
                        isEditing = true
                    )
                }
            }
        }
    }

    fun onOriginChange(v: String) = _form.update { it.copy(origin = v) }
    fun onDestinationChange(v: String) = _form.update { it.copy(destination = v) }
    fun onStartDateChange(v: Long) = _form.update { it.copy(startDate = v) }
    fun onEndDateChange(v: Long?) = _form.update { it.copy(endDate = v) }
    fun onCurrencyChange(v: Currency) = _form.update { it.copy(currency = v) }

    /** Сохраняет рейс и вызывает [onDone] в главном потоке после записи. */
    fun save(onDone: () -> Unit) {
        val s = _form.value
        if (!s.isValid) return
        // Склеиваем в "Откуда → Куда"; если отправление не указано — только назначение.
        val fullDestination = if (s.origin.isNotBlank())
            "${s.origin.trim()} → ${s.destination.trim()}"
        else
            s.destination.trim()
        viewModelScope.launch {
            repository.upsertTrip(
                Trip(
                    id = tripId,
                    destination = fullDestination,
                    startDate = s.startDate,
                    endDate = s.endDate,
                    currency = s.currency
                )
            )
            onDone()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { TripEditViewModel(voyageRepository(), createSavedStateHandle()) }
        }
    }
}
