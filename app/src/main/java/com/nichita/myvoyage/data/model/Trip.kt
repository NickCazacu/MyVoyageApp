package com.nichita.myvoyage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Рейс — поездка, к которой привязываются расходы и заправки.
 *
 * Даты хранятся как epoch millis (Long) — это компактно, индексируется и
 * не требует TypeConverter для дат. [endDate] = null, пока рейс не завершён.
 */
@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Направление, напр. "Кишинёв → Бухарест" */
    val destination: String,

    /** Дата начала (epoch millis) */
    val startDate: Long,

    /** Дата конца (epoch millis), null — рейс ещё не завершён */
    val endDate: Long? = null,

    /** Основная валюта рейса */
    val currency: Currency = Currency.DEFAULT
)
