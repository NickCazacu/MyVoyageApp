package com.nichita.myvoyage.data.model

/**
 * Валюта. Набор ориентирован на еженедельные авто-рейсы по Европе/региону.
 * [code] — ISO-код (хранится в БД), [symbol] — символ для отображения.
 *
 * Приложение полностью офлайн и НЕ конвертирует валюты: суммы складываются
 * только в пределах одной валюты. Поэтому валюта рейса считается основной.
 */
enum class Currency(val code: String, val symbol: String) {
    EUR("EUR", "€"),
    RON("RON", "lei"),
    MDL("MDL", "L"),
    USD("USD", "$"),
    UAH("UAH", "₴"),
    PLN("PLN", "zł");

    companion object {
        val DEFAULT = EUR

        fun fromCode(value: String?): Currency =
            entries.firstOrNull { it.code == value } ?: DEFAULT
    }
}
