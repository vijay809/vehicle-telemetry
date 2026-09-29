package com.antigravity.telemetry

import android.app.Application
import androidx.car.app.connection.CarConnection
import com.antigravity.telemetry.core.database.AppDatabase
import com.antigravity.telemetry.core.repository.TelemetryRepository
import com.antigravity.telemetry.core.telemetry.AutoTelemetryLogger
import com.antigravity.telemetry.core.telemetry.TelemetryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AntiGravityApp : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val preferences: com.antigravity.telemetry.core.repository.FuelPreferences by lazy {
        com.antigravity.telemetry.core.repository.FuelPreferences(this)
    }

    val repository: TelemetryRepository by lazy {
        TelemetryRepository(database, preferences)
    }

    val telemetryManager: TelemetryManager by lazy {
        TelemetryManager(repository, this)
    }

    override fun onCreate() {
        super.onCreate()
        AutoTelemetryLogger.initialize(this)

        // Observe Android Auto connection lifecycle across entire application scope
        try {
            val carConnection = CarConnection(this)
            carConnection.type.observeForever { type ->
                val isConnected = (type == CarConnection.CONNECTION_TYPE_PROJECTION ||
                                   type == CarConnection.CONNECTION_TYPE_NATIVE)
                AutoTelemetryLogger.log(
                    "APP_CAR_CONNECTION",
                    "CarConnection state update: type=$type (isConnected=$isConnected)"
                )
                telemetryManager.onConnectionStateChanged(isConnected)
            }
        } catch (e: Exception) {
            AutoTelemetryLogger.log("APP_CAR_CONNECTION", "Failed to initialize CarConnection observer: ${e.message}")
        }

        // Seed existing or missing start event durations so they can be visually inspected immediately
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                repository.seedMissingStartDurations()
            } catch (e: Exception) {
                AutoTelemetryLogger.log("APP_INIT", "Failed to seed start durations: ${e.message}", isError = true)
            }
        }
    }
}
