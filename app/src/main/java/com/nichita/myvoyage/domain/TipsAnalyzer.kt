package com.nichita.myvoyage.domain

import com.nichita.myvoyage.data.db.CategorySum
import com.nichita.myvoyage.data.model.Category
import com.nichita.myvoyage.data.model.Trip
import kotlin.math.roundToInt

/**
 * Входные данные для анализа советов по одному рейсу.
 * Историю (другие рейсы) передаём уже без текущего рейса.
 */
data class TipsInput(
    val trip: Trip,
    val currentTotal: Double,
    val currentCategorySums: List<CategorySum>,
    /** Итоги ДРУГИХ рейсов (без текущего) — для сравнения со средним. */
    val otherTripTotals: List<Double>,
    /** Разбивка по категориям по ВСЕМ рейсам — чтобы найти самую дорогую. */
    val categorySumsAllTrips: List<CategorySum>
)

/**
 * Rule-based анализатор. Никакого AI и сети — только пороги и арифметика.
 * Пороги вынесены в константы, чтобы их было легко настроить.
 */
object TipsAnalyzer {

    // Пороги срабатывания правил
    private const val PRICIER_THAN_AVG = 0.20        // рейс дороже среднего на 20%

    fun analyze(input: TipsInput): List<Tip> {
        val tips = mutableListOf<Tip>()
        val symbol = input.trip.currency.symbol

        ruleComparedToAverage(input, symbol, tips)
        ruleFuelMostExpensive(input, tips)

        // Если ничего не сработало — нейтральное сообщение.
        if (tips.isEmpty()) {
            tips += Tip(
                TipSeverity.POSITIVE,
                "Всё в порядке",
                "Явных проблем с затратами не обнаружено. Так держать!"
            )
        }
        return tips
    }

    /** Рейс дороже/дешевле среднего по прошлым рейсам. */
    private fun ruleComparedToAverage(input: TipsInput, symbol: String, out: MutableList<Tip>) {
        val others = input.otherTripTotals.filter { it > 0.0 }
        if (others.isEmpty() || input.currentTotal <= 0.0) return
        val avg = others.average()
        if (avg <= 0.0) return

        val diff = (input.currentTotal - avg) / avg
        when {
            diff >= PRICIER_THAN_AVG -> out += Tip(
                TipSeverity.WARNING,
                "Дороже среднего на ${(diff * 100).roundToInt()}%",
                "Этот рейс дороже среднего по прошлым рейсам " +
                    "(${money(avg, symbol)}) на ${(diff * 100).roundToInt()}%."
            )
            diff <= -PRICIER_THAN_AVG -> out += Tip(
                TipSeverity.POSITIVE,
                "Дешевле среднего на ${(-diff * 100).roundToInt()}%",
                "Этот рейс обошёлся дешевле среднего (${money(avg, symbol)}). Отличная экономия!"
            )
        }
    }

    /** Топливо стабильно — самая дорогая категория по всем рейсам. */
    private fun ruleFuelMostExpensive(input: TipsInput, out: MutableList<Tip>) {
        val all = input.categorySumsAllTrips.filter { it.total > 0.0 }
        if (all.isEmpty()) return
        val top = all.maxByOrNull { it.total } ?: return
        if (top.category == Category.FUEL) {
            out += Tip(
                TipSeverity.INFO,
                "Топливо — главная статья расходов",
                "Топливо стабильно дороже всего. Планируйте заправки заранее, " +
                    "избегайте АЗС на трассе и сравнивайте цены вдоль маршрута."
            )
        }
    }

    // --- Форматирование (без зависимостей от Android, чтобы было тестируемо) ---
    private fun money(value: Double, symbol: String): String =
        "${(value * 100).roundToInt() / 100.0} $symbol"
}
