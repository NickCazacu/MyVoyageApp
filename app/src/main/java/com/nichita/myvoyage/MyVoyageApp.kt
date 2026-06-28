package com.nichita.myvoyage

import android.app.Application
import com.nichita.myvoyage.data.db.AppDatabase
import com.nichita.myvoyage.data.repository.VoyageRepository

/**
 * Application — точка ручного DI (без Hilt, чтобы держать минимум зависимостей).
 * БД и репозиторий создаются лениво и живут всё время работы приложения.
 */
class MyVoyageApp : Application() {

    private val database by lazy { AppDatabase.getInstance(this) }

    val repository: VoyageRepository by lazy {
        VoyageRepository(
            tripDao = database.tripDao(),
            expenseDao = database.expenseDao(),
            fuelDao = database.fuelDao()
        )
    }
}
