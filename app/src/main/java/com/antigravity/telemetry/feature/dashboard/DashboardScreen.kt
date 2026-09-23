package com.antigravity.telemetry.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Propane
import androidx.compose.material.icons.filled.Savings
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Card: Blended Running Cost
            item {
                BlendedCostHeroCard(
                    costPerKm = state.blendedCost.blendedCostPerKm,
                    totalDistanceKm = state.blendedCost.totalDistanceKm,
                    totalSpend = state.blendedCost.totalCost,
                    cngRatioPercent = state.blendedCost.cngSharePercent,
                    petrolRatioPercent = state.blendedCost.petrolSharePercent,
                    cngCostPerKm = state.blendedCost.cngCostPerKm,
                    petrolCostPerKm = state.blendedCost.petrolCostPerKm,
                    odometerKm = state.telemetry.odometerKm,
                    onOdometerClick = { activeOdoAction = DashboardOdoAction.UPDATE_ODOMETER }
                )
            }

            // Dual Fuel Cards: Sorted with Fuel in Use on Top, with Switch Fuel Button In-Between
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.isCngInUse) {
                        // CNG Active (Primary)
                        CngEfficiencyCard(
                            mileageKmPerKg = state.cngEfficiency.latestMileageKmPerKg,
                            tankPercent = null,
                            currentTripKm = state.cngEfficiency.currentTripKm,
                            isCngExhausted = false,
                            exhaustedAtOdoKm = state.cngEfficiency.exhaustedAtOdometerKm,
                            currentOdoKm = state.telemetry.odometerKm,
                            lastFill = state.lastCngRefill,
                            onMarkEmpty = { activeOdoAction = DashboardOdoAction.MARK_CNG_EMPTY }
                        )

                        // Switch Fuel Button between fuel cards
                        FuelSwitchButton(
                            isCngInUse = true,
                            onSwitch = { activeOdoAction = DashboardOdoAction.SWITCH_FUEL }
                        )

                        // Petrol (Secondary / Standby)
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
                            totalColdStarts = state.petrolEfficiency.totalColdStartsCount
                        )
                    } else {
                        // Petrol Active (Primary)
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
                            totalColdStarts = state.petrolEfficiency.totalColdStartsCount
                        )

                        // Switch Fuel Button between fuel cards
                        FuelSwitchButton(
                            isCngInUse = false,
                            onSwitch = { activeOdoAction = DashboardOdoAction.SWITCH_FUEL }
                        )

                        // CNG (Secondary / Exhausted)
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
                            } else null
                        )
                    }
                }
            }

            // Savings Banner (Soft Serene Mint) - Only show if savings exist
            if (state.blendedCost.monthlySavingsVsPetrol > 0) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, Rounded3xl, spotColor = Color(0x0A10B981))
                            .clip(Rounded3xl)
                            .background(CngPastelBg)
                            .border(1.dp, CngPastelBorder, Rounded3xl)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedSm)
                                    .background(SurfaceWhite),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = CngAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "BI-FUEL ADVANTAGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp,
                                    color = SlateTextMuted
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Saved ₹${String.format("%,.0f", state.blendedCost.monthlySavingsVsPetrol)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF065F46)
                                    )
                                    Text(
                                        text = "this month vs Petrol",
                                        fontSize = 12.sp,
                                        color = SlateTextMuted
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SlateTextFaint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
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
                                onConfirmOdometer = { newOdo ->
                                    viewModel.markCngEmpty(newOdo)
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
                .shadow(2.dp, CircleShape, spotColor = Color(0x158B73A8))
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
