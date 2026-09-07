package com.antigravity.telemetry.car

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.hardware.CarHardwareManager
import androidx.car.app.hardware.common.CarValue
import androidx.car.app.hardware.common.OnCarDataAvailableListener
import androidx.car.app.hardware.info.Accelerometer
import androidx.car.app.hardware.info.CarHardwareLocation
import androidx.car.app.hardware.info.CarInfo
import androidx.car.app.hardware.info.CarSensors
import androidx.car.app.hardware.info.Compass
import androidx.car.app.hardware.info.EnergyLevel
import androidx.car.app.hardware.info.EnergyProfile
import androidx.car.app.hardware.info.EvStatus
import androidx.car.app.hardware.info.Gyroscope
import androidx.car.app.hardware.info.Mileage
import androidx.car.app.hardware.info.Speed
import androidx.car.app.hardware.info.TollCard
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.validation.HostValidator
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.antigravity.telemetry.AntiGravityApp
import com.antigravity.telemetry.core.model.EventSource
import com.antigravity.telemetry.core.model.EventType
import com.antigravity.telemetry.core.model.FuelEvent
import com.antigravity.telemetry.core.model.HardwareStatus
import com.antigravity.telemetry.core.model.TelemetrySnapshot
import com.antigravity.telemetry.core.telemetry.AutoTelemetryLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class AntiGravityCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return AntiGravityCarSession()
    }
}

class AntiGravityCarSession : Session(), DefaultLifecycleObserver {
    private val app by lazy { carContext.applicationContext as AntiGravityApp }

    init {
        lifecycle.addObserver(this)
    }

    override fun onCreateScreen(intent: Intent): Screen {
        return TelemetryCarScreen(carContext)
    }

    override fun onStart(owner: LifecycleOwner) {
        AutoTelemetryLogger.log("SESSION", "Android Auto in-car session started.")
        app.telemetryManager.onConnectionStateChanged(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        AutoTelemetryLogger.log("SESSION", "Android Auto in-car session stopped.")
        app.telemetryManager.onConnectionStateChanged(false)
    }
}

class TelemetryCarScreen(carContext: CarContext) : Screen(carContext), DefaultLifecycleObserver {

    private val app = carContext.applicationContext as AntiGravityApp
    private val repository = app.repository
    private val telemetryManager = app.telemetryManager
    private val scope = CoroutineScope(Dispatchers.Main)

    private var currentSnapshot: TelemetrySnapshot? = null
    private var carInfo: CarInfo? = null
    private var carSensors: CarSensors? = null

    // 1. Mileage Listener
    private val mileageListener = OnCarDataAvailableListener<Mileage> { mileage ->
        val status = mileage.odometerMeters.toHardwareStatus()
        val rawMeters = mileage.odometerMeters.value
        val km = if (rawMeters != null && rawMeters > 0f) rawMeters / 1000.0 else null

        AutoTelemetryLogger.log(
            "MILEAGE",
            "Odometer: ${km?.let { String.format(Locale.US, "%.2f km", it) } ?: "null"}, Raw: $rawMeters m, Status: ${mileage.odometerMeters.status}"
        )

        if (km != null) {
            telemetryManager.onOdometerUpdate(km)
        }
        repository.updateVehicleHardwareState {
            it.copy(
                odometerKm = km ?: it.odometerKm,
                odometerMetersRaw = rawMeters,
                mileageStatus = status
            )
        }
    }

    // 2. Speed Listener
    private val speedListener = OnCarDataAvailableListener<Speed> { speed ->
        val rawMps = if (speed.displaySpeedMetersPerSecond.status == CarValue.STATUS_SUCCESS) {
            speed.displaySpeedMetersPerSecond.value
        } else if (speed.rawSpeedMetersPerSecond.status == CarValue.STATUS_SUCCESS) {
            speed.rawSpeedMetersPerSecond.value
        } else null

        val kmh = rawMps?.let { it * 3.6 }
        val status = speed.displaySpeedMetersPerSecond.toHardwareStatus()

        AutoTelemetryLogger.log(
            "SPEED",
            "Speed: ${kmh?.let { String.format(Locale.US, "%.1f km/h", it) } ?: "null"}, Raw: $rawMps m/s, Status: ${speed.displaySpeedMetersPerSecond.status}"
        )

        if (kmh != null) {
            telemetryManager.onSpeedUpdate(kmh)
        }
        repository.updateVehicleHardwareState {
            it.copy(
                speedKmh = kmh ?: it.speedKmh,
                speedMpsRaw = rawMps,
                speedStatus = status
            )
        }
    }

    // 3. Energy Level Listener
    private val energyListener = OnCarDataAvailableListener<EnergyLevel> { energy ->
        val fuelPct = if (energy.fuelPercent.status == CarValue.STATUS_SUCCESS) {
            energy.fuelPercent.value?.toDouble()
        } else null

        val isLow = if (energy.energyIsLow.status == CarValue.STATUS_SUCCESS) {
            energy.energyIsLow.value
        } else null

        val rangeMeters = if (energy.rangeRemainingMeters.status == CarValue.STATUS_SUCCESS) {
            energy.rangeRemainingMeters.value
        } else null
        val rangeKm = rangeMeters?.let { it / 1000.0 }

        val batteryPct = if (energy.batteryPercent.status == CarValue.STATUS_SUCCESS) {
            energy.batteryPercent.value?.toDouble()
        } else null

        AutoTelemetryLogger.log(
            "ENERGY",
            "Fuel: $fuelPct%, LowFuel: $isLow, Range: ${rangeKm?.let { String.format(Locale.US, "%.1f km", it) }}, Battery: $batteryPct%, Status: ${energy.fuelPercent.status}"
        )

        if (fuelPct != null) {
            telemetryManager.onFuelLevelUpdate(currentFuelPercent = fuelPct, isLowFuel = isLow)
        }
        repository.updateVehicleHardwareState {
            it.copy(
                fuelPercent = fuelPct ?: it.fuelPercent,
                batteryPercent = batteryPct,
                isLowFuel = isLow ?: it.isLowFuel,
                rangeRemainingKm = rangeKm ?: it.rangeRemainingKm,
                energyStatus = energy.fuelPercent.toHardwareStatus()
            )
        }
    }

    // 4. Toll Card Listener
    private val tollListener = OnCarDataAvailableListener<TollCard> { toll ->
        val tollStr = when (toll.cardState.value) {
            TollCard.TOLLCARD_STATE_VALID -> "Valid"
            TollCard.TOLLCARD_STATE_INVALID -> "Invalid"
            TollCard.TOLLCARD_STATE_NOT_INSERTED -> "Not Inserted"
            else -> "Unknown"
        }
        AutoTelemetryLogger.log("TOLL", "Toll Card State: $tollStr, Status: ${toll.cardState.status}")
        repository.updateVehicleHardwareState {
            it.copy(
                tollCardState = tollStr,
                tollStatus = toll.cardState.toHardwareStatus()
            )
        }
    }

    // 5. EV Status Listener
    private val evStatusListener = OnCarDataAvailableListener<EvStatus> { ev ->
        val portOpen = ev.evChargePortOpen.value
        val connected = ev.evChargePortConnected.value
        AutoTelemetryLogger.log("EV_STATUS", "PortOpen: $portOpen, Connected: $connected")
        repository.updateVehicleHardwareState {
            it.copy(
                evPortOpen = portOpen,
                evPortConnected = connected,
                evStatus = ev.evChargePortOpen.toHardwareStatus()
            )
        }
    }

    // 6. Compass Listener
    private val compassListener = OnCarDataAvailableListener<Compass> { compass ->
        val list = compass.orientations.value
        val yaw = list?.getOrNull(0)
        val pitch = list?.getOrNull(1)
        val roll = list?.getOrNull(2)
        AutoTelemetryLogger.log("COMPASS", "Yaw: $yaw, Pitch: $pitch, Roll: $roll, Status: ${compass.orientations.status}")
        repository.updateVehicleHardwareState {
            it.copy(
                compassYaw = yaw,
                compassPitch = pitch,
                compassRoll = roll,
                compassStatus = compass.orientations.toHardwareStatus()
            )
        }
    }

    // 7. Hardware GPS Listener
    private val locationListener = OnCarDataAvailableListener<CarHardwareLocation> { locVal ->
        val loc = locVal.location.value
        AutoTelemetryLogger.log(
            "GPS",
            "Lat: ${loc?.latitude}, Lon: ${loc?.longitude}, Alt: ${loc?.altitude}, Bearing: ${loc?.bearing}, Status: ${locVal.location.status}"
        )
        repository.updateVehicleHardwareState {
            it.copy(
                latitude = loc?.latitude,
                longitude = loc?.longitude,
                altitude = loc?.altitude,
                bearing = loc?.bearing,
                locationStatus = locVal.location.toHardwareStatus()
            )
        }
    }

    // 8. Accelerometer Listener
    private val accelListener = OnCarDataAvailableListener<Accelerometer> { accel ->
        val f = accel.forces.value
        val x = f?.getOrNull(0)
        val y = f?.getOrNull(1)
        val z = f?.getOrNull(2)
        AutoTelemetryLogger.log("ACCEL", "Forces [X: $x, Y: $y, Z: $z], Status: ${accel.forces.status}")
        repository.updateVehicleHardwareState {
            it.copy(
                accelX = x,
                accelY = y,
                accelZ = z,
                accelStatus = accel.forces.toHardwareStatus()
            )
        }
    }

    // 9. Gyroscope Listener
    private val gyroListener = OnCarDataAvailableListener<Gyroscope> { gyro ->
        val r = gyro.rotations.value
        val x = r?.getOrNull(0)
        val y = r?.getOrNull(1)
        val z = r?.getOrNull(2)
        AutoTelemetryLogger.log("GYRO", "Rotations [X: $x, Y: $y, Z: $z], Status: ${gyro.rotations.status}")
        repository.updateVehicleHardwareState {
            it.copy(
                gyroX = x,
                gyroY = y,
                gyroZ = z,
                gyroStatus = gyro.rotations.toHardwareStatus()
            )
        }
    }

    init {
        lifecycle.addObserver(this)

        scope.launch {
            repository.telemetryState.collect { snapshot ->
                currentSnapshot = snapshot
                invalidate()
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        setupCarHardware()
    }

    override fun onPause(owner: LifecycleOwner) {
        teardownCarHardware()
    }

    private fun setupCarHardware() {
        try {
            val permissions = listOf(
                "android.car.permission.CAR_MILEAGE",
                "android.car.permission.CAR_ENERGY",
                "android.car.permission.CAR_SPEED",
                "android.permission.ACCESS_FINE_LOCATION",
                "androidx.car.app.CAR_INFO"
            )
            carContext.requestPermissions(permissions) { _, _ ->
                registerListeners()
            }
        } catch (e: Exception) {
            AutoTelemetryLogger.log("PERM_ERROR", "Permission request failed: ${e.message}", isError = true)
            registerListeners()
        }
    }

    private fun registerListeners() {
        try {
            val hardwareManager = carContext.getCarService(CarHardwareManager::class.java)
            val info = hardwareManager?.carInfo
            val sensors = hardwareManager?.carSensors
            carInfo = info
            carSensors = sensors

            val executor = ContextCompat.getMainExecutor(carContext)

            // Static Vehicle Queries
            info?.let { carInfo ->
                try {
                    carInfo.fetchModel(executor) { model ->
                        AutoTelemetryLogger.log(
                            "MODEL",
                            "Make: ${model.manufacturer.value}, Model: ${model.name.value}, Year: ${model.year.value}, Status: ${model.name.status}"
                        )
                        repository.updateVehicleHardwareState {
                            it.copy(
                                manufacturer = model.manufacturer.value,
                                modelName = model.name.value,
                                modelYear = model.year.value,
                                modelStatus = model.name.toHardwareStatus()
                            )
                        }
                    }
                } catch (e: Exception) {
                    AutoTelemetryLogger.log("MODEL_ERROR", "fetchModel failed: ${e.message}", isError = true)
                }

                try {
                    carInfo.fetchEnergyProfile(executor) { profile ->
                        val fuelNames = profile.fuelTypes.value?.map { type ->
                            when (type) {
                                EnergyProfile.FUEL_TYPE_CNG -> "CNG"
                                EnergyProfile.FUEL_TYPE_UNLEADED -> "Petrol (Unleaded)"
                                EnergyProfile.FUEL_TYPE_LEADED -> "Petrol (Leaded)"
                                EnergyProfile.FUEL_TYPE_DIESEL_1, EnergyProfile.FUEL_TYPE_DIESEL_2 -> "Diesel"
                                EnergyProfile.FUEL_TYPE_ELECTRIC -> "Electric"
                                EnergyProfile.FUEL_TYPE_LPG -> "LPG"
                                else -> "Type #$type"
                            }
                        } ?: emptyList()

                        AutoTelemetryLogger.log(
                            "PROFILE",
                            "FuelTypes: $fuelNames, Status: ${profile.fuelTypes.status}"
                        )
                        repository.updateVehicleHardwareState {
                            it.copy(
                                fuelTypes = fuelNames,
                                profileStatus = profile.fuelTypes.toHardwareStatus()
                            )
                        }
                    }
                } catch (e: Exception) {
                    AutoTelemetryLogger.log("PROFILE_ERROR", "fetchEnergyProfile failed: ${e.message}", isError = true)
                }

                // Dynamic Continuous Listeners
                try { carInfo.addMileageListener(executor, mileageListener) } catch (e: Exception) {
                    AutoTelemetryLogger.log("MILEAGE_ERR", "addMileageListener failed: ${e.message}", isError = true)
                }
                try { carInfo.addSpeedListener(executor, speedListener) } catch (e: Exception) {
                    AutoTelemetryLogger.log("SPEED_ERR", "addSpeedListener failed: ${e.message}", isError = true)
                }
                try { carInfo.addEnergyLevelListener(executor, energyListener) } catch (e: Exception) {
                    AutoTelemetryLogger.log("ENERGY_ERR", "addEnergyLevelListener failed: ${e.message}", isError = true)
                }
                try { carInfo.addTollListener(executor, tollListener) } catch (e: Exception) {
                    AutoTelemetryLogger.log("TOLL_ERR", "addTollListener failed: ${e.message}", isError = true)
                }
                try { carInfo.addEvStatusListener(executor, evStatusListener) } catch (e: Exception) {
                    AutoTelemetryLogger.log("EV_ERR", "addEvStatusListener failed: ${e.message}", isError = true)
                }
            }

            // Real-Time Sensor Listeners
            sensors?.let { carSensors ->
                try {
                    carSensors.addCompassListener(CarSensors.UPDATE_RATE_NORMAL, executor, compassListener)
                } catch (e: Exception) {
                    AutoTelemetryLogger.log("COMPASS_ERR", "addCompassListener failed: ${e.message}", isError = true)
                }
                try {
                    carSensors.addCarHardwareLocationListener(CarSensors.UPDATE_RATE_NORMAL, executor, locationListener)
                } catch (e: Exception) {
                    AutoTelemetryLogger.log("LOC_ERR", "addCarHardwareLocationListener failed: ${e.message}", isError = true)
                }
                try {
                    carSensors.addAccelerometerListener(CarSensors.UPDATE_RATE_NORMAL, executor, accelListener)
                } catch (e: Exception) {
                    AutoTelemetryLogger.log("ACCEL_ERR", "addAccelerometerListener failed: ${e.message}", isError = true)
                }
                try {
                    carSensors.addGyroscopeListener(CarSensors.UPDATE_RATE_NORMAL, executor, gyroListener)
                } catch (e: Exception) {
                    AutoTelemetryLogger.log("GYRO_ERR", "addGyroscopeListener failed: ${e.message}", isError = true)
                }
            }
        } catch (e: Exception) {
            AutoTelemetryLogger.log("HARDWARE_ERROR", "Failed to register car hardware: ${e.message}", isError = true)
        }
    }

    private fun teardownCarHardware() {
        try {
            carInfo?.let { info ->
                try { info.removeMileageListener(mileageListener) } catch (_: Exception) {}
                try { info.removeSpeedListener(speedListener) } catch (_: Exception) {}
                try { info.removeEnergyLevelListener(energyListener) } catch (_: Exception) {}
                try { info.removeTollListener(tollListener) } catch (_: Exception) {}
                try { info.removeEvStatusListener(evStatusListener) } catch (_: Exception) {}
            }
            carSensors?.let { sensors ->
                try { sensors.removeCompassListener(compassListener) } catch (_: Exception) {}
                try { sensors.removeCarHardwareLocationListener(locationListener) } catch (_: Exception) {}
                try { sensors.removeAccelerometerListener(accelListener) } catch (_: Exception) {}
                try { sensors.removeGyroscopeListener(gyroListener) } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    override fun onGetTemplate(): Template {
        val snapshot = currentSnapshot
        val currentOdo = snapshot?.odometerKm ?: 0.0
        val currentSpeed = snapshot?.speedKmh ?: 0.0
        val petrolPct = snapshot?.petrolPercent

        val cngEmptyAction = Action.Builder()
            .setTitle("CNG Ran Out")
            .setBackgroundColor(CarColor.createCustom(0xFFD97706.toInt(), 0xFFD97706.toInt()))
            .setOnClickListener {
                scope.launch {
                    val targetOdo = if (currentOdo > 0.0) currentOdo else repository.getLatestActualOdometer()
                    repository.addEvent(
                        FuelEvent(
                            odometerKm = targetOdo,
                            source = EventSource.ANDROID_AUTO,
                            type = EventType.CNG_EMPTY,
                            confirmedByUser = true
                        )
                    )
                    CarToast.makeText(
                        carContext,
                        "CNG Exhaust logged at ${String.format(Locale.US, "%,.0f", targetOdo)} km",
                        CarToast.LENGTH_SHORT
                    ).show()
                }
            }
            .build()

        val actionStrip = ActionStrip.Builder()
            .addAction(cngEmptyAction)
            .build()

        val speedText = if (currentSpeed > 0.0) "${String.format(Locale.US, "%.0f", currentSpeed)} km/h" else "0 km/h"
        val fuelSummary = if (petrolPct != null) "Petrol: ${String.format(Locale.US, "%.0f", petrolPct)}%" else "Petrol Level: Connected"

        val pane = Pane.Builder()
            .addRow(
                Row.Builder()
                    .setTitle("Grand Vitara • Live Telemetry")
                    .addText("Speed: $speedText • $fuelSummary")
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("Odometer: ${String.format(Locale.US, "%,.0f", currentOdo)} km")
                    .addText("Sync: ${if (snapshot?.isConnectedToAuto == true) "Live Stream Active" else "Connected"}")
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("1-Tap Action Ready")
                    .addText("Tap 'CNG Ran Out' when vehicle auto-switches to petrol.")
                    .build()
            )
            .build()

        return PaneTemplate.Builder(pane)
            .setTitle("Vehicle Telemetry Auto")
            .setActionStrip(actionStrip)
            .build()
    }
}

private fun <T> CarValue<T>?.toHardwareStatus(): HardwareStatus {
    return when (this?.status) {
        CarValue.STATUS_SUCCESS -> HardwareStatus.SUCCESS
        CarValue.STATUS_UNIMPLEMENTED -> HardwareStatus.UNSUPPORTED_BY_CAR
        CarValue.STATUS_UNAVAILABLE -> HardwareStatus.UNAVAILABLE
        else -> HardwareStatus.AWAITING
    }
}
