package com.nichita.myvoyage.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nichita.myvoyage.data.model.Trip
import kotlinx.coroutines.flow.Flow

/** Доступ к рейсам. Чтения отдаются как Flow для реактивного UI. */
@Dao
interface TripDao {

    /** Все рейсы, новые сверху. */
    @Query("SELECT * FROM trips ORDER BY startDate DESC")
    fun observeAll(): Flow<List<Trip>>

    /** Один рейс по id (Flow, чтобы экран обновлялся при изменении). */
    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    fun observeById(tripId: Long): Flow<Trip?>

    /** Разовое чтение рейса (для domain-расчётов). */
    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    suspend fun getById(tripId: Long): Trip?

    /** Все рейсы разово (для сравнения со средним). */
    @Query("SELECT * FROM trips ORDER BY startDate DESC")
    suspend fun getAll(): List<Trip>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: Trip): Long

    @Update
    suspend fun update(trip: Trip)

    @Delete
    suspend fun delete(trip: Trip)
}
