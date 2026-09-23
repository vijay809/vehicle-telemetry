package com.antigravity.telemetry.feature.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.telemetry.core.designsystem.CanvasLavender
import com.antigravity.telemetry.core.designsystem.CngAccent
import com.antigravity.telemetry.core.designsystem.CngBadge
import com.antigravity.telemetry.core.designsystem.CngPastelBg
import com.antigravity.telemetry.core.designsystem.CngPastelBorder
import com.antigravity.telemetry.core.designsystem.PetrolAccent
import com.antigravity.telemetry.core.designsystem.PetrolBadge
import com.antigravity.telemetry.core.designsystem.PetrolPastelBg
import com.antigravity.telemetry.core.designsystem.Rounded2xl
import com.antigravity.telemetry.core.designsystem.Rounded3xl
import com.antigravity.telemetry.core.designsystem.RoundedSm
import com.antigravity.telemetry.core.designsystem.SlateSoft
import com.antigravity.telemetry.core.designsystem.SlateTextFaint
import com.antigravity.telemetry.core.designsystem.SlateTextMain
import com.antigravity.telemetry.core.designsystem.SlateTextMuted
import com.antigravity.telemetry.core.designsystem.SurfaceWhite
import com.antigravity.telemetry.core.designsystem.components.BlendedCostHeroCard
import com.antigravity.telemetry.core.designsystem.components.CngEfficiencyCard
import com.antigravity.telemetry.core.designsystem.components.PetrolEfficiencyCard

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenSimulator: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
                    odometerKm = state.telemetry.odometerKm
                )
            }

            // Dual Fuel Cards: Sorted with Fuel in Use on Top
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.isCngInUse) {
                        // CNG Active (Primary)
                        CngEfficiencyCard(
                            mileageKmPerKg = state.cngEfficiency.latestMileageKmPerKg,
                            tankPercent = null, // Hidden unless vehicle hardware reports CNG tank level
                            currentTripKm = state.cngEfficiency.currentTripKm,
                            isCngExhausted = false,
                            exhaustedAtOdoKm = state.cngEfficiency.exhaustedAtOdometerKm,
                            currentOdoKm = state.telemetry.odometerKm,
                            lastFill = state.lastCngRefill
                        )

                        PetrolEfficiencyCard(
                            mileageKmPerL = state.displayedPetrolMileageKmPerL,
                            petrolPercent = state.telemetry.petrolPercent, // Only shown if reported by vehicle hardware
                            estimatedRangeKm = state.displayedPetrolEstimatedRangeKm,
                            currentTripKm = state.displayedPetrolCurrentTripKm,
                            isPetrolActive = false,
                            onMarkLowFuel = { viewModel.markPetrolLowFuel(state.telemetry.odometerKm) },
                            isLowFuelMarked = state.isLowFuelPetrolMarked,
                            lowFuelOdometerKm = state.lowFuelPetrolOdoKm,
                            lastFill = state.lastPetrolRefill,
                            includeColdStart = state.isPetrolColdStartIncluded,
                            onToggleColdStart = { viewModel.setPetrolColdStartIncluded(it) },
                            coldStartDeductionKm = state.petrolEfficiency.totalColdStartKm,
                            totalColdStarts = state.petrolEfficiency.totalColdStartsCount
                        )
                    } else {
                        // Petrol Active (CNG Exhausted -> CNG Greyed Out & Placed Below)
                        PetrolEfficiencyCard(
                            mileageKmPerL = state.displayedPetrolMileageKmPerL,
                            petrolPercent = state.telemetry.petrolPercent,
                            estimatedRangeKm = state.displayedPetrolEstimatedRangeKm,
                            currentTripKm = state.displayedPetrolCurrentTripKm,
                            isPetrolActive = true,
                            onMarkLowFuel = { viewModel.markPetrolLowFuel(state.telemetry.odometerKm) },
                            isLowFuelMarked = state.isLowFuelPetrolMarked,
                            lowFuelOdometerKm = state.lowFuelPetrolOdoKm,
                            lastFill = state.lastPetrolRefill,
                            includeColdStart = state.isPetrolColdStartIncluded,
                            onToggleColdStart = { viewModel.setPetrolColdStartIncluded(it) },
                            coldStartDeductionKm = state.petrolEfficiency.totalColdStartKm,
                            totalColdStarts = state.petrolEfficiency.totalColdStartsCount
                        )

                        CngEfficiencyCard(
                            mileageKmPerKg = state.cngEfficiency.latestMileageKmPerKg,
                            tankPercent = null,
                            currentTripKm = state.cngEfficiency.currentTripKm,
                            isCngExhausted = true,
                            exhaustedAtOdoKm = state.cngEfficiency.exhaustedAtOdometerKm,
                            currentOdoKm = state.telemetry.odometerKm,
                            lastFill = state.lastCngRefill
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
    }
