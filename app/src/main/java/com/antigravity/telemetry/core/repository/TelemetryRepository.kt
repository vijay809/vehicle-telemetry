package com.antigravity.telemetry.core.repository

import com.antigravity.telemetry.core.database.AppDatabase
import com.antigravity.telemetry.core.database.FuelEventEntity
import com.antigravity.telemetry.core.database.VehicleEntity
import com.antigravity.telemetry.core.model.FuelEvent
import com.antigravity.telemetry.core.model.FuelType
import com.antigravity.telemetry.core.model.TelemetrySnapshot
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

    // Clean actual telemetry state - no guesswork, nulls for unmeasured sensors
    private val _actualTelemetryState = MutableStateFlow(
        TelemetrySnapshot(
            odometerKm = 0.0,
            speedKmh = 0.0,
            petrolPercent = null,
            cngPressureBar = null,
            isAutoModeActive = false,
            isLowFuelWarning = false,
            isConnectedToAuto = false,
            isSimulation = false
        )
    )

    // Isolated simulated telemetry state
    private val _simulatedTelemetryState = MutableStateFlow(
        TelemetrySnapshot(
            odometerKm = 42850.0,
            speedKmh = 45.0,
            petrolPercent = 70.0,
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
            val bestActualOdo = maxOf(vehicle?.activeOdometerKm ?: 0.0, actualEvents.maxOfOrNull { it.odometerKm } ?: 0.0)
            if (bestActualOdo > 0.0) {
                _actualTelemetryState.value = _actualTelemetryState.value.copy(odometerKm = bestActualOdo)
            }

            val simEvents = database.fuelEventDao().getEventsAscSync(true)
            val bestSimOdo = maxOf(42850.0, simEvents.maxOfOrNull { it.odometerKm } ?: 0.0)
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
