package com.nichita.myvoyage.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Расход, привязанный к рейсу.
 *
 * - Внешний ключ на [Trip] с CASCADE: при удалении рейса удаляются и расходы.
 * - Индекс по tripId ускоряет выборки "все расходы рейса" (часто используется).
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId"), Index("date")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Рейс, к которому относится расход */
    val tripId: Long,

    /** Сумма расхода */
    val amount: Double,

    /** Валюта расхода (по умолчанию совпадает с валютой рейса) */
    val currency: Currency = Currency.DEFAULT,

    /** Категория расхода */
    val category: Category = Category.OTHER,

    /** Дата расхода (epoch millis) */
    val date: Long,

    /** Необязательная заметка */
    val note: String = ""
)
