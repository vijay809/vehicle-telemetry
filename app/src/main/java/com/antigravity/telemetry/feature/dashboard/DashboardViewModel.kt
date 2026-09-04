package com.antigravity.telemetry.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.telemetry.core.engine.BlendedCostResult
import com.antigravity.telemetry.core.engine.CalculationEngines
import com.antigravity.telemetry.core.engine.CngEfficiencyResult
import com.antigravity.telemetry.core.engine.PetrolEfficiencyResult
import com.antigravity.telemetry.core.model.EventType
import com.antigravity.telemetry.core.model.FuelEvent
import com.antigravity.telemetry.core.model.TelemetrySnapshot
import com.antigravity.telemetry.core.model.VehicleMeta
import com.antigravity.telemetry.core.repository.TelemetryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val vehicle: VehicleMeta = VehicleMeta(),
    val telemetry: TelemetrySnapshot = TelemetrySnapshot(),
    val blendedCost: BlendedCostResult = BlendedCostResult(
        blendedCostPerKm = 0.0,
        totalCost = 0.0,
        totalDistanceKm = 0.0,
        cngSpend = 0.0,
        petrolSpend = 0.0,
        cngSharePercent = 0.0,
        petrolSharePercent = 0.0,
        cngCostPerKm = 0.0,
        petrolCostPerKm = 0.0,
        monthlySavingsVsPetrol = 0.0
    ),
    val cngEfficiency: CngEfficiencyResult = CngEfficiencyResult(
        latestMileageKmPerKg = 0.0,
        rawDistanceKm = 0.0,
        coldStartDeductionKm = 0.0,
        netCngDistanceKm = 0.0,
        currentTripKm = 0.0,
        isCngExhausted = false,
        exhaustedAtOdometerKm = null
    ),
    val petrolEfficiency: PetrolEfficiencyResult = PetrolEfficiencyResult(
        latestMileageKmPerL = 0.0,
        residualDistanceKm = 0.0,
        estimatedRangeKm = 0.0
    ),
    val recentFill: FuelEvent? = null,
    val lastCngRefill: FuelEvent? = null,
    val lastPetrolRefill: FuelEvent? = null,
    val isSimulationMode: Boolean = false,
    val isLowFuelPetrolMarked: Boolean = false,
    val lowFuelPetrolOdoKm: Double? = null
) {
    val isCngInUse: Boolean
        get() = !cngEfficiency.isCngExhausted
}

class DashboardViewModel(private val repository: TelemetryRepository) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.vehicleFlow,
        repository.telemetryState,
        repository.eventsFlow,
        repository.isSimulationMode
    ) { vehicle, telemetry, events, isSim ->
        val activeVehicle = vehicle ?: VehicleMeta()
        val currentOdo = telemetry.odometerKm

        val blended = CalculationEngines.calculateBlendedCost(events, currentOdo)
        val cng = CalculationEngines.calculateCngEfficiency(events, activeVehicle, currentOdo)
        val pet = CalculationEngines.calculateResidualPetrolEfficiency(events, currentOdo, telemetry.petrolPercent ?: 0.0, activeVehicle)
        val recent = events.firstOrNull { it.type == EventType.REFILL }
        val lastCng = events.firstOrNull { it.type == EventType.REFILL && it.fuelType == com.antigravity.telemetry.core.model.FuelType.CNG }
        val lastPet = events.firstOrNull { it.type == EventType.REFILL && it.fuelType == com.antigravity.telemetry.core.model.FuelType.PETROL }

        val lastPetrolRefillEvent = events.filter { it.type == EventType.REFILL && it.fuelType == com.antigravity.telemetry.core.model.FuelType.PETROL }
            .maxWithOrNull(compareBy<FuelEvent> { it.odometerKm }.thenBy { it.timestamp })
        val lastPetrolLowFuel = events.filter { it.type == EventType.FUEL_LOW && it.fuelType == com.antigravity.telemetry.core.model.FuelType.PETROL }
            .maxWithOrNull(compareBy<FuelEvent> { it.odometerKm }.thenBy { it.timestamp })

        val isLowFuel = when {
            lastPetrolLowFuel == null -> false
            lastPetrolRefillEvent == null -> true
            else -> lastPetrolLowFuel.timestamp > lastPetrolRefillEvent.timestamp && lastPetrolLowFuel.odometerKm >= lastPetrolRefillEvent.odometerKm
        }
        val lowFuelOdo = if (isLowFuel) lastPetrolLowFuel?.odometerKm else null

        DashboardUiState(
            vehicle = activeVehicle,
            telemetry = telemetry,
            blendedCost = blended,
            cngEfficiency = cng,
            petrolEfficiency = pet,
            recentFill = recent,
            lastCngRefill = lastCng,
            lastPetrolRefill = lastPet,
            isSimulationMode = isSim,
            isLowFuelPetrolMarked = isLowFuel,
            lowFuelPetrolOdoKm = lowFuelOdo
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun markPetrolLowFuel(odometerKm: Double) {
        viewModelScope.launch {
            val event = FuelEvent(
                odometerKm = odometerKm,
                source = com.antigravity.telemetry.core.model.EventSource.MANUAL,
                type = EventType.FUEL_LOW,
                fuelType = com.antigravity.telemetry.core.model.FuelType.PETROL,
                confirmedByUser = true,
                isSimulation = repository.isSimulationMode.value
            )
            repository.addEvent(event)
        }
    }
}
