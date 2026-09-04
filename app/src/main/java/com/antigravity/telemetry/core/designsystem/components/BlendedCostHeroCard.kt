package com.antigravity.telemetry.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.antigravity.telemetry.core.designsystem.PetrolAccent
import com.antigravity.telemetry.core.designsystem.Rounded3xl
import com.antigravity.telemetry.core.designsystem.RoundedSm
import com.antigravity.telemetry.core.designsystem.SlateSoft
import com.antigravity.telemetry.core.designsystem.SlateTextFaint
import com.antigravity.telemetry.core.designsystem.SlateTextMain
import com.antigravity.telemetry.core.designsystem.SlateTextMuted
import com.antigravity.telemetry.core.designsystem.SurfaceSubtle
import com.antigravity.telemetry.core.designsystem.SurfaceWhite

@Composable
fun BlendedCostHeroCard(
    costPerKm: Double,
    totalDistanceKm: Double,
    totalSpend: Double,
    cngRatioPercent: Double,
    petrolRatioPercent: Double,
    cngCostPerKm: Double,
    petrolCostPerKm: Double,
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
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title & 4% badge
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
                        text = "BLENDED RUNNING COST",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = SlateTextMuted
                    )
                }

                if (costPerKm > 0) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CngBadge)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = CngAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "4% vs last mo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CngAccent
                        )
                    }
                }
            }

            // Price per km main display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "₹",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CngAccent,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = if (costPerKm > 0) String.format("%.2f", costPerKm) else "--",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateTextMain,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "/ km",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlateTextMuted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Distance & Spend Caption
            Text(
                text = if (totalDistanceKm > 0) "Based on last ${String.format("%,.0f", totalDistanceKm)} km (₹${String.format("%,.0f", totalSpend)} total spend)" else "Awaiting refill data to compute blended running cost",
                fontSize = 13.sp,
                color = SlateTextMuted
            )

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
