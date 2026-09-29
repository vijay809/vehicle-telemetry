package com.antigravity.telemetry.core.telemetry

import android.content.Context
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
    private val context: Context? = null,
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
        val wasConnected = repository.preferences?.isVehicleConnected()
            ?: repository.vehicleHardwareState.value.isConnected

        if (isConnected == wasConnected) {
            return
        }
        if (isConnected) {
            handleCarConnected()
        } else {
            handleCarDisconnected()
        }
        repository.preferences?.setVehicleConnected(isConnected)
        repository.updateActualConnection(isConnected)
    }

    private fun handleCarConnected() {
        val prefs = repository.preferences ?: return
        val now = System.currentTimeMillis()
        val lastDisconnect = prefs.getLastAutoDisconnectedTimestamp()
        val lastHeartbeat = prefs.getLastConnectionHeartbeat()
        val lastStartLogged = prefs.getLastEngineStartLoggedTimestamp()
        val thresholdHours = prefs.getColdStartThresholdHours()
        val thresholdMs = (thresholdHours * 60 * 60 * 1000L).toLong()

        // Debounce 1: If an engine start was logged within the last 2 minutes, ignore rapid bounce
        if (lastStartLogged > 0L && (now - lastStartLogged) < 2 * 60 * 1000L) {
            AutoTelemetryLogger.log(
                "ENGINE_START",
                "Ignored connect: start already logged ${(now - lastStartLogged) / 1000}s ago (< 120s)"
            )
            context?.let { VehicleMonitoringService.start(it) }
            return
        }

        // Most recent known active or disconnect time
        val lastActive = maxOf(lastDisconnect, lastHeartbeat)

        // Debounce 2: If the vehicle was active less than 2 minutes ago, treat as ongoing trip / cable jiggle
        if (lastActive > 0L && lastStartLogged > 0L && (now - lastActive) < 2 * 60 * 1000L) {
            AutoTelemetryLogger.log(
                "ENGINE_START",
                "Ignored transient reconnect: active ${(now - lastActive) / 1000}s ago (< 120s)"
            )
            context?.let { VehicleMonitoringService.start(it) }
            return
        }

        // Calculate cooldown duration from most recent activity
        val cooldownMs = if (lastActive > 0L) {
            now - lastActive
        } else if (lastStartLogged > 0L) {
            now - lastStartLogged
        } else {
            Long.MAX_VALUE // Initial baseline connection is Cold Start
        }

        val isColdStart = (cooldownMs >= thresholdMs)

        prefs.setLastEngineStartLoggedTimestamp(now)
        prefs.setLastAutoConnectedTimestamp(now)
        prefs.setActiveStartConnectTimestamp(now)
        prefs.setLastConnectionHeartbeat(now)
        prefs.setVehicleConnected(true)

        // Start Foreground Service to keep app process alive throughout driving
        context?.let { VehicleMonitoringService.start(it) }

        scope.launch {
            val currentOdo = repository.getLatestActualOdometer()
            val eventId = repository.logEngineStart(
                isColdStart = isColdStart,
                odometerKm = currentOdo,
                timestamp = now
            )
            prefs.setActiveStartEventId(eventId)
            AutoTelemetryLogger.log(
                "ENGINE_START",
                "Engine start detected -> ${if (isColdStart) "COLD START" else "WARM START"} (cooldown: ${cooldownMs / 60000}m) logged @ $currentOdo km"
            )
        }
    }

    private fun handleCarDisconnected() {
        val prefs = repository.preferences ?: return
        val now = System.currentTimeMillis()
        prefs.setLastAutoDisconnectedTimestamp(now)
        prefs.setVehicleConnected(false)

        val connectTime = prefs.getActiveStartConnectTimestamp()
        val activeEventId = prefs.getActiveStartEventId()
        val driveMinutes = if (connectTime > 0L) {
            maxOf(1, ((now - connectTime) / (60 * 1000L)).toInt())
        } else null

        if (activeEventId != null && driveMinutes != null) {
            scope.launch {
                repository.updateEngineStartDuration(activeEventId, driveMinutes)
            }
            prefs.setActiveStartEventId(null)
            prefs.setActiveStartConnectTimestamp(0L)
            AutoTelemetryLogger.log(
                "ENGINE_STOP",
                "Android Auto disconnected -> Trip completed: $driveMinutes min drive"
            )
        } else {
            AutoTelemetryLogger.log(
                "ENGINE_STOP",
                "Android Auto disconnected -> disconnect timestamp recorded for cooldown tracking"
            )
        }

        // Stop Foreground Service
        context?.let { VehicleMonitoringService.stop(it) }
    }

    fun simulateColdStart() {
        val prefs = repository.preferences
        val now = System.currentTimeMillis()
        prefs?.setLastEngineStartLoggedTimestamp(now)
        prefs?.setActiveStartConnectTimestamp(now)
        scope.launch {
            val currentOdo = repository.getLatestActualOdometer()
            val eventId = repository.logEngineStart(
                isColdStart = true,
                odometerKm = currentOdo,
                timestamp = now
            )
            prefs?.setActiveStartEventId(eventId)
        }
    }

    fun simulateWarmStart() {
        val prefs = repository.preferences
        val now = System.currentTimeMillis()
        prefs?.setLastEngineStartLoggedTimestamp(now)
        prefs?.setActiveStartConnectTimestamp(now)
        scope.launch {
            val currentOdo = repository.getLatestActualOdometer()
            val eventId = repository.logEngineStart(
                isColdStart = false,
                odometerKm = currentOdo,
                timestamp = now
            )
            prefs?.setActiveStartEventId(eventId)
        }
    }

    fun simulateTripEnd(durationMinutes: Int = 15) {
        val prefs = repository.preferences ?: return
        val activeEventId = prefs.getActiveStartEventId()
        if (activeEventId != null) {
            scope.launch {
                repository.updateEngineStartDuration(activeEventId, durationMinutes)
            }
            prefs.setActiveStartEventId(null)
            prefs.setActiveStartConnectTimestamp(0L)
        }
        val now = System.currentTimeMillis()
        prefs.setLastAutoDisconnectedTimestamp(now)
        prefs.setVehicleConnected(false)
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
