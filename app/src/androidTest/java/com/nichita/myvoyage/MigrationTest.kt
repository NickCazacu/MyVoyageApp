package com.nichita.myvoyage

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nichita.myvoyage.data.db.AppDatabase
import com.nichita.myvoyage.data.db.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Проверяет, что миграции Room сохраняют данные пользователя (не пересоздают БД).
 * Использует экспортированные схемы из `app/schemas` (подключены как ассеты теста).
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDb = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate1To2_preservesData_andComputesFuelCost() {
        // --- Создаём БД версии 1 и наполняем данными старой схемы ---
        helper.createDatabase(testDb, 1).apply {
            execSQL(
                "INSERT INTO trips (id, destination, startDate, endDate, budget, currency) " +
                    "VALUES (1, 'Кишинёв → Бухарест', 1000, 2000, 500.0, 'EUR')"
            )
            // cost должен получиться liters * pricePerLiter = 40 * 1.5 = 60.0
            execSQL(
                "INSERT INTO fuel_entries (id, tripId, date, liters, pricePerLiter, odometer, fuelType) " +
                    "VALUES (1, 1, 1500, 40.0, 1.5, 123456.0, 'DIESEL')"
            )
            execSQL(
                "INSERT INTO expenses (id, tripId, amount, currency, category, date, note) " +
                    "VALUES (1, 1, 25.0, 'EUR', 'FOOD', 1600, 'обед')"
            )
            close()
        }

        // --- Применяем миграцию и валидируем против схемы версии 2 ---
        val db = helper.runMigrationsAndValidate(testDb, 2, true, MIGRATION_1_2)

        // Рейс сохранился (и колонка budget исчезла — валидация это уже проверила).
        db.query("SELECT destination, currency FROM trips WHERE id = 1").use { c ->
            assertTrue("Рейс должен сохраниться", c.moveToFirst())
            assertEquals("Кишинёв → Бухарест", c.getString(0))
            assertEquals("EUR", c.getString(1))
        }

        // Заправка: стоимость пересчитана из литров × цена за литр.
        db.query("SELECT cost, fuelType FROM fuel_entries WHERE id = 1").use { c ->
            assertTrue("Заправка должна сохраниться", c.moveToFirst())
            assertEquals(60.0, c.getDouble(0), 0.001)
            assertEquals("DIESEL", c.getString(1))
        }

        // Расход не затронут.
        db.query("SELECT amount, note FROM expenses WHERE id = 1").use { c ->
            assertTrue("Расход должен сохраниться", c.moveToFirst())
            assertEquals(25.0, c.getDouble(0), 0.001)
            assertEquals("обед", c.getString(1))
        }

        db.close()
    }
}
