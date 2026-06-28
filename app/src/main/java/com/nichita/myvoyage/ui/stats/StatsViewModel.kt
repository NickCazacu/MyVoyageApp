package com.nichita.myvoyage.ui.stats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nichita.myvoyage.data.db.CategorySum
import com.nichita.myvoyage.data.model.Category
import com.nichita.myvoyage.data.model.Trip
import com.nichita.myvoyage.data.repository.VoyageRepository
import com.nichita.myvoyage.domain.FuelCalculator
import com.nichita.myvoyage.domain.FuelStats
import com.nichita.myvoyage.ui.nav.NavArgs
import com.nichita.myvoyage.ui.voyageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Состояние экрана статистики одного рейса. */
data class StatsUiState(
    val trip: Trip? = null,
    /** Итог рейса: расходы + топливо. */
    val total: Double = 0.0,
    /** Разбивка по категориям (топливо включено), по убыванию. */
    val categorySums: List<CategorySum> = emptyList(),
    /** Расчёт по топливу. */
    val fuelStats: FuelStats = FuelStats(),
    /** Средний итог по прошлым рейсам (null — нет данных). */
    val avgOtherTotal: Double? = null,
    /** Отклонение текущего рейса от среднего (доля, напр. 0.2 = +20%). */
    val diffVsAvg: Double? = null
)

/** ViewModel статистики: всё считается реактивно из Room-потоков. */
class StatsViewModel(
    repository: VoyageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: Long = savedStateHandle.get<Long>(NavArgs.TRIP_ID) ?: 0L

    // Часть состояния по текущему рейсу.
    private val current = combine(
        repository.observeTrip(tripId),
        repository.observeCategorySums(tripId),
        repository.observeFuel(tripId),
        repository.observeTotal(tripId)
    ) { trip, expenseCatSums, fuel, expensesTotal ->
        val fuelStats = FuelCalculator.calculate(fuel)
        val total = expensesTotal + fuelStats.totalFuelCost
        Triple(trip, mergeFuelIntoCategories(expenseCatSums, fuelStats.totalFuelCost), fuelStats to total)
    }

    // Историческая часть: суммарные итоги по всем рейсам (для среднего).
    private val history = combine(
        repository.observeTotalsPerTrip(),
        repository.observeFuelTotalsPerTrip()
    ) { expenseTotals, fuelTotals ->
        val map = HashMap<Long, Double>()
        expenseTotals.forEach { map[it.tripId] = (map[it.tripId] ?: 0.0) + it.total }
        fuelTotals.forEach { map[it.tripId] = (map[it.tripId] ?: 0.0) + it.total }
        map
    }

    val uiState: StateFlow<StatsUiState> =
        combine(current, history) { cur, totalsByTrip ->
            val (trip, categorySums, fuelAndTotal) = cur
            val (fuelStats, total) = fuelAndTotal

            // Средний итог по ДРУГИМ рейсам.
            val others = totalsByTrip.filterKeys { it != tripId }.values.filter { it > 0.0 }
            val avg = others.takeIf { it.isNotEmpty() }?.average()
            val diff = if (avg != null && avg > 0.0) (total - avg) / avg else null

            StatsUiState(
                trip = trip,
                total = total,
                categorySums = categorySums,
                fuelStats = fuelStats,
                avgOtherTotal = avg,
                diffVsAvg = diff
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState()
        )

    /** Добавляет стоимость заправок в категорию «Топливо» и сортирует по убыванию. */
    private fun mergeFuelIntoCategories(
        expenseSums: List<CategorySum>,
        fuelCost: Double
    ): List<CategorySum> {
        if (fuelCost <= 0.0) return expenseSums.sortedByDescending { it.total }
        val map = expenseSums.associate { it.category to it.total }.toMutableMap()
        map[Category.FUEL] = (map[Category.FUEL] ?: 0.0) + fuelCost
        return map.map { CategorySum(it.key, it.value) }.sortedByDescending { it.total }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { StatsViewModel(voyageRepository(), createSavedStateHandle()) }
        }
    }
}
