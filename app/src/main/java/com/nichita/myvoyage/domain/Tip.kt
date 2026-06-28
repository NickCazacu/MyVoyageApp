package com.nichita.myvoyage.domain

/** Уровень важности совета — влияет на цвет/иконку в UI. */
enum class TipSeverity { POSITIVE, INFO, WARNING, ALERT }

/**
 * Совет по оптимизации затрат (результат rule-based анализа).
 * Полностью офлайн, без AI.
 */
data class Tip(
    val severity: TipSeverity,
    val title: String,
    val message: String
)
