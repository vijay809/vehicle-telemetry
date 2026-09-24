package com.antigravity.telemetry.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Propane
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.telemetry.core.designsystem.CngAccent
import com.antigravity.telemetry.core.designsystem.CngPastelBg
import com.antigravity.telemetry.core.designsystem.CngPastelBorder
import com.antigravity.telemetry.core.designsystem.PetrolAccent
import com.antigravity.telemetry.core.designsystem.Rounded3xl
import com.antigravity.telemetry.core.designsystem.RoundedSm
import com.antigravity.telemetry.core.designsystem.SlateSoft
import com.antigravity.telemetry.core.designsystem.SlateTextFaint
import com.antigravity.telemetry.core.designsystem.SlateTextMain
import com.antigravity.telemetry.core.designsystem.SlateTextMuted
import com.antigravity.telemetry.core.designsystem.SurfaceWhite
import com.antigravity.telemetry.core.designsystem.SwitchLavenderAccent
import com.antigravity.telemetry.core.designsystem.SwitchLavenderBg
import com.antigravity.telemetry.core.designsystem.SwitchLavenderBorder
import com.antigravity.telemetry.core.designsystem.components.BlendedCostHeroCard
import com.antigravity.telemetry.core.designsystem.components.CngEfficiencyCard
import com.antigravity.telemetry.core.designsystem.components.OdometerUpdateSheet
import com.antigravity.telemetry.core.designsystem.components.PetrolEfficiencyCard
import com.antigravity.telemetry.core.model.FuelType

private enum class DashboardOdoAction {
    UPDATE_ODOMETER,
    MARK_CNG_EMPTY,
    MARK_LOW_FUEL,
    SWITCH_FUEL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenSimulator: () -> Unit = {},
    onOpenOdometerSheet: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var activeOdoAction by remember { mutableStateOf<DashboardOdoAction?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hero Card: Blended Running Cost (Responsive weight = 1.15f)
            BlendedCostHeroCard(
                costPerKm = state.blendedCost.blendedCostPerKm,
                totalDistanceKm = state.blendedCost.totalDistanceKm,
                totalSpend = state.blendedCost.totalCost,
                cngRatioPercent = state.blendedCost.cngSharePercent,
                petrolRatioPercent = state.blendedCost.petrolSharePercent,
                cngCostPerKm = state.blendedCost.cngCostPerKm,
                petrolCostPerKm = state.blendedCost.petrolCostPerKm,
                odometerKm = state.telemetry.odometerKm,
                monthlySavings = state.blendedCost.monthlySavingsVsPetrol,
                onOdometerClick = { activeOdoAction = DashboardOdoAction.UPDATE_ODOMETER },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.15f)
            )

            // Primary Fuel Card (In Use)
            if (state.isCngInUse) {
                CngEfficiencyCard(
                    mileageKmPerKg = state.cngEfficiency.latestMileageKmPerKg,
                    tankPercent = null,
                    currentTripKm = state.cngEfficiency.currentTripKm,
                    isCngExhausted = false,
                    exhaustedAtOdoKm = state.cngEfficiency.exhaustedAtOdometerKm,
                    currentOdoKm = state.telemetry.odometerKm,
                    lastFill = state.lastCngRefill,
                    onMarkEmpty = { activeOdoAction = DashboardOdoAction.MARK_CNG_EMPTY },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f)
                )
            } else {
                PetrolEfficiencyCard(
                    mileageKmPerL = state.displayedPetrolMileageKmPerL,
                    petrolPercent = state.telemetry.petrolPercent,
                    estimatedRangeKm = state.displayedPetrolEstimatedRangeKm,
                    currentTripKm = state.displayedPetrolCurrentTripKm,
                    isPetrolActive = true,
                    onMarkLowFuel = { activeOdoAction = DashboardOdoAction.MARK_LOW_FUEL },
                    isLowFuelMarked = state.isLowFuelPetrolMarked,
                    lowFuelOdometerKm = state.lowFuelPetrolOdoKm,
                    lastFill = state.lastPetrolRefill,
                    includeColdStart = state.isPetrolColdStartIncluded,
                    onToggleColdStart = { viewModel.setPetrolColdStartIncluded(it) },
                    coldStartDeductionKm = state.petrolEfficiency.totalColdStartKm,
                    totalColdStarts = state.petrolEfficiency.totalColdStartsCount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f)
                )
            }

            // Switch Fuel Button between fuel cards
            FuelSwitchButton(
                isCngInUse = state.isCngInUse,
                onSwitch = { activeOdoAction = DashboardOdoAction.SWITCH_FUEL },
                modifier = Modifier.fillMaxWidth()
            )

            // Secondary Fuel Card (Standby / Exhausted)
            if (state.isCngInUse) {
                PetrolEfficiencyCard(
                    mileageKmPerL = state.displayedPetrolMileageKmPerL,
                    petrolPercent = state.telemetry.petrolPercent,
                    estimatedRangeKm = state.displayedPetrolEstimatedRangeKm,
                    currentTripKm = state.displayedPetrolCurrentTripKm,
                    isPetrolActive = false,
                    onMarkLowFuel = { activeOdoAction = DashboardOdoAction.MARK_LOW_FUEL },
                    isLowFuelMarked = state.isLowFuelPetrolMarked,
                    lowFuelOdometerKm = state.lowFuelPetrolOdoKm,
                    lastFill = state.lastPetrolRefill,
                    includeColdStart = state.isPetrolColdStartIncluded,
                    onToggleColdStart = { viewModel.setPetrolColdStartIncluded(it) },
                    coldStartDeductionKm = state.petrolEfficiency.totalColdStartKm,
                    totalColdStarts = state.petrolEfficiency.totalColdStartsCount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f)
                )
            } else {
                CngEfficiencyCard(
                    mileageKmPerKg = state.cngEfficiency.latestMileageKmPerKg,
                    tankPercent = null,
                    currentTripKm = state.cngEfficiency.currentTripKm,
                    isCngExhausted = state.cngEfficiency.isCngExhausted,
                    exhaustedAtOdoKm = state.cngEfficiency.exhaustedAtOdometerKm,
                    currentOdoKm = state.telemetry.odometerKm,
                    isCngInUse = false,
                    lastFill = state.lastCngRefill,
                    onMarkEmpty = if (!state.cngEfficiency.isCngExhausted) {
                        { activeOdoAction = DashboardOdoAction.MARK_CNG_EMPTY }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f)
                )
            }
        }

        // Reusable Odometer Sheet for all 4 Dashboard actions
        if (activeOdoAction != null) {
            val action = activeOdoAction!!
            val targetFuel = if (state.isCngInUse) FuelType.PETROL else FuelType.CNG
            val currentOdo = state.telemetry.odometerKm

            key(action) {
                ModalBottomSheet(
                    onDismissRequest = { activeOdoAction = null },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = Color.Transparent
                ) {
                    when (action) {
                        DashboardOdoAction.UPDATE_ODOMETER -> {
                            OdometerUpdateSheet(
                                currentOdometerKm = currentOdo,
                                title = "Update Odometer",
                                confirmButtonText = "Save Odometer",
                                accentColor = SlateTextMain,
                                icon = Icons.Default.Speed,
                                onConfirmOdometer = { newOdo ->
                                    viewModel.logOdometerUpdate(newOdo)
                                    activeOdoAction = null
                                },
                                onDismiss = { activeOdoAction = null }
                            )
                        }
                        DashboardOdoAction.MARK_CNG_EMPTY -> {
                            OdometerUpdateSheet(
                                currentOdometerKm = currentOdo,
                                title = "Mark CNG Empty",
                                confirmButtonText = "Confirm CNG Empty",
                                accentColor = CngAccent,
                                icon = Icons.Default.Propane,
                                initialColdStarts = viewModel.getActiveCycleColdStarts(),
                                onConfirmOdometer = { newOdo ->
                                    viewModel.markCngEmpty(newOdo)
                                    activeOdoAction = null
                                },
                                onConfirmWithColdStarts = { newOdo, coldStarts ->
                                    viewModel.markCngEmpty(newOdo, coldStarts)
                                    activeOdoAction = null
                                },
                                onDismiss = { activeOdoAction = null }
                            )
                        }
                        DashboardOdoAction.MARK_LOW_FUEL -> {
                            OdometerUpdateSheet(
                                currentOdometerKm = currentOdo,
                                title = "Mark Petrol Low Fuel",
                                confirmButtonText = "Confirm Low Fuel",
                                accentColor = PetrolAccent,
                                icon = Icons.Default.Warning,
                                onConfirmOdometer = { newOdo ->
                                    viewModel.markPetrolLowFuel(newOdo)
                                    activeOdoAction = null
                                },
                                onDismiss = { activeOdoAction = null }
                            )
                        }
                        DashboardOdoAction.SWITCH_FUEL -> {
                            OdometerUpdateSheet(
                                currentOdometerKm = currentOdo,
                                title = "Switch to ${if (targetFuel == FuelType.CNG) "CNG" else "Petrol"}",
                                confirmButtonText = "Confirm Switch to ${if (targetFuel == FuelType.CNG) "CNG" else "Petrol"}",
                                accentColor = SwitchLavenderAccent,
                                icon = Icons.Default.SwapVert,
                                onConfirmOdometer = { newOdo ->
                                    viewModel.logManualFuelSwitch(targetFuel, newOdo)
                                    activeOdoAction = null
                                },
                                onDismiss = { activeOdoAction = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FuelSwitchButton(
    isCngInUse: Boolean,
    onSwitch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(SlateSoft.copy(alpha = 0.8f))
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .shadow(2.dp, CircleShape, spotColor = SwitchLavenderAccent.copy(alpha = 0.15f))
                .clip(CircleShape)
                .background(SwitchLavenderBg)
                .border(1.2.dp, SwitchLavenderBorder, CircleShape)
                .clickable(onClick = onSwitch)
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Switch Fuel Mode",
                    tint = SwitchLavenderAccent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isCngInUse) "Switch to Petrol" else "Switch to CNG",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SwitchLavenderAccent
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(SlateSoft.copy(alpha = 0.8f))
        )
    }
}
