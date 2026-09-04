package com.antigravity.telemetry

import android.app.Application
import com.antigravity.telemetry.core.database.AppDatabase
import com.antigravity.telemetry.core.repository.TelemetryRepository
import com.antigravity.telemetry.core.telemetry.TelemetryManager

class AntiGravityApp : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val repository: TelemetryRepository by lazy {
        TelemetryRepository(database)
    }

    val telemetryManager: TelemetryManager by lazy {
        TelemetryManager(repository)
    }

    val preferences: com.antigravity.telemetry.core.repository.FuelPreferences by lazy {
        com.antigravity.telemetry.core.repository.FuelPreferences(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
