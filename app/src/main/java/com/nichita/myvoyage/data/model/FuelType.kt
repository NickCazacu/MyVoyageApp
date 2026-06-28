package com.nichita.myvoyage.data.model

/**
 * Тип топлива. По умолчанию — дизель (основной сценарий пользователя).
 * [title] — отображаемое название на русском.
 */
enum class FuelType(val title: String) {
    DIESEL("Дизель"),
    PETROL_95("Бензин 95"),
    PETROL_98("Бензин 98"),
    LPG("Газ (LPG)");

    companion object {
        val DEFAULT = DIESEL

        fun fromName(value: String?): FuelType =
            entries.firstOrNull { it.name == value } ?: DEFAULT
    }
}
