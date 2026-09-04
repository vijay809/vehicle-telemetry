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
import androidx.compose.material.icons.filled.SettingsSuggest
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
import com.antigravity.telemetry.core.designsystem.components.FloatingQuickActionDock
import com.antigravity.telemetry.core.designsystem.components.PetrolEfficiencyCard

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToRefill: () -> Unit,
    onNavigateToCngEmpty: () -> Unit,
    onNavigateToLedger: () -> Unit,
    onOpenSimulator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CanvasLavender,
        bottomBar = {
            FloatingQuickActionDock(
                onCngEmptyClick = onNavigateToCngEmpty,
                onRefillClick = onNavigateToRefill,
                onHistoryClick = onNavigateToLedger
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Airy Light Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedSm)
                                .background(CngBadge)
                                .border(1.dp, CngPastelBorder, RoundedSm),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "VC",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CngAccent
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = state.vehicle.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextMain
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(CngPastelBg)
                                        .border(1.dp, CngPastelBorder, CircleShape)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(CngAccent)
                                                .alpha(pulseAlpha)
                                        )
                                        Text(
                                            text = "Live Sync",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CngAccent
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Synced via Android Auto",
                                fontSize = 11.sp,
                                color = SlateTextMuted
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Odometer Pill
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SurfaceWhite)
                                .border(1.dp, SlateSoft, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = SlateTextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = String.format("%,.0f", state.telemetry.odometerKm),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateTextMain
                            )
                            Text(
                                text = "km",
                                fontSize = 11.sp,
                                color = SlateTextFaint
                            )
                        }

                        // Simulator Trigger Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceWhite)
                                .border(1.dp, SlateSoft, CircleShape)
                                .clickable(onClick = onOpenSimulator),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Simulator",
                                tint = SlateTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Top Live Status Capsule
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, Rounded2xl, spotColor = Color(0x060F172A))
                        .clip(Rounded2xl)
                        .background(SurfaceWhite.copy(alpha = 0.85f))
                        .border(1.dp, SlateSoft.copy(alpha = 0.6f), Rounded2xl)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CngAccent)
                                .alpha(pulseAlpha)
                        )
                        Text(
                            text = "LIVE ECU TELEMETRY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = SlateTextMuted
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PetrolPastelBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsSuggest,
                            contentDescription = null,
                            tint = PetrolAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Auto Mode Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PetrolAccent
                        )
                    }
                }
            }

            // Hero Card: Blended Running Cost
            item {
                BlendedCostHeroCard(
                    costPerKm = state.blendedCost.blendedCostPerKm,
                    totalDistanceKm = state.blendedCost.totalDistanceKm,
                    totalSpend = state.blendedCost.totalCost,
                    cngRatioPercent = state.blendedCost.cngSharePercent,
                    petrolRatioPercent = state.blendedCost.petrolSharePercent,
                    cngCostPerKm = state.blendedCost.cngCostPerKm,
                    petrolCostPerKm = state.blendedCost.petrolCostPerKm
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
                            mileageKmPerL = state.petrolEfficiency.latestMileageKmPerL,
                            petrolPercent = state.telemetry.petrolPercent, // Only shown if reported by vehicle hardware
                            estimatedRangeKm = state.petrolEfficiency.estimatedRangeKm,
                            isPetrolActive = false,
                            onMarkLowFuel = { viewModel.markPetrolLowFuel(state.telemetry.odometerKm) },
                            isLowFuelMarked = state.isLowFuelPetrolMarked,
                            lowFuelOdometerKm = state.lowFuelPetrolOdoKm,
                            lastFill = state.lastPetrolRefill
                        )
                    } else {
                        // Petrol Active (CNG Exhausted -> CNG Greyed Out & Placed Below)
                        PetrolEfficiencyCard(
                            mileageKmPerL = state.petrolEfficiency.latestMileageKmPerL,
                            petrolPercent = state.telemetry.petrolPercent,
                            estimatedRangeKm = state.petrolEfficiency.estimatedRangeKm,
                            isPetrolActive = true,
                            onMarkLowFuel = { viewModel.markPetrolLowFuel(state.telemetry.odometerKm) },
                            isLowFuelMarked = state.isLowFuelPetrolMarked,
                            lowFuelOdometerKm = state.lowFuelPetrolOdoKm,
                            lastFill = state.lastPetrolRefill
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
}
