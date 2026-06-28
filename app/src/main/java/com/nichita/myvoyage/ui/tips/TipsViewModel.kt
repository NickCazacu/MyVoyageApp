package com.nichita.myvoyage.ui.tips

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nichita.myvoyage.data.db.CategorySum
import com.nichita.myvoyage.data.model.Category
import com.nichita.myvoyage.data.repository.VoyageRepository
import com.nichita.myvoyage.domain.FuelCalculator
import com.nichita.myvoyage.domain.Tip
import com.nichita.myvoyage.domain.TipsAnalyzer
import com.nichita.myvoyage.domain.TipsInput
import com.nichita.myvoyage.ui.nav.NavArgs
import com.nichita.myvoyage.ui.voyageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel раздела «Советы».
 * Собирает снимок данных по рейсу и истории, прогоняет через rule-based
 * [TipsAnalyzer] и отдаёт список советов. Пересчёт — по запросу/при входе.
 */
class TipsViewModel(
    private val repository: VoyageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: Long = savedStateHandle.get<Long>(NavArgs.TRIP_ID) ?: 0L

    private val _tips = MutableStateFlow<List<Tip>>(emptyList())
    val tips: StateFlow<List<Tip>> = _tips.asStateFlow()

    init {
        analyze()
    }

    fun analyze() {
        viewModelScope.launch {
            val trip = repository.getTrip(tripId) ?: return@launch

            // --- Текущий рейс ---
            val currentFuel = repository.getFuelForTrip(tripId)
            val currentFuelStats = FuelCalculator.calculate(currentFuel)
            val currentExpenseCatSums = repository.getCategorySumsForTrip(tripId)
            val currentCatSums = mergeFuel(currentExpenseCatSums, currentFuelStats.totalFuelCost)
            val currentTotal = currentCatSums.sumOf { it.total }

            // --- История (другие рейсы) ---
            val expenseTotals = repository.getTotalsPerTrip().associate { it.tripId to it.total }
            val fuelTotals = repository.getFuelTotalsPerTrip().associate { it.tripId to it.total }
            val combinedTotals = HashMap<Long, Double>()
            expenseTotals.forEach { combinedTotals[it.key] = (combinedTotals[it.key] ?: 0.0) + it.value }
            fuelTotals.forEach { combinedTotals[it.key] = (combinedTotals[it.key] ?: 0.0) + it.value }
            val otherTotals = combinedTotals.filterKeys { it != tripId }.values.toList()

            // Категории по всем рейсам (с учётом топлива) — для правила "топливо дороже всего".
            val allExpenseCatSums = repository.getCategorySumsAllTrips()
            val allFuelCost = fuelTotals.values.sum()
            val allCatSums = mergeFuel(allExpenseCatSums, allFuelCost)

            _tips.value = TipsAnalyzer.analyze(
                TipsInput(
                    trip = trip,
                    currentTotal = currentTotal,
                    currentCategorySums = currentCatSums,
                    otherTripTotals = otherTotals,
                    categorySumsAllTrips = allCatSums
                )
            )
        }
    }

    /** Складывает стоимость топлива в категорию FUEL. */
    private fun mergeFuel(expenseSums: List<CategorySum>, fuelCost: Double): List<CategorySum> {
        if (fuelCost <= 0.0) return expenseSums
        val map = expenseSums.associate { it.category to it.total }.toMutableMap()
        map[Category.FUEL] = (map[Category.FUEL] ?: 0.0) + fuelCost
        return map.map { CategorySum(it.key, it.value) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { TipsViewModel(voyageRepository(), createSavedStateHandle()) }
        }
    }
}
