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
    SWITCHOVER
}

data class FuelLedgerUiState(
    val selectedFilter: LedgerFilter = LedgerFilter.ALL,
    val allEvents: List<FuelEvent> = emptyList(),
    val filteredEvents: List<FuelEvent> = emptyList(),
    val cngCount: Int = 0,
    val petrolCount: Int = 0,
    val switchCount: Int = 0,
    val cngRatioPercent: Double = 0.0,
    val petrolRatioPercent: Double = 0.0,
    val netRunningCost: Double = 0.0,
    val odometerKm: Double = 0.0
)

class FuelLedgerViewModel(private val repository: TelemetryRepository) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(LedgerFilter.ALL)

    val uiState: StateFlow<FuelLedgerUiState> = combine(
        _selectedFilter,
        repository.eventsFlow,
        repository.telemetryState
    ) { filter, events, telemetry ->
        val cngs = events.filter { it.type == EventType.REFILL && it.fuelType == FuelType.CNG }
        val pets = events.filter { it.type == EventType.REFILL && it.fuelType == FuelType.PETROL }
        val switches = events.filter { it.type == EventType.CNG_EMPTY }

        val filtered = when (filter) {
            LedgerFilter.ALL -> events
            LedgerFilter.CNG -> cngs
            LedgerFilter.PETROL -> pets
            LedgerFilter.SWITCHOVER -> switches
        }

        val blended = CalculationEngines.calculateBlendedCost(events, telemetry.odometerKm)

        FuelLedgerUiState(
            selectedFilter = filter,
            allEvents = events,
            filteredEvents = filtered,
            cngCount = cngs.size,
            petrolCount = pets.size,
            switchCount = switches.size,
            cngRatioPercent = if (blended.totalCost > 0) blended.cngSharePercent else 0.0,
            petrolRatioPercent = if (blended.totalCost > 0) blended.petrolSharePercent else 0.0,
            netRunningCost = if (blended.blendedCostPerKm > 0) blended.blendedCostPerKm else 0.0,
            odometerKm = telemetry.odometerKm
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
}
