package com.antigravity.telemetry.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Propane
import androidx.compose.material.icons.filled.Warning
import com.antigravity.telemetry.core.model.FuelEvent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.telemetry.core.designsystem.AlertAccent
import com.antigravity.telemetry.core.designsystem.AlertPastelBg
import com.antigravity.telemetry.core.designsystem.AlertPastelBorder
import com.antigravity.telemetry.core.designsystem.CngAccent
import com.antigravity.telemetry.core.designsystem.CngBadge
import com.antigravity.telemetry.core.designsystem.CngPastelBg
import com.antigravity.telemetry.core.designsystem.CngPastelBorder
import com.antigravity.telemetry.core.designsystem.PetrolAccent
import com.antigravity.telemetry.core.designsystem.PetrolBadge
import com.antigravity.telemetry.core.designsystem.PetrolPastelBg
import com.antigravity.telemetry.core.designsystem.PetrolPastelBorder
import com.antigravity.telemetry.core.designsystem.Rounded3xl
import com.antigravity.telemetry.core.designsystem.RoundedSm
import com.antigravity.telemetry.core.designsystem.SlateSoft
import com.antigravity.telemetry.core.designsystem.SlateTextFaint
import com.antigravity.telemetry.core.designsystem.SlateTextMain
import com.antigravity.telemetry.core.designsystem.SlateTextMuted
import com.antigravity.telemetry.core.designsystem.SurfaceSubtle
import com.antigravity.telemetry.core.designsystem.SurfaceWhite

@Composable
fun CngEfficiencyCard(
    mileageKmPerKg: Double,
    tankPercent: Double?, // Nullable: only shown if reported by vehicle hardware
    currentTripKm: Double,
    isCngExhausted: Boolean,
    exhaustedAtOdoKm: Double?,
    currentOdoKm: Double,
    lastFill: FuelEvent? = null,
    modifier: Modifier = Modifier
) {
    // When CNG is empty, grey out the card
    val cardBg = if (isCngExhausted) SurfaceSubtle.copy(alpha = 0.8f) else SurfaceWhite
    val cardBorder = if (isCngExhausted) SlateSoft else CngPastelBorder.copy(alpha = 0.6f)
    val cardAlpha = if (isCngExhausted) 0.65f else 1f
    val iconBg = if (isCngExhausted) SlateSoft else CngPastelBg
    val iconTint = if (isCngExhausted) SlateTextMuted else CngAccent
    val titleColor = if (isCngExhausted) SlateTextMuted else SlateTextMain
    val valueColor = if (isCngExhausted) SlateTextMuted else SlateTextMain

    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .shadow(if (isCngExhausted) 1.dp else 4.dp, Rounded3xl, spotColor = Color(0x0A0F172A))
            .clip(Rounded3xl)
            .background(cardBg)
            .border(1.dp, cardBorder, Rounded3xl)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top row: Title & status badge
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
                            .size(32.dp)
                            .clip(RoundedSm)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Propane,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Text(
                        text = "CNG Efficiency",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                }

                if (isCngExhausted) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SlateSoft)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Exhausted",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextMuted
                        )
                    }
                } else if (tankPercent != null) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CngBadge)
                            .border(1.dp, CngPastelBorder.copy(alpha = 0.6f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${String.format("%.0f", tankPercent)}% Tank",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CngAccent
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CngBadge.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "In Use",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CngAccent
                        )
                    }
                }
            }

            // Middle row: Large km/kg & current trip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (mileageKmPerKg > 0) String.format("%.1f", mileageKmPerKg) else "--",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = valueColor,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "km/kg",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateTextMuted,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "CURRENT TRIP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = SlateTextFaint
                    )
                    Text(
                        text = "${String.format("%.0f", currentTripKm)} km",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = valueColor
                    )
                }
            }

            // Only show meter bar if vehicle actually provides tank level data (no guesswork)
            if (tankPercent != null && !isCngExhausted) {
                val animatedTank by animateFloatAsState(
                    targetValue = (tankPercent / 100f).toFloat().coerceIn(0f, 1f),
                    label = "cngTank"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(SurfaceSubtle)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedTank)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(CngAccent)
                    )
                }
            }

            // Footer note
            if (isCngExhausted && exhaustedAtOdoKm != null) {
                val reserveKm = (currentOdoKm - exhaustedAtOdoKm).coerceAtLeast(0.0)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AlertAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Marked empty at ${String.format("%,.0f", exhaustedAtOdoKm)} km",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = AlertAccent
                        )
                    }
                    Text(
                        text = "+${String.format("%.0f", reserveKm)} km on reserve",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PetrolAccent
                    )
                }
            }

            // Integrated Last Fill Details
            if (lastFill != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedSm)
                        .background(if (isCngExhausted) SlateSoft.copy(alpha = 0.5f) else SurfaceSubtle)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
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
                                    .size(24.dp)
                                    .clip(RoundedSm)
                                    .background(if (isCngExhausted) SlateSoft else CngPastelBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EvStation,
                                    contentDescription = null,
                                    tint = if (isCngExhausted) SlateTextMuted else CngAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "LAST CNG FILL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = SlateTextFaint
                                )
                                Text(
                                    text = "${String.format("%.2f", lastFill.quantity ?: 0.0)} kg @ ₹${String.format("%.1f", lastFill.pricePerUnit ?: 0.0)}/kg",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = titleColor
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            lastFill.totalCost?.let { cost ->
                                Text(
                                    text = "₹${String.format("%.0f", cost)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCngExhausted) SlateTextMuted else CngAccent
                                )
                            }
                            Text(
                                text = "@ ${String.format("%,.0f", lastFill.odometerKm)} km",
                                fontSize = 10.sp,
                                color = SlateTextFaint
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PetrolColdStartToggle(
    includeColdStart: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(PetrolPastelBg)
            .border(1.dp, PetrolPastelBorder.copy(alpha = 0.8f), CircleShape)
            .padding(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            // "w/ Cold Start" segment
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (includeColdStart) SurfaceWhite else Color.Transparent)
                    .then(
                        if (includeColdStart) Modifier.shadow(1.dp, CircleShape, spotColor = Color(0x1AD97706))
                        else Modifier
                    )
                    .clickable { onToggle(true) }
                    .padding(horizontal = 7.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AcUnit,
                        contentDescription = null,
                        tint = if (includeColdStart) PetrolAccent else SlateTextFaint,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "w/ CS",
                        fontSize = 10.sp,
                        fontWeight = if (includeColdStart) FontWeight.Bold else FontWeight.Medium,
                        color = if (includeColdStart) PetrolAccent else SlateTextMuted
                    )
                }
            }

            // "w/o Cold Start" segment
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (!includeColdStart) SurfaceWhite else Color.Transparent)
                    .then(
                        if (!includeColdStart) Modifier.shadow(1.dp, CircleShape, spotColor = Color(0x1AD97706))
                        else Modifier
                    )
                    .clickable { onToggle(false) }
                    .padding(horizontal = 7.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "w/o",
                    fontSize = 10.sp,
                    fontWeight = if (!includeColdStart) FontWeight.Bold else FontWeight.Medium,
                    color = if (!includeColdStart) PetrolAccent else SlateTextMuted
                )
            }
        }
    }
}

@Composable
fun PetrolEfficiencyCard(
    mileageKmPerL: Double,
    petrolPercent: Double?, // Nullable: only shown if reported by vehicle hardware
    estimatedRangeKm: Double,
    currentTripKm: Double = 0.0,
    isPetrolActive: Boolean = false,
    onMarkLowFuel: (() -> Unit)? = null,
    isLowFuelMarked: Boolean = false,
    lowFuelOdometerKm: Double? = null,
    lastFill: FuelEvent? = null,
    includeColdStart: Boolean = true,
    onToggleColdStart: ((Boolean) -> Unit)? = null,
    coldStartDeductionKm: Double = 0.0,
    totalColdStarts: Int = 0,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (isPetrolActive) 4.dp else 2.dp, Rounded3xl, spotColor = Color(0x0A0F172A))
            .clip(Rounded3xl)
            .background(SurfaceWhite)
            .border(
                1.dp,
                if (isPetrolActive) PetrolAccent.copy(alpha = 0.5f) else PetrolPastelBorder.copy(alpha = 0.6f),
                Rounded3xl
            )
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top row: Title on left, Cold Start Toggle & Status Badge on right
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
                            .size(32.dp)
                            .clip(RoundedSm)
                            .background(PetrolPastelBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = PetrolAccent,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Text(
                        text = "Petrol Efficiency",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextMain
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PetrolColdStartToggle(
                        includeColdStart = includeColdStart,
                        onToggle = { onToggleColdStart?.invoke(it) }
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PetrolBadge)
                            .border(1.dp, PetrolPastelBorder.copy(alpha = 0.6f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isPetrolActive) "In Use" else "Reserve",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PetrolAccent
                        )
                    }
                }
            }

            // Middle row: Large km/L on left, CURRENT TRIP on right (symmetrical with CNG card)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (mileageKmPerL > 0) String.format(Locale.US, "%.1f", mileageKmPerL) else "--",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateTextMain,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "km/L",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateTextMuted,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "CURRENT TRIP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = SlateTextFaint
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.0f", currentTripKm)} km",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PetrolAccent
                    )
                }
            }

            // Only show meter bar if petrol percentage is actually measured by vehicle (no guesswork)
            if (petrolPercent != null) {
                val animatedTank by animateFloatAsState(
                    targetValue = (petrolPercent / 100f).toFloat().coerceIn(0f, 1f),
                    label = "petrolTank"
                )
                val remainingLiters = (petrolPercent / 100.0) * 45.0

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(SurfaceSubtle)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedTank)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(PetrolAccent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.0f", petrolPercent)}% remaining (approx ${String.format(Locale.US, "%.1f", remainingLiters)} L)",
                        fontSize = 11.sp,
                        color = SlateTextMuted
                    )
                    if (estimatedRangeKm > 0) {
                        Text(
                            text = "Est. Range: ~${String.format(Locale.US, "%.0f", estimatedRangeKm)} km",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PetrolAccent
                        )
                    }
                }
            }

            // Cold Start Adjustment Footnote
            Text(
                text = if (includeColdStart) {
                    if (coldStartDeductionKm > 0) "+${String.format(Locale.US, "%.1f", coldStartDeductionKm)} km cold start warmup credited ($totalColdStarts starts)"
                    else "1.2 km/cold start adj included"
                } else {
                    "Pure petrol driving only (cold start warmup excluded)"
                },
                fontSize = 10.sp,
                color = SlateTextFaint
            )

            // Integrated Last Fill Details
            if (lastFill != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedSm)
                        .background(SurfaceSubtle)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
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
                                    .size(24.dp)
                                    .clip(RoundedSm)
                                    .background(PetrolPastelBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = PetrolAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "LAST PETROL FILL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = SlateTextFaint
                                )
                                Text(
                                    text = "${String.format("%.2f", lastFill.quantity ?: 0.0)} L @ ₹${String.format("%.1f", lastFill.pricePerUnit ?: 0.0)}/L",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextMain
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            lastFill.totalCost?.let { cost ->
                                Text(
                                    text = "₹${String.format("%.0f", cost)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PetrolAccent
                                )
                            }
                            Text(
                                text = "@ ${String.format("%,.0f", lastFill.odometerKm)} km",
                                fontSize = 10.sp,
                                color = SlateTextFaint
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subtractive residual (1.2 km/cold start)",
                    fontSize = 11.sp,
                    color = SlateTextFaint,
                    modifier = Modifier.weight(1f)
                )

                if (onMarkLowFuel != null) {
                    if (isLowFuelMarked) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AlertPastelBg)
                                .border(1.dp, AlertPastelBorder, CircleShape)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AlertAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (lowFuelOdometerKm != null) "Reserve @ ${String.format("%,.0f", lowFuelOdometerKm)} km" else "Reserve Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertAccent
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PetrolPastelBg)
                                .border(1.dp, PetrolPastelBorder, CircleShape)
                                .clickable(onClick = onMarkLowFuel)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = PetrolAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Mark Low Fuel",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PetrolAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
