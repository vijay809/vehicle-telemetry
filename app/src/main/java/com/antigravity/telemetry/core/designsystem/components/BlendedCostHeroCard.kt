package com.antigravity.telemetry.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.telemetry.core.designsystem.CngAccent
import com.antigravity.telemetry.core.designsystem.CngBadge
import com.antigravity.telemetry.core.designsystem.CngPastelBg
import com.antigravity.telemetry.core.designsystem.CngPastelBorder
import com.antigravity.telemetry.core.designsystem.PetrolAccent
import com.antigravity.telemetry.core.designsystem.Rounded3xl
import com.antigravity.telemetry.core.designsystem.RoundedSm
import com.antigravity.telemetry.core.designsystem.SlateSoft
import com.antigravity.telemetry.core.designsystem.SlateTextFaint
import com.antigravity.telemetry.core.designsystem.SlateTextMain
import com.antigravity.telemetry.core.designsystem.SlateTextMuted
import com.antigravity.telemetry.core.designsystem.SurfaceSubtle
import com.antigravity.telemetry.core.designsystem.SurfaceWhite

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.Savings
import com.antigravity.telemetry.core.model.CostTimeframe

@Composable
fun CostTimeframeSelector(
    selectedTimeframe: CostTimeframe,
    onTimeframeSelected: (CostTimeframe) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(SurfaceSubtle)
            .border(1.dp, SlateSoft.copy(alpha = 0.8f), CircleShape)
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CostTimeframe.values().forEach { timeframe ->
            val isSelected = timeframe == selectedTimeframe
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) CngAccent else Color.Transparent)
                    .clickable { onTimeframeSelected(timeframe) }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeframe.label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (isSelected) SurfaceWhite else SlateTextMuted
                )
            }
        }
    }
}

@Composable
fun BlendedCostHeroCard(
    costPerKm: Double,
    totalDistanceKm: Double,
    totalSpend: Double,
    cngRatioPercent: Double,
    petrolRatioPercent: Double,
    cngCostPerKm: Double,
    petrolCostPerKm: Double,
    odometerKm: Double = 0.0,
    monthlySavings: Double = 0.0,
    selectedTimeframe: CostTimeframe = CostTimeframe.ONE_MONTH,
    onTimeframeSelected: ((CostTimeframe) -> Unit)? = null,
    onOdometerClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val animatedCngRatio by animateFloatAsState(targetValue = (cngRatioPercent / 100f).toFloat(), label = "cngRatio")
    val animatedPetRatio by animateFloatAsState(targetValue = (petrolRatioPercent / 100f).toFloat(), label = "petRatio")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = Rounded3xl,
                spotColor = Color(0x0F0F172A),
                ambientColor = Color(0x050F172A)
            )
            .clip(Rounded3xl)
            .background(SurfaceWhite)
            .border(1.dp, SlateSoft.copy(alpha = 0.8f), Rounded3xl)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Title & Timeframe Selector & Odometer pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedSm)
                            .background(CngPastelBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = null,
                            tint = CngAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "RUNNING COST",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = SlateTextMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CostTimeframeSelector(
                        selectedTimeframe = selectedTimeframe,
                        onTimeframeSelected = { onTimeframeSelected?.invoke(it) }
                    )

                    // Odometer Pill
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceSubtle)
                            .border(1.dp, SlateSoft.copy(alpha = 0.8f), CircleShape)
                            .then(
                                if (onOdometerClick != null) Modifier.clickable { onOdometerClick() } else Modifier
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Odometer",
                            tint = SlateTextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (odometerKm > 0) String.format(java.util.Locale.US, "%,.0f km", odometerKm) else "-- km",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextMain
                        )
                    }
                }
            }

            // Middle Section: Price per km display + optional monthly savings pill + distance & spend caption
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "₹",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = CngAccent,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                        Text(
                            text = if (costPerKm > 0) String.format("%.2f", costPerKm) else "--",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SlateTextMain,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "/ km",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateTextMuted,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }

                    if (monthlySavings > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CngPastelBg)
                                .border(1.dp, CngPastelBorder, CircleShape)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = CngAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Saved ₹${String.format(java.util.Locale.US, "%,.0f", monthlySavings)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                            }
                        }
                    }
                }

                // Distance & Spend Caption
                Text(
                    text = if (totalDistanceKm > 0) "${selectedTimeframe.label} timeframe: ${String.format("%,.0f", totalDistanceKm)} km (₹${String.format("%,.0f", totalSpend)} total spend)" else "No refill logged in ${selectedTimeframe.label} timeframe",
                    fontSize = 12.sp,
                    color = SlateTextMuted
                )
            }

            // Segmented Progress Track
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(SurfaceSubtle)
                        .padding(2.dp)
                ) {
                    if (animatedCngRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(animatedCngRatio.coerceAtLeast(0.01f))
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(CngAccent, Color(0xFF10B981))
                                    )
                                )
                        )
                    }
                    if (animatedPetRatio > 0f) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .weight(animatedPetRatio.coerceAtLeast(0.01f))
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(PetrolAccent, Color(0xFFF59E0B))
                                    )
                                )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CngAccent)
                        )
                        Text(
                            text = "CNG ${String.format("%.0f", cngRatioPercent)}% ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateTextMuted
                        )
                        Text(
                            text = "(₹${String.format("%.2f", cngCostPerKm)}/km)",
                            fontSize = 11.sp,
                            color = SlateTextFaint
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PetrolAccent)
                        )
                        Text(
                            text = "Petrol ${String.format("%.0f", petrolRatioPercent)}% ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateTextMuted
                        )
                        Text(
                            text = "(₹${String.format("%.2f", petrolCostPerKm)}/km)",
                            fontSize = 11.sp,
                            color = SlateTextFaint
                        )
                    }
                }
            }
        }
    }
}
