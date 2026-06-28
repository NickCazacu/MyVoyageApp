package com.nichita.myvoyage.data.db

import androidx.room.TypeConverter
import com.nichita.myvoyage.data.model.Category
import com.nichita.myvoyage.data.model.Currency
import com.nichita.myvoyage.data.model.FuelType

/**
 * TypeConverter'ы для Room: enum'ы хранятся как строки.
 * Даты НЕ конвертируем — они уже Long (epoch millis) в сущностях.
 */
class Converters {

    @TypeConverter
    fun categoryToString(value: Category): String = value.name

    @TypeConverter
    fun stringToCategory(value: String?): Category = Category.fromName(value)

    @TypeConverter
    fun currencyToString(value: Currency): String = value.code

    @TypeConverter
    fun stringToCurrency(value: String?): Currency = Currency.fromCode(value)

    @TypeConverter
    fun fuelTypeToString(value: FuelType): String = value.name

    @TypeConverter
    fun stringToFuelType(value: String?): FuelType = FuelType.fromName(value)
}
