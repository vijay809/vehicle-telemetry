package com.antigravity.telemetry.core.designsystem.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Propane
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.telemetry.core.designsystem.CngAccent
import com.antigravity.telemetry.core.designsystem.CngPastelBg
import com.antigravity.telemetry.core.designsystem.CngPastelBorder
import com.antigravity.telemetry.core.designsystem.PetrolAccent
import com.antigravity.telemetry.core.designsystem.PetrolPastelBg
import com.antigravity.telemetry.core.designsystem.PetrolPastelBorder
import com.antigravity.telemetry.core.designsystem.RoundedSm
import com.antigravity.telemetry.core.designsystem.SlateSoft
import com.antigravity.telemetry.core.designsystem.SlateTextFaint
import com.antigravity.telemetry.core.designsystem.SlateTextMain
import com.antigravity.telemetry.core.designsystem.SlateTextMuted
import com.antigravity.telemetry.core.designsystem.SurfaceSubtle
import com.antigravity.telemetry.core.designsystem.SurfaceWhite
import com.antigravity.telemetry.core.model.FuelType
import com.antigravity.telemetry.core.model.MileageSegment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MileageBreakdownSheet(
    segments: List<MileageSegment>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<FuelType?>(null) } // null = All
    val filteredSegments = remember(segments, selectedFilter) {
        if (selectedFilter == null) segments else segments.filter { it.fuelType == selectedFilter }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(SurfaceWhite)
            .padding(top = 16.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title and Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedSm)
                            .background(CngPastelBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = CngAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Mileage Breakdown",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextMain
                        )
                        Text(
                            text = "Every event detail up to till now",
                            fontSize = 11.sp,
                            color = SlateTextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceSubtle)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SlateTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Filter Tabs: [ All | CNG | Petrol ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(SurfaceSubtle)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterTabItem(
                    label = "All Fuels (${segments.size})",
                    isSelected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    modifier = Modifier.weight(1f)
                )
                FilterTabItem(
                    label = "CNG",
                    isSelected = selectedFilter == FuelType.CNG,
                    onClick = { selectedFilter = FuelType.CNG },
                    accentColor = CngAccent,
                    modifier = Modifier.weight(1f)
                )
                FilterTabItem(
                    label = "Petrol",
                    isSelected = selectedFilter == FuelType.PETROL,
                    onClick = { selectedFilter = FuelType.PETROL },
                    accentColor = PetrolAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = SlateSoft.copy(alpha = 0.6f), thickness = 0.5.dp)

            // Segments List
            if (filteredSegments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricMeter,
                            contentDescription = null,
                            tint = SlateTextFaint,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No mileage cycles recorded yet",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateTextMuted
                        )
                        Text(
                            text = "Refill or mark empty/reserve to log efficiency",
                            fontSize = 11.sp,
                            color = SlateTextFaint
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSegments, key = { it.id }) { segment ->
                        MileageSegmentCard(segment = segment)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color = SlateTextMain,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isSelected) SurfaceWhite else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) accentColor else SlateTextMuted
        )
    }
}

@Composable
private fun MileageSegmentCard(
    segment: MileageSegment,
    modifier: Modifier = Modifier
) {
    val isCng = segment.fuelType == FuelType.CNG
    val accent = if (isCng) CngAccent else PetrolAccent
    val bg = if (segment.isOngoing) (if (isCng) CngPastelBg.copy(alpha = 0.35f) else PetrolPastelBg.copy(alpha = 0.35f)) else SurfaceSubtle
    val border = if (segment.isOngoing) (if (isCng) CngPastelBorder else PetrolPastelBorder) else SlateSoft.copy(alpha = 0.7f)
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedSm)
            .background(bg)
            .border(1.dp, border, RoundedSm)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Top Row: Fuel icon + Fuel Tag + Condition Badge + Timestamp / Ongoing Pill
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
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isCng) CngPastelBg else PetrolPastelBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCng) Icons.Default.Propane else Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    Text(
                        text = if (isCng) "CNG" else "PETROL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent
                    )

                    // Condition Badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceWhite)
                            .border(1.dp, border, CircleShape)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = segment.conditionLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (segment.isOngoing) accent else SlateTextMuted
                        )
                    }
                }

                if (segment.isOngoing) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(accent)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TILL NOW",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SurfaceWhite,
                            letterSpacing = 0.4.sp
                        )
                    }
                } else {
                    Text(
                        text = dateFormat.format(Date(segment.endTimestamp)),
                        fontSize = 10.sp,
                        color = SlateTextFaint
                    )
                }
            }

            // Middle Row: Odometer Span & Mileage Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${String.format(Locale.US, "%,.0f", segment.startOdometerKm)} km ➔ ${String.format(Locale.US, "%,.0f", segment.endOdometerKm)} km",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextMain
                    )
                    Text(
                        text = "Net: ${String.format(Locale.US, "%.1f", segment.netDistanceKm)} km • Gross: ${String.format(Locale.US, "%.0f", segment.rawDistanceKm)} km",
                        fontSize = 11.sp,
                        color = SlateTextMuted
                    )
                }

                if (segment.calculatedMileage != null) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", segment.calculatedMileage),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accent
                        )
                        Text(
                            text = if (isCng) "km/kg" else "km/L",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateTextMuted,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                } else {
                    Text(
                        text = "In Progress",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
            }

            // Bottom detail row: Fuel Qty & Cold start adjustments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (segment.fuelQuantity != null && segment.fuelQuantity > 0) {
                    Text(
                        text = "Fuel: ${String.format(Locale.US, "%.2f", segment.fuelQuantity)} ${if (isCng) "kg" else "L"}",
                        fontSize = 10.sp,
                        color = SlateTextFaint
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (segment.coldStartsCount > 0) {
                    Text(
                        text = "${segment.coldStartsCount} cold start${if (segment.coldStartsCount > 1) "s" else ""} (-${String.format(Locale.US, "%.1f", segment.coldStartDeductionKm)} km)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateTextMuted
                    )
                }
            }
        }
    }
}
