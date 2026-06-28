package com.nichita.myvoyage.domain

import com.nichita.myvoyage.data.db.CategorySum
import com.nichita.myvoyage.data.model.Category
import com.nichita.myvoyage.data.model.Currency
import kotlin.math.roundToInt

/** Итог одного рейса с названием — для поиска самого дорогого. */
data class NamedTripTotal(
    val destination: String,
    val total: Double
)

/**
 * Входные данные для сводных советов по одной валюте.
 * Все суммы — в пределах одной валюты (приложение валюты не конвертирует).
 */
data class OverallTipsInput(
    val currency: Currency,
    val tripCount: Int,
    val total: Double,
    /** Разбивка по категориям (с учётом топлива), по убыванию. */
    val categorySums: List<CategorySum>,
    /** Итоги по рейсам этой валюты — для сравнения дорогих рейсов. */
    val tripTotals: List<NamedTripTotal>
)

/**
 * Сводный rule-based анализатор расходов по ВСЕМ рейсам (офлайн, без AI).
 * Даёт практические советы по оптимизации, опираясь на доли категорий
 * и разброс стоимости рейсов.
 */
object OverallTipsAnalyzer {

    private const val CATEGORY_SHARE_TIP = 0.20      // совет по категории от 20% расходов
    private const val CATEGORY_SHARE_WARN = 0.35     // выделяем предупреждением от 35%
    private const val PRICIER_THAN_AVG = 0.30        // рейс дороже среднего на 30%
    private const val MAX_CATEGORY_TIPS = 3

    fun analyze(input: OverallTipsInput): List<Tip> {
        val tips = mutableListOf<Tip>()
        val symbol = input.currency.symbol
        if (input.total <= 0.0 || input.tripCount == 0) return tips

        // 1. Сводка-итог.
        val avg = input.total / input.tripCount
        tips += Tip(
            TipSeverity.INFO,
            "Итог: ${input.tripCount} ${tripWord(input.tripCount)}",
            "Всего потрачено ${money(input.total, symbol)}, в среднем " +
                "${money(avg, symbol)} на рейс."
        )

        // 2. Советы по самым крупным категориям.
        input.categorySums
            .filter { it.total > 0.0 && it.total / input.total >= CATEGORY_SHARE_TIP }
            .take(MAX_CATEGORY_TIPS)
            .forEach { cs ->
                val share = (cs.total / input.total * 100).roundToInt()
                val severe = cs.total / input.total >= CATEGORY_SHARE_WARN
                tips += Tip(
                    if (severe) TipSeverity.WARNING else TipSeverity.INFO,
                    "${cs.category.title}: $share% расходов",
                    "${money(cs.total, symbol)} за всё время. ${advice(cs.category)}"
                )
            }

        // 3. Самый дорогой рейс заметно выше среднего.
        if (input.tripCount >= 2) {
            val priciest = input.tripTotals.maxByOrNull { it.total }
            if (priciest != null && avg > 0.0) {
                val diff = (priciest.total - avg) / avg
                if (diff >= PRICIER_THAN_AVG) {
                    tips += Tip(
                        TipSeverity.WARNING,
                        "Самый дорогой рейс: ${priciest.destination}",
                        "Обошёлся в ${money(priciest.total, symbol)} — на " +
                            "${(diff * 100).roundToInt()}% выше среднего. Посмотрите его расходы: " +
                            "там вероятнее всего и есть, на чём сэкономить."
                    )
                }
            }
        }

        // 4. Если ничего тревожного — поощрение.
        val onlySummary = tips.size == 1
        if (onlySummary) {
            tips += Tip(
                TipSeverity.POSITIVE,
                "Расходы сбалансированы",
                "Явных перекосов по категориям не видно. Так держать!"
            )
        }
        return tips
    }

    /** Практический совет под конкретную категорию. */
    private fun advice(category: Category): String = when (category) {
        Category.FUEL ->
            "Сравнивайте цены на АЗС через приложения-агрегаторы, заправляйтесь не на трассе, " +
                "держите давление в шинах и плавный стиль езды — расход падает."
        Category.TOLLS ->
            "Планируйте маршрут заранее: иногда виньетка на неделю или объезд платного участка " +
                "выходит дешевле разовых оплат."
        Category.PARKING ->
            "Используйте перехватывающие парковки и стоянки вне центра — там в разы дешевле."
        Category.MAINTENANCE ->
            "Делайте плановое ТО до рейса: ремонт в дороге почти всегда дороже и срывает планы."
        Category.INSURANCE ->
            "На частых рейсах сравните годовой полис с разовыми — годовой обычно выгоднее."
        Category.FOOD ->
            "Завтраки в жилье и закупки в супермаркетах вместо кафе у трассы заметно снижают траты."
        Category.LODGING ->
            "Бронируйте жильё заранее и не в самом центре; апартаменты с кухней экономят " +
                "и на проживании, и на еде."
        Category.OTHER ->
            "Разнесите крупные траты из «Прочего» по конкретным категориям — так видно, где резерв."
    }

    private fun tripWord(n: Int): String {
        val mod100 = n % 100
        val mod10 = n % 10
        return when {
            mod100 in 11..14 -> "рейсов"
            mod10 == 1 -> "рейс"
            mod10 in 2..4 -> "рейса"
            else -> "рейсов"
        }
    }

    private fun money(value: Double, symbol: String): String =
        "${(value * 100).roundToInt() / 100.0} $symbol"
}
