package com.antigravity.telemetry.core.telemetry

import com.antigravity.telemetry.core.model.TelemetrySnapshot
import com.antigravity.telemetry.core.repository.TelemetryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class StationaryRefillPrompt(
    val detectedOdometerKm: Double,
    val fuelLevelDeltaPercent: Double
)

class TelemetryManager(
    private val repository: TelemetryRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private var simulationJob: Job? = null
    private var isSimulating = false

    private val _refillPromptFlow = MutableSharedFlow<StationaryRefillPrompt>(replay = 0)
    val refillPromptFlow: SharedFlow<StationaryRefillPrompt> = _refillPromptFlow.asSharedFlow()

    private var previousFuelPercent: Double? = null
    private var previousSpeed: Double = 0.0

    fun onOdometerUpdate(odometerKm: Double) {
        if (odometerKm > 0.0) {
            repository.updateActualOdometer(odometerKm)
        }
    }

    fun onSpeedUpdate(speedKmh: Double) {
        previousSpeed = speedKmh
        repository.updateActualSpeed(speedKmh)
    }

    fun onFuelLevelUpdate(
        currentFuelPercent: Double,
        currentOdometer: Double? = null,
        isLowFuel: Boolean? = null
    ) {
        val odo = currentOdometer ?: repository.getLatestActualOdometer()
        // Stationary Refill Detection Heuristic:
        // If fuel increases by >= 8% while vehicle speed is 0 km/h
        val prev = previousFuelPercent
        if (prev != null && previousSpeed == 0.0 && (currentFuelPercent - prev) >= 8.0) {
            scope.launch {
                _refillPromptFlow.emit(
                    StationaryRefillPrompt(
                        detectedOdometerKm = odo,
                        fuelLevelDeltaPercent = currentFuelPercent - prev
                    )
                )
            }
        }
        previousFuelPercent = currentFuelPercent
        repository.updateActualFuel(
            fuelPercent = currentFuelPercent,
            isLowFuel = isLowFuel ?: (currentFuelPercent <= 15.0)
        )
        if (currentOdometer != null && currentOdometer > 0.0) {
            repository.updateActualOdometer(currentOdometer)
        }
    }

    fun onConnectionStateChanged(isConnected: Boolean) {
        repository.updateActualConnection(isConnected)
    }

    fun startDriveSimulation() {
        if (isSimulating) return
        isSimulating = true
        repository.setSimulationMode(true)
        simulationJob = scope.launch {
            val lastSimEvent = repository.getEventsAsc(true).lastOrNull()?.odometerKm ?: 9284.0
            var odo = maxOf(lastSimEvent, 9284.0)
            var speed = 45.0
            var pressure = 32.0

            while (isActive && isSimulating) {
                delay(1000)
                odo += 0.25
                pressure = maxOf(0.5, pressure - 0.05)
                repository.updateSimulatedTelemetry(
                    TelemetrySnapshot(
                        odometerKm = String.format(java.util.Locale.US, "%.2f", odo).toDouble(),
                        speedKmh = speed,
                        petrolPercent = 25.0,
                        cngPressureBar = String.format(java.util.Locale.US, "%.1f", pressure).toDouble(),
                        isAutoModeActive = true,
                        isLowFuelWarning = pressure < 2.0,
                        isConnectedToAuto = true,
                        isSimulation = true
                    )
                )
            }
        }
    }

    fun stopDriveSimulation() {
        isSimulating = false
        repository.setSimulationMode(false)
        simulationJob?.cancel()
        simulationJob = null
    }

    fun triggerStationaryRefillSimulation(jumpPercent: Double = 25.0) {
        scope.launch {
            val currentOdo = 42850.0
            _refillPromptFlow.emit(
                StationaryRefillPrompt(
                    detectedOdometerKm = currentOdo,
                    fuelLevelDeltaPercent = jumpPercent
                )
            )
        }
    }

    fun isSimulating(): Boolean = isSimulating
}
