package com.antigravity.telemetry.feature.cngempty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.telemetry.core.model.EventSource
import com.antigravity.telemetry.core.model.EventType
import com.antigravity.telemetry.core.model.FuelEvent
import com.antigravity.telemetry.core.model.FuelType
import com.antigravity.telemetry.core.repository.TelemetryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CngEmptyUiState(
    val odometerKm: Double = 0.0,
    val cngTripKm: Double = 0.0,
    val coldStartsCount: Int = 0,
    val warmupCoeffKm: Double = 1.2,
    val cngPressureBar: Double = 0.0,
    val petrolReservePercent: Double = 0.0,
    val isConfirmed: Boolean = false
) {
    val deductionKm: Double
        get() = coldStartsCount * warmupCoeffKm
}

class CngEmptyViewModel(private val repository: TelemetryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CngEmptyUiState())
    val uiState: StateFlow<CngEmptyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val telemetry = repository.telemetryState.firstOrNull()
            val lastCngRefill = repository.getLastRefillEvent(FuelType.CNG)

            if (telemetry != null) {
                val trip = if (lastCngRefill != null) {
                    (telemetry.odometerKm - lastCngRefill.odometerKm).coerceAtLeast(0.0)
                } else 0.0

                _uiState.update {
                    it.copy(
                        odometerKm = telemetry.odometerKm,
                        cngTripKm = trip,
                        cngPressureBar = telemetry.cngPressureBar?.coerceAtMost(1.0) ?: 0.0,
                        petrolReservePercent = telemetry.petrolPercent ?: 0.0
                    )
                }
            }
        }
    }

    fun incrementColdStarts() {
        _uiState.update { it.copy(coldStartsCount = it.coldStartsCount + 1) }
    }

    fun decrementColdStarts() {
        _uiState.update { it.copy(coldStartsCount = maxOf(0, it.coldStartsCount - 1)) }
    }

    fun confirmCngEmpty(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val current = _uiState.value
            val event = FuelEvent(
                odometerKm = current.odometerKm,
                source = EventSource.ANDROID_AUTO,
                type = EventType.CNG_EMPTY,
                fuelType = null,
                coldStartsSinceLastRefill = current.coldStartsCount,
                confirmedByUser = true,
                isSimulation = repository.isSimulationMode.value
            )
            repository.addEvent(event)
            _uiState.update { it.copy(isConfirmed = true) }
            onSuccess()
        }
    }
}
