package com.nichita.myvoyage.domain

import com.nichita.myvoyage.data.model.FuelEntry

/**
 * Результат расчёта по топливу для одного рейса.
 */
data class FuelStats(
    /** Полная стоимость топлива за рейс. */
    val totalFuelCost: Double = 0.0,
    /** Количество заправок. */
    val entriesCount: Int = 0
)

/** Суммирует стоимость заправок рейса. */
object FuelCalculator {

    fun calculate(entries: List<FuelEntry>): FuelStats =
        FuelStats(
            totalFuelCost = entries.sumOf { it.cost },
            entriesCount = entries.size
        )
}
