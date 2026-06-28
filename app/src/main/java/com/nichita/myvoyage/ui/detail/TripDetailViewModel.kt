package com.nichita.myvoyage.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nichita.myvoyage.data.model.Expense
import com.nichita.myvoyage.data.model.FuelEntry
import com.nichita.myvoyage.data.model.Trip
import com.nichita.myvoyage.data.repository.VoyageRepository
import com.nichita.myvoyage.ui.nav.NavArgs
import com.nichita.myvoyage.ui.voyageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Состояние экрана рейса (детали): сам рейс, расходы, заправки, итог. */
data class TripDetailUiState(
    val trip: Trip? = null,
    val expenses: List<Expense> = emptyList(),
    val fuel: List<FuelEntry> = emptyList(),
    val total: Double = 0.0
)

/**
 * ViewModel экрана конкретного рейса.
 * Реактивно собирает всё, что относится к рейсу, в одно состояние.
 */
class TripDetailViewModel(
    private val repository: VoyageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: Long = savedStateHandle.get<Long>(NavArgs.TRIP_ID) ?: 0L

    val uiState: StateFlow<TripDetailUiState> =
        combine(
            repository.observeTrip(tripId),
            repository.observeExpenses(tripId),
            repository.observeFuel(tripId),
            repository.observeTotal(tripId)
        ) { trip, expenses, fuel, expensesTotal ->
            // Итог рейса = обычные расходы + стоимость заправок.
            val fuelCost = fuel.sumOf { it.cost }
            TripDetailUiState(trip, expenses, fuel, expensesTotal + fuelCost)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TripDetailUiState()
        )

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun deleteFuel(entry: FuelEntry) {
        viewModelScope.launch { repository.deleteFuel(entry) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { TripDetailViewModel(voyageRepository(), createSavedStateHandle()) }
        }
    }
}
