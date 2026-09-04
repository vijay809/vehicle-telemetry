package com.antigravity.telemetry.feature.refill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.telemetry.core.model.EventSource
import com.antigravity.telemetry.core.model.EventType
import com.antigravity.telemetry.core.model.FuelEvent
import com.antigravity.telemetry.core.model.FuelType
import com.antigravity.telemetry.core.repository.FuelPreferences
import com.antigravity.telemetry.core.repository.TelemetryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max

data class RefillUiState(
    val fuelType: FuelType = FuelType.CNG,
    val odometerKm: Double = 0.0,
    val quantityText: String = "",
    val totalCostText: String = "",
    val unitRateText: String = "",
    val unitRate: Double = 89.0,
    val isFullTank: Boolean = true,
    val stationName: String = "",
    val isSaved: Boolean = false
) {
    val quantity: Double
        get() = quantityText.toDoubleOrNull() ?: 0.0

    val totalCost: Double
        get() = totalCostText.toDoubleOrNull() ?: 0.0
}

class RefillViewModel(
    private val repository: TelemetryRepository,
    private val preferences: FuelPreferences
) : ViewModel() {

    private val initialFuelType = FuelType.CNG
    private var isQuantityUserEdited = false
    private var isCostUserEdited = false

    private val _uiState = MutableStateFlow(
        run {
            val rate = preferences.getUnitRate(initialFuelType)
            RefillUiState(
                fuelType = initialFuelType,
                unitRate = rate,
                unitRateText = formatRate(rate),
                isFullTank = preferences.getAutoCut(initialFuelType)
            )
        }
    )
    val uiState: StateFlow<RefillUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val telemetry = repository.telemetryState.firstOrNull()
            if (telemetry != null) {
                _uiState.update { it.copy(odometerKm = telemetry.odometerKm) }
            }
        }
    }

    fun setFuelType(type: FuelType) {
        val rememberedRate = preferences.getUnitRate(type)
        val rememberedAutoCut = preferences.getAutoCut(type)
        isQuantityUserEdited = false
        isCostUserEdited = false

        _uiState.update { current ->
            val qty = current.quantityText.toDoubleOrNull() ?: 0.0
            val newCost = if (qty > 0) formatCost(qty * rememberedRate) else current.totalCostText

            current.copy(
                fuelType = type,
                unitRate = rememberedRate,
                unitRateText = formatRate(rememberedRate),
                isFullTank = rememberedAutoCut,
                totalCostText = newCost
            )
        }
    }

    /**
     * Rule 1: On dispensed change, update total cost.
     * Rule 4: On dispense and cost both change, update rate.
     */
    fun onQuantityChange(newInput: String) {
        if (newInput.isNotEmpty() && !newInput.matches(Regex("^\\d*\\.?\\d*$"))) {
            return
        }

        if (newInput.isBlank()) {
            isQuantityUserEdited = false
            _uiState.update { current ->
                current.copy(
                    quantityText = newInput,
                    totalCostText = if (!isCostUserEdited) "" else current.totalCostText
                )
            }
            return
        }

        isQuantityUserEdited = true
        val qty = newInput.toDoubleOrNull()

        _uiState.update { current ->
            if (isCostUserEdited && qty != null && qty > 0) {
                val cost = current.totalCostText.toDoubleOrNull()
                if (cost != null && cost > 0) {
                    // Rule 4: Both changed -> update rate
                    val newRate = cost / qty
                    preferences.setUnitRate(current.fuelType, newRate)
                    current.copy(
                        quantityText = newInput,
                        unitRate = newRate,
                        unitRateText = formatRate(newRate)
                    )
                } else {
                    current.copy(quantityText = newInput)
                }
            } else {
                // Rule 1: Update total cost
                val newCost = if (qty != null && qty > 0) formatCost(qty * current.unitRate) else ""
                current.copy(
                    quantityText = newInput,
                    totalCostText = newCost
                )
            }
        }
    }

    /**
     * Rule 2: On cost change, update dispensed.
     * Rule 4: On dispense and cost both change, update rate.
     */
    fun onTotalCostChange(newInput: String) {
        if (newInput.isNotEmpty() && !newInput.matches(Regex("^\\d*\\.?\\d*$"))) {
            return
        }

        if (newInput.isBlank()) {
            isCostUserEdited = false
            _uiState.update { current ->
                current.copy(
                    totalCostText = newInput,
                    quantityText = if (!isQuantityUserEdited) "" else current.quantityText
                )
            }
            return
        }

        isCostUserEdited = true
        val cost = newInput.toDoubleOrNull()

        _uiState.update { current ->
            if (isQuantityUserEdited && cost != null && cost > 0) {
                val qty = current.quantityText.toDoubleOrNull()
                if (qty != null && qty > 0) {
                    // Rule 4: Both changed -> update rate
                    val newRate = cost / qty
                    preferences.setUnitRate(current.fuelType, newRate)
                    current.copy(
                        totalCostText = newInput,
                        unitRate = newRate,
                        unitRateText = formatRate(newRate)
                    )
                } else {
                    current.copy(totalCostText = newInput)
                }
            } else {
                // Rule 2: Update dispensed
                val newQty = if (cost != null && cost > 0 && current.unitRate > 0) {
                    formatQty(cost / current.unitRate)
                } else {
                    ""
                }
                current.copy(
                    totalCostText = newInput,
                    quantityText = newQty
                )
            }
        }
    }

    /**
     * Rule 3: On rate change, update total cost.
     */
    fun onRateChange(newInput: String) {
        if (newInput.isNotEmpty() && !newInput.matches(Regex("^\\d*\\.?\\d*$"))) {
            return
        }

        isCostUserEdited = false // Reset cost edited anchor so subsequent changes respect new rate

        val rate = newInput.toDoubleOrNull()

        _uiState.update { current ->
            if (rate != null && rate > 0) {
                preferences.setUnitRate(current.fuelType, rate)
                val qty = current.quantityText.toDoubleOrNull()
                val newCost = if (qty != null && qty > 0) formatCost(qty * rate) else current.totalCostText
                current.copy(
                    unitRateText = newInput,
                    unitRate = rate,
                    totalCostText = newCost
                )
            } else {
                current.copy(unitRateText = newInput)
            }
        }
    }

    fun stepRate(delta: Double) {
        val currentRate = _uiState.value.unitRate
        val nextRate = max(1.0, currentRate + delta)
        onRateChange(formatRate(nextRate))
    }

    fun stepQuantity(delta: Double) {
        val currentQty = _uiState.value.quantityText.toDoubleOrNull() ?: 0.0
        val nextQty = max(0.0, currentQty + delta)
        val qtyStr = if (nextQty > 0) formatQty(nextQty) else ""
        onQuantityChange(qtyStr)
    }

    fun addCost(amount: Double) {
        val currentCost = _uiState.value.totalCostText.toDoubleOrNull() ?: 0.0
        val nextCost = currentCost + amount
        val costStr = formatCost(nextCost)
        onTotalCostChange(costStr)
    }

    fun setOdometer(odo: Double) {
        _uiState.update { it.copy(odometerKm = odo) }
    }

    fun setStationName(name: String) {
        _uiState.update { it.copy(stationName = name) }
    }

    fun toggleFullTank() {
        _uiState.update { current ->
            val nextValue = !current.isFullTank
            preferences.setAutoCut(current.fuelType, nextValue)
            current.copy(isFullTank = nextValue)
        }
    }

    fun saveRefill(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val current = _uiState.value
            val qty = current.quantity
            val cost = current.totalCost
            if (qty <= 0 && cost <= 0) {
                return@launch
            }

            val event = FuelEvent(
                odometerKm = current.odometerKm,
                source = EventSource.MANUAL,
                type = EventType.REFILL,
                fuelType = current.fuelType,
                quantity = qty,
                pricePerUnit = current.unitRate,
                totalCost = cost,
                isFullTank = current.isFullTank,
                fuelLevelPercent = if (current.fuelType == FuelType.PETROL) 100.0 else null,
                confirmedByUser = true,
                stationName = current.stationName.ifBlank { null },
                isSimulation = repository.isSimulationMode.value
            )
            repository.addEvent(event)
            _uiState.update { it.copy(isSaved = true) }
            onSuccess()
        }
    }

    companion object {
        private fun formatRate(rate: Double): String = String.format(Locale.US, "%.2f", rate)
        private fun formatQty(qty: Double): String = if (qty % 1.0 == 0.0) String.format(Locale.US, "%.0f", qty) else String.format(Locale.US, "%.2f", qty)
        private fun formatCost(cost: Double): String = if (cost % 1.0 == 0.0) String.format(Locale.US, "%.0f", cost) else String.format(Locale.US, "%.1f", cost)
    }
}
