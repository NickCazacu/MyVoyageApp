package com.nichita.myvoyage.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nichita.myvoyage.data.db.CategorySum
import com.nichita.myvoyage.data.model.Category
import com.nichita.myvoyage.data.model.Currency
import com.nichita.myvoyage.data.repository.VoyageRepository
import com.nichita.myvoyage.ui.voyageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Сводная статистика по одной валюте (рейсы этой валюты). */
data class CurrencyStats(
    val currency: Currency,
    val tripCount: Int,
    val totalSpent: Double,
    val avgPerTrip: Double,
    /** Разбивка по категориям (с учётом топлива), по убыванию. */
    val categorySums: List<CategorySum>
)

/** Состояние экрана сводной статистики по всем рейсам. */
data class OverallStatsUiState(
    /** Блоки по валютам (валюты не складываются), по убыванию суммы. */
    val blocks: List<CurrencyStats> = emptyList(),
    /** Всего рейсов (по всем валютам). */
    val tripCount: Int = 0
)

/**
 * ViewModel сводной статистики по ВСЕМ рейсам.
 *
 * Валюты не конвертируются (как и во всём приложении): итоги, средние и
 * разбивка по категориям считаются отдельно для каждой валюты рейса.
 */
class OverallStatsViewModel(
    repository: VoyageRepository
) : ViewModel() {

    val uiState: StateFlow<OverallStatsUiState> =
        combine(
            repository.observeTrips(),
            repository.observeTotalsPerTrip(),
            repository.observeFuelTotalsPerTrip(),
            repository.observeCategorySumsByCurrency()
        ) { trips, expenseTotals, fuelTotals, catByCurrency ->
            val expenseByTrip = expenseTotals.associate { it.tripId to it.total }
            val fuelByTrip = fuelTotals.associate { it.tripId to it.total }

            // Итог и количество рейсов по каждой валюте + стоимость топлива по валюте.
            val spentByCurrency = HashMap<Currency, Double>()
            val tripsByCurrency = HashMap<Currency, Int>()
            val fuelByCurrency = HashMap<Currency, Double>()
            trips.forEach { t ->
                val fuel = fuelByTrip[t.id] ?: 0.0
                val spent = (expenseByTrip[t.id] ?: 0.0) + fuel
                spentByCurrency[t.currency] = (spentByCurrency[t.currency] ?: 0.0) + spent
                tripsByCurrency[t.currency] = (tripsByCurrency[t.currency] ?: 0) + 1
                fuelByCurrency[t.currency] = (fuelByCurrency[t.currency] ?: 0.0) + fuel
            }

            // Разбивка по категориям расходов в разрезе валюты (без топлива-заправок).
            val categoriesByCurrency: Map<Currency, MutableMap<Category, Double>> =
                catByCurrency.groupBy { it.currency }.mapValues { (_, rows) ->
                    rows.associate { it.category to it.total }.toMutableMap()
                }

            val blocks = tripsByCurrency.keys.map { currency ->
                val cats = categoriesByCurrency[currency] ?: mutableMapOf()
                // Стоимость заправок добавляем в категорию «Топливо».
                val fuelCost = fuelByCurrency[currency] ?: 0.0
                if (fuelCost > 0.0) cats[Category.FUEL] = (cats[Category.FUEL] ?: 0.0) + fuelCost
                val count = tripsByCurrency[currency] ?: 0
                val total = spentByCurrency[currency] ?: 0.0
                CurrencyStats(
                    currency = currency,
                    tripCount = count,
                    totalSpent = total,
                    avgPerTrip = if (count > 0) total / count else 0.0,
                    categorySums = cats.map { CategorySum(it.key, it.value) }
                        .filter { it.total > 0.0 }
                        .sortedByDescending { it.total }
                )
            }.sortedByDescending { it.totalSpent }

            OverallStatsUiState(blocks = blocks, tripCount = trips.size)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OverallStatsUiState()
        )

    companion object {
        val Factory = viewModelFactory {
            initializer { OverallStatsViewModel(voyageRepository()) }
        }
    }
}
