package com.nichita.myvoyage.data.model

/**
 * Категория расхода.
 * [title] — отображаемое название на русском (используется в UI).
 *
 * Перечисление хранится в Room как строка (имя константы) через TypeConverter,
 * поэтому переименовывать константы без миграции нельзя.
 */
enum class Category(val title: String) {
    FUEL("Топливо"),
    TOLLS("Платные дороги/виньетки"),
    PARKING("Парковка"),
    MAINTENANCE("ТО/ремонт"),
    INSURANCE("Страховка"),
    FOOD("Еда"),
    LODGING("Жильё"),
    OTHER("Прочее");

    companion object {
        /** Безопасный разбор из строки БД (на случай неизвестного значения). */
        fun fromName(value: String?): Category =
            entries.firstOrNull { it.name == value } ?: OTHER
    }
}
