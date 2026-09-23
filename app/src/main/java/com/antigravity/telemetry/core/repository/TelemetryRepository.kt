package com.antigravity.telemetry.core.repository

import com.antigravity.telemetry.core.database.AppDatabase
import com.antigravity.telemetry.core.database.FuelEventEntity
import com.antigravity.telemetry.core.database.VehicleEntity
import com.antigravity.telemetry.core.model.FuelEvent
import com.antigravity.telemetry.core.model.FuelType
import com.antigravity.telemetry.core.model.HardwareStatus
import com.antigravity.telemetry.core.model.TelemetrySnapshot
import com.antigravity.telemetry.core.model.VehicleHardwareState
import com.antigravity.telemetry.core.model.VehicleMeta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TelemetryRepository(
    private val database: AppDatabase,
    private val preferences: FuelPreferences? = null
) {

    private val _isSimulationMode = MutableStateFlow(preferences?.getSimulationMode() ?: false)
    val isSimulationMode: StateFlow<Boolean> = _isSimulationMode.asStateFlow()

    private val _vehicleHardwareState = MutableStateFlow(VehicleHardwareState())
    val vehicleHardwareState: StateFlow<VehicleHardwareState> = _vehicleHardwareState.asStateFlow()

    private val _autoConnectionCount = MutableStateFlow(preferences?.getAutoConnectionCount() ?: 0)
    val autoConnectionCount: StateFlow<Int> = _autoConnectionCount.asStateFlow()

    private val _lastAutoConnectedTime = MutableStateFlow(preferences?.getLastAutoConnectedTimestamp() ?: 0L)
    val lastAutoConnectedTime: StateFlow<Long> = _lastAutoConnectedTime.asStateFlow()

    fun resetAutoConnectionCounter() {
        preferences?.resetAutoConnectionCount()
        _autoConnectionCount.value = 0
    }


    // Clean actual telemetry state - initialized with user's calibrated cluster values
    private val _actualTelemetryState = MutableStateFlow(
        TelemetrySnapshot(
            odometerKm = preferences?.getCalibratedOdometer() ?: 9284.0,
            speedKmh = 0.0,
            petrolPercent = preferences?.getCalibratedFuelPercent() ?: 25.0,
            cngPressureBar = null,
            isAutoModeActive = false,
            isLowFuelWarning = (preferences?.getCalibratedFuelPercent() ?: 25.0) <= 15.0,
            isConnectedToAuto = false,
            isSimulation = false
        )
    )

    // Isolated simulated telemetry state
    private val _simulatedTelemetryState = MutableStateFlow(
        TelemetrySnapshot(
            odometerKm = 9284.0,
            speedKmh = 45.0,
            petrolPercent = 25.0,
            cngPressureBar = 32.0,
            isAutoModeActive = true,
            isLowFuelWarning = false,
            isConnectedToAuto = true,
            isSimulation = true
        )
    )

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val vehicle = database.vehicleDao().getVehicleSync("default-vehicle-victoris")
            val actualEvents = database.fuelEventDao().getEventsAscSync(false)
            val calibratedOdo = preferences?.getCalibratedOdometer() ?: 9284.0
            val calibratedFuel = preferences?.getCalibratedFuelPercent() ?: 25.0

            // If stored odo in DB is legacy simulation baseline (> 40000 km) or 0.0, overwrite with cluster reading
            val effectiveOdo = if (vehicle == null || vehicle.activeOdometerKm > 40000.0 || vehicle.activeOdometerKm == 0.0) {
                database.vehicleDao().updateOdometer("default-vehicle-victoris", calibratedOdo)
                calibratedOdo
            } else {
                maxOf(calibratedOdo, vehicle.activeOdometerKm, actualEvents.filter { it.odometerKm < 40000.0 }.maxOfOrNull { it.odometerKm } ?: 0.0)
            }

            _actualTelemetryState.value = _actualTelemetryState.value.copy(
                odometerKm = effectiveOdo,
                petrolPercent = calibratedFuel,
                isLowFuelWarning = calibratedFuel <= 15.0
            )
            updateVehicleHardwareState {
                it.copy(
                    odometerKm = effectiveOdo,
                    fuelPercent = calibratedFuel,
                    isLowFuel = calibratedFuel <= 15.0,
                    manufacturer = "Maruti Suzuki",
                    modelName = "Victoris CNG",
                    modelYear = 2024,
                    fuelTypes = listOf("CNG", "Petrol"),
                    mileageStatus = HardwareStatus.SUCCESS,
                    energyStatus = HardwareStatus.SUCCESS,
                    modelStatus = HardwareStatus.SUCCESS,
                    profileStatus = HardwareStatus.SUCCESS,
                    rangeRemainingKm = (calibratedFuel * 6.5) + 180.0,
                    tollCardState = "FASTAG ACTIVE",
                    tollStatus = HardwareStatus.SUCCESS
                )
            }

            val simEvents = database.fuelEventDao().getEventsAscSync(true)
            val bestSimOdo = maxOf(9284.0, simEvents.maxOfOrNull { it.odometerKm } ?: 0.0)
            _simulatedTelemetryState.value = _simulatedTelemetryState.value.copy(odometerKm = bestSimOdo)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val telemetryState: Flow<TelemetrySnapshot> = _isSimulationMode.flatMapLatest { isSim ->
        if (isSim) _simulatedTelemetryState else _actualTelemetryState
    }

    val vehicleFlow: Flow<VehicleMeta?> = database.vehicleDao().getVehicle("default-vehicle-victoris").map { entity ->
        entity?.let {
            VehicleMeta(
                id = it.id,
                name = it.name,
                cngTankCapacityKg = it.cngTankCapacityKg,
                petrolTankCapacityL = it.petrolTankCapacityL,
                estimatedWarmupDistanceKmPerColdStart = it.estimatedWarmupDistanceKmPerColdStart,
                activeOdometerKm = it.activeOdometerKm
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsFlow: Flow<List<FuelEvent>> = _isSimulationMode.flatMapLatest { isSim ->
        database.fuelEventDao().getEventsFlow(isSimulation = isSim).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun setSimulationMode(enabled: Boolean) {
        _isSimulationMode.value = enabled
        preferences?.setSimulationMode(enabled)
    }

    suspend fun getEventsAsc(isSimulation: Boolean = _isSimulationMode.value): List<FuelEvent> {
        return database.fuelEventDao().getEventsAscSync(isSimulation).map { it.toDomain() }
    }

    suspend fun addEvent(event: FuelEvent) {
        database.fuelEventDao().insertEvent(event.toEntity())
        if (event.isSimulation) {
            _simulatedTelemetryState.value = _simulatedTelemetryState.value.copy(odometerKm = event.odometerKm)
        } else {
            database.vehicleDao().updateOdometer(event.vehicleId, event.odometerKm)
            _actualTelemetryState.value = _actualTelemetryState.value.copy(odometerKm = event.odometerKm)
        }
    }

    suspend fun deleteEvent(id: String) {
        database.fuelEventDao().deleteEvent(id)
    }

    suspend fun resetActualData() {
        AppDatabase.resetAllActualData(database)
        _actualTelemetryState.value = TelemetrySnapshot(
            odometerKm = 0.0,
            speedKmh = 0.0,
            petrolPercent = null,
            cngPressureBar = null,
            isAutoModeActive = false,
            isLowFuelWarning = false,
            isConnectedToAuto = false,
            isSimulation = false
        )
    }

    suspend fun updateVehicle(vehicle: VehicleMeta) {
        database.vehicleDao().upsertVehicle(
            VehicleEntity(
                id = vehicle.id,
                name = vehicle.name,
                cngTankCapacityKg = vehicle.cngTankCapacityKg,
                petrolTankCapacityL = vehicle.petrolTankCapacityL,
                estimatedWarmupDistanceKmPerColdStart = vehicle.estimatedWarmupDistanceKmPerColdStart,
                activeOdometerKm = vehicle.activeOdometerKm
            )
        )
    }

    suspend fun updateOdometer(odometerKm: Double) {
        if (_isSimulationMode.value) {
            _simulatedTelemetryState.value = _simulatedTelemetryState.value.copy(odometerKm = odometerKm)
        } else {
            database.vehicleDao().updateOdometer("default-vehicle-victoris", odometerKm)
            _actualTelemetryState.value = _actualTelemetryState.value.copy(odometerKm = odometerKm)
        }
    }

    fun updateActualTelemetry(snapshot: TelemetrySnapshot) {
        _actualTelemetryState.value = snapshot.copy(isSimulation = false)
    }

    fun updateVehicleHardwareState(transform: (VehicleHardwareState) -> VehicleHardwareState) {
        _vehicleHardwareState.value = transform(_vehicleHardwareState.value).copy(
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }

    fun updateActualOdometer(odometerKm: Double) {
        if (odometerKm <= 0.0) return
        _actualTelemetryState.value = _actualTelemetryState.value.copy(odometerKm = odometerKm)
        _vehicleHardwareState.value = _vehicleHardwareState.value.copy(
            odometerKm = odometerKm,
            mileageStatus = HardwareStatus.SUCCESS,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        CoroutineScope(Dispatchers.IO).launch {
            database.vehicleDao().updateOdometer("default-vehicle-victoris", odometerKm)
        }
    }

    fun updateActualSpeed(speedKmh: Double) {
        _actualTelemetryState.value = _actualTelemetryState.value.copy(speedKmh = speedKmh)
        _vehicleHardwareState.value = _vehicleHardwareState.value.copy(
            speedKmh = speedKmh,
            speedStatus = HardwareStatus.SUCCESS,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }

    fun updateActualFuel(fuelPercent: Double, isLowFuel: Boolean? = null) {
        _actualTelemetryState.value = _actualTelemetryState.value.copy(
            petrolPercent = fuelPercent,
            isLowFuelWarning = isLowFuel ?: _actualTelemetryState.value.isLowFuelWarning
        )
        _vehicleHardwareState.value = _vehicleHardwareState.value.copy(
            fuelPercent = fuelPercent,
            isLowFuel = isLowFuel ?: _vehicleHardwareState.value.isLowFuel,
            energyStatus = HardwareStatus.SUCCESS,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }

    fun updateActualConnection(isConnected: Boolean) {
        val wasConnected = _actualTelemetryState.value.isConnectedToAuto
        _actualTelemetryState.value = _actualTelemetryState.value.copy(
            isConnectedToAuto = isConnected,
            isAutoModeActive = isConnected
        )
        _vehicleHardwareState.value = _vehicleHardwareState.value.copy(
            isConnected = isConnected,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        if (isConnected && !wasConnected) {
            val newCount = preferences?.incrementAutoConnectionCount() ?: (_autoConnectionCount.value + 1)
            val now = System.currentTimeMillis()
            _autoConnectionCount.value = newCount
            _lastAutoConnectedTime.value = now
            com.antigravity.telemetry.core.telemetry.AutoTelemetryLogger.log(
                "AUTO_CONNECT",
                "Android Auto projection connected! Total connections: $newCount"
            )
        } else if (!isConnected && wasConnected) {
            com.antigravity.telemetry.core.telemetry.AutoTelemetryLogger.log(
                "AUTO_CONNECT",
                "Android Auto projection disconnected."
            )
        }
    }

    fun getLatestActualOdometer(): Double = _actualTelemetryState.value.odometerKm

    suspend fun calibrateCluster(odometerKm: Double, fuelPercent: Double) {
        preferences?.setCalibratedOdometer(odometerKm)
        preferences?.setCalibratedFuelPercent(fuelPercent)
        database.vehicleDao().updateOdometer("default-vehicle-victoris", odometerKm)
        _actualTelemetryState.value = _actualTelemetryState.value.copy(
            odometerKm = odometerKm,
            petrolPercent = fuelPercent,
            isLowFuelWarning = fuelPercent <= 15.0
        )
        updateVehicleHardwareState {
            it.copy(
                odometerKm = odometerKm,
                fuelPercent = fuelPercent,
                isLowFuel = fuelPercent <= 15.0,
                rangeRemainingKm = (fuelPercent * 6.5) + 180.0,
                mileageStatus = HardwareStatus.SUCCESS,
                energyStatus = HardwareStatus.SUCCESS,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun updateSimulatedTelemetry(snapshot: TelemetrySnapshot) {
        _simulatedTelemetryState.value = snapshot.copy(isSimulation = true)
    }

    suspend fun getLastCngEmptyEvent(isSimulation: Boolean = _isSimulationMode.value): FuelEvent? {
        return database.fuelEventDao().getLastCngEmptyEvent("default-vehicle-victoris", isSimulation)?.toDomain()
    }

    suspend fun getLastRefillEvent(fuelType: FuelType, isSimulation: Boolean = _isSimulationMode.value): FuelEvent? {
        return database.fuelEventDao().getLastRefillEvent("default-vehicle-victoris", fuelType, isSimulation)?.toDomain()
    }

    private fun FuelEventEntity.toDomain(): FuelEvent = FuelEvent(
        id = id,
        vehicleId = vehicleId,
        timestamp = timestamp,
        odometerKm = odometerKm,
        source = source,
        type = type,
        fuelType = fuelType,
        quantity = quantity,
        pricePerUnit = pricePerUnit,
        totalCost = totalCost,
        isFullTank = isFullTank,
        fuelLevelPercent = fuelLevelPercent,
        coldStartsSinceLastRefill = coldStartsSinceLastRefill,
        confirmedByUser = confirmedByUser,
        stationName = stationName,
        isSimulation = isSimulation
    )

    private fun FuelEvent.toEntity(): FuelEventEntity = FuelEventEntity(
        id = id,
        vehicleId = vehicleId,
        timestamp = timestamp,
        odometerKm = odometerKm,
        source = source,
        type = type,
        fuelType = fuelType,
        quantity = quantity,
        pricePerUnit = pricePerUnit,
        totalCost = totalCost,
        isFullTank = isFullTank,
        fuelLevelPercent = fuelLevelPercent,
        coldStartsSinceLastRefill = coldStartsSinceLastRefill,
        confirmedByUser = confirmedByUser,
        stationName = stationName,
        isSimulation = isSimulation
    )
}
