package com.antigravity.telemetry.feature.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.telemetry.core.engine.CalculationEngines
import com.antigravity.telemetry.core.model.EventType
import com.antigravity.telemetry.core.model.FuelEvent
import com.antigravity.telemetry.core.model.FuelType
import com.antigravity.telemetry.core.repository.TelemetryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

enum class LedgerFilter {
    ALL,
    CNG,
    PETROL,
    ODOMETER,
    SWITCH
}

data class FuelLedgerUiState(
    val selectedFilter: LedgerFilter = LedgerFilter.ALL,
    val allEvents: List<FuelEvent> = emptyList(),
    val filteredEvents: List<FuelEvent> = emptyList(),
    val cngCount: Int = 0,
    val petrolCount: Int = 0,
    val odometerCount: Int = 0,
    val switchCount: Int = 0,
    val cngRatioPercent: Double = 0.0,
    val petrolRatioPercent: Double = 0.0,
    val netRunningCost: Double = 0.0,
    val odometerKm: Double = 0.0,
    val isConnectedToAuto: Boolean = false,
    val vehicleName: String = "Victoris CNG"
)

class FuelLedgerViewModel(private val repository: TelemetryRepository) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(LedgerFilter.ALL)

    val uiState: StateFlow<FuelLedgerUiState> = combine(
        _selectedFilter,
        repository.eventsFlow,
        repository.telemetryState
    ) { filter, rawEvents, telemetry ->
        // Sort strictly by odometerKm DESC, then timestamp DESC
        val events = rawEvents.sortedWith(
            compareByDescending<FuelEvent> { it.odometerKm }.thenByDescending { it.timestamp }
        )

        val cngs = events.filter { it.isCngRefill || it.isCngEmpty }
        val pets = events.filter { it.isPetrolRefill || it.isPetrolReserve }
        val odos = events.filter { it.isOdometerUpdate }
        val switches = events.filter { it.isManualFuelSwitch || it.isCngEmpty }

        val filtered = when (filter) {
            LedgerFilter.ALL -> events
            LedgerFilter.CNG -> cngs
            LedgerFilter.PETROL -> pets
            LedgerFilter.ODOMETER -> odos
            LedgerFilter.SWITCH -> switches
        }

        val blended = CalculationEngines.calculateBlendedCost(events, telemetry.odometerKm)

        FuelLedgerUiState(
            selectedFilter = filter,
            allEvents = events,
            filteredEvents = filtered,
            cngCount = cngs.size,
            petrolCount = pets.size,
            odometerCount = odos.size,
            switchCount = switches.size,
            cngRatioPercent = if (blended.totalCost > 0) blended.cngSharePercent else 0.0,
            petrolRatioPercent = if (blended.totalCost > 0) blended.petrolSharePercent else 0.0,
            netRunningCost = if (blended.blendedCostPerKm > 0) blended.blendedCostPerKm else 0.0,
            odometerKm = telemetry.odometerKm,
            isConnectedToAuto = telemetry.isConnectedToAuto,
            vehicleName = "Victoris CNG"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FuelLedgerUiState()
    )

    fun setFilter(filter: LedgerFilter) {
        _selectedFilter.value = filter
    }

    fun deleteEvent(id: String) {
        viewModelScope.launch {
            repository.deleteEvent(id)
        }
    }

    fun logOdometerUpdate(odometerKm: Double) {
        viewModelScope.launch {
            repository.logOdometerUpdate(odometerKm)
        }
    }

    fun logManualFuelSwitch(targetFuel: FuelType, odometerKm: Double) {
        viewModelScope.launch {
            repository.logManualFuelSwitch(targetFuel, odometerKm)
        }
    }
}
