package com.antigravity.telemetry.feature.ledger

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Propane
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.antigravity.telemetry.core.designsystem.Rounded2xl
import com.antigravity.telemetry.core.designsystem.Rounded3xl
import com.antigravity.telemetry.core.designsystem.RoundedSm
import com.antigravity.telemetry.core.designsystem.SlateSoft
import com.antigravity.telemetry.core.designsystem.SlateTextFaint
import com.antigravity.telemetry.core.designsystem.SlateTextMain
import com.antigravity.telemetry.core.designsystem.SlateTextMuted
import com.antigravity.telemetry.core.designsystem.SurfaceSubtle
import com.antigravity.telemetry.core.designsystem.SurfaceWhite
import com.antigravity.telemetry.core.designsystem.SwitchLavenderAccent
import com.antigravity.telemetry.core.designsystem.SwitchLavenderBadge
import com.antigravity.telemetry.core.designsystem.SwitchLavenderBg
import com.antigravity.telemetry.core.designsystem.SwitchLavenderBorder
import com.antigravity.telemetry.core.designsystem.components.OdometerUpdateSheet
import com.antigravity.telemetry.core.designsystem.components.PastelFilterChip
import com.antigravity.telemetry.core.model.EventType
import com.antigravity.telemetry.core.model.FuelEvent
import com.antigravity.telemetry.core.model.FuelType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelLedgerScreen(
    viewModel: FuelLedgerViewModel,
    onOpenSimulator: () -> Unit = {},
    onOpenOdometerSheet: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    var showLocalOdoSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Screen Context Header with "+ Update Odo" quick action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Fuel Ledger & Events",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextMain,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Sorted by odometer • Lifecycle timeline",
                            fontSize = 13.sp,
                            color = SlateTextMuted
                        )
                    }

                    // Quick Action: Log / Update Odometer
                    Row(
                        modifier = Modifier
                            .clip(Rounded2xl)
                            .background(SurfaceWhite)
                            .border(1.dp, SlateSoft, Rounded2xl)
                            .clickable {
                                if (onOpenOdometerSheet != null) {
                                    onOpenOdometerSheet()
                                } else {
                                    showLocalOdoSheet = true
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = SlateTextMain,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "+ Update Odo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextMain
                        )
                    }
                }
            }

            // Filter Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PastelFilterChip(
                        label = "All Events",
                        isSelected = state.selectedFilter == LedgerFilter.ALL,
                        count = state.allEvents.size,
                        onClick = { viewModel.setFilter(LedgerFilter.ALL) }
                    )
                    PastelFilterChip(
                        label = "CNG",
                        isSelected = state.selectedFilter == LedgerFilter.CNG,
                        count = state.cngCount,
                        indicatorColor = CngAccent,
                        onClick = { viewModel.setFilter(LedgerFilter.CNG) }
                    )
                    PastelFilterChip(
                        label = "Petrol",
                        isSelected = state.selectedFilter == LedgerFilter.PETROL,
                        count = state.petrolCount,
                        indicatorColor = PetrolAccent,
                        onClick = { viewModel.setFilter(LedgerFilter.PETROL) }
                    )
                    PastelFilterChip(
                        label = "Odometer",
                        isSelected = state.selectedFilter == LedgerFilter.ODOMETER,
                        count = state.odometerCount,
                        indicatorColor = SlateTextMuted,
                        onClick = { viewModel.setFilter(LedgerFilter.ODOMETER) }
                    )
                    PastelFilterChip(
                        label = "Switchovers",
                        isSelected = state.selectedFilter == LedgerFilter.SWITCH,
                        count = state.switchCount,
                        indicatorColor = SwitchLavenderAccent,
                        onClick = { viewModel.setFilter(LedgerFilter.SWITCH) }
                    )
                }
            }

            // Top Metrics Summary Bento Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, Rounded3xl, spotColor = Color(0x0A0F172A))
                        .clip(Rounded3xl)
                        .background(SurfaceWhite)
                        .border(1.dp, SlateSoft.copy(alpha = 0.8f), Rounded3xl)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 30D Fuel Ratio
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(Rounded2xl)
                            .background(CngPastelBg.copy(alpha = 0.6f))
                            .border(1.dp, CngPastelBorder.copy(alpha = 0.6f), Rounded2xl)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "30D FUEL RATIO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = CngAccent
                                )
                                Icon(Icons.Default.PieChart, null, tint = CngAccent, modifier = Modifier.size(15.dp))
                            }

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "${String.format("%.0f", state.cngRatioPercent)}%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CngAccent
                                )
                                Text(
                                    text = "CNG",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CngAccent,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${String.format("%.0f", state.petrolRatioPercent)}%",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PetrolAccent
                                )
                                Text(
                                    text = "PET",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PetrolAccent,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                )
                            }

                            // Dual progress bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceSubtle)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight((state.cngRatioPercent / 100f).toFloat().coerceAtLeast(0.01f))
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(CngAccent)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight((state.petrolRatioPercent / 100f).toFloat().coerceAtLeast(0.01f))
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(PetrolAccent)
                                )
                            }
                        }
                    }

                    // Net Running Cost
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(Rounded2xl)
                            .background(SurfaceSubtle)
                            .border(1.dp, SlateSoft, Rounded2xl)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "NET RUNNING COST",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = SlateTextMuted
                                )
                                Icon(Icons.Default.Payments, null, tint = SlateTextMuted, modifier = Modifier.size(15.dp))
                            }

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = if (state.netRunningCost > 0) "₹${String.format("%.2f", state.netRunningCost)}" else "--",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextMain
                                )
                                Text(
                                    text = "/ km",
                                    fontSize = 11.sp,
                                    color = SlateTextMuted,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }

                            if (state.netRunningCost > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Default.TrendingDown, null, tint = CngAccent, modifier = Modifier.size(13.dp))
                                    Text(
                                        text = "-62% vs Petrol pure",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CngAccent
                                    )
                                }
                            } else {
                                Text(
                                    text = "Awaiting data",
                                    fontSize = 10.sp,
                                    color = SlateTextFaint
                                )
                            }
                        }
                    }
                }
            }

            // Empty state if no events match filter
            if (state.filteredEvents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Rounded3xl)
                            .background(SurfaceWhite)
                            .border(1.dp, SlateSoft, Rounded3xl)
                            .padding(vertical = 36.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EvStation,
                                contentDescription = null,
                                tint = SlateTextFaint,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "No Events Found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextMain
                            )
                            Text(
                                text = "Logged refills, switches, and odometer updates will appear here.",
                                fontSize = 12.sp,
                                color = SlateTextMuted
                            )
                        }
                    }
                }
            }

            // Timeline Items (Sorted strictly by odometerKm DESC, then timestamp DESC)
            items(state.filteredEvents, key = { it.id }) { event ->
                TimelineEventCard(
                    event = event,
                    dateFormatter = dateFormatter,
                    onDelete = { viewModel.deleteEvent(event.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Local Odometer Update Sheet fallback if not hoisted
        if (showLocalOdoSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLocalOdoSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color.Transparent
            ) {
                OdometerUpdateSheet(
                    currentOdometerKm = state.odometerKm,
                    onConfirmOdometer = { newOdo ->
                        viewModel.logOdometerUpdate(newOdo)
                        showLocalOdoSheet = false
                    },
                    onDismiss = { showLocalOdoSheet = false }
                )
            }
        }
    }
}

/**
 * Visual styling and text content configuration for each of the 6 standardized event types.
 */
private data class EventCardContent(
    val title: String,
    val coloredLine: String,
    val subtitle: String,
    val accentColor: Color,
    val pastelBg: Color,
    val pastelBorder: Color,
    val icon: ImageVector,
    val badgeLabel: String? = null
)

@Composable
private fun getEventCardContent(event: FuelEvent): EventCardContent {
    return when {
        // 1. CNG Fill
        event.isCngRefill -> EventCardContent(
            title = "CNG Refill",
            coloredLine = "${String.format(Locale.US, "%.2f", event.quantity ?: 0.0)} kg • ₹${String.format(Locale.US, "%,.0f", event.totalCost ?: 0.0)}",
            subtitle = "${event.stationName ?: "CNG Station"} • ₹${String.format(Locale.US, "%.1f", event.pricePerUnit ?: 0.0)}/kg",
            accentColor = CngAccent,
            pastelBg = CngPastelBg,
            pastelBorder = CngPastelBorder,
            icon = Icons.Default.EvStation,
            badgeLabel = if (event.isFullTank) "Full Tank" else null
        )

        // 2. CNG Empty
        event.isCngEmpty -> EventCardContent(
            title = "CNG Exhausted",
            coloredLine = "Tank Exhausted • Switched to Petrol",
            subtitle = "${event.coldStartsSinceLastRefill} cold starts deducted (${String.format(Locale.US, "%.1f", event.coldStartsSinceLastRefill * 1.2)} km)",
            accentColor = CngAccent,
            pastelBg = CngPastelBg,
            pastelBorder = CngPastelBorder,
            icon = Icons.Default.Propane,
            badgeLabel = "Exhausted"
        )

        // 3. Petrol Fill
        event.isPetrolRefill -> EventCardContent(
            title = "Petrol Refill",
            coloredLine = "${String.format(Locale.US, "%.2f", event.quantity ?: 0.0)} L • ₹${String.format(Locale.US, "%,.0f", event.totalCost ?: 0.0)}",
            subtitle = "${event.stationName ?: "Petrol Pump"} • ₹${String.format(Locale.US, "%.1f", event.pricePerUnit ?: 0.0)}/L",
            accentColor = PetrolAccent,
            pastelBg = PetrolPastelBg,
            pastelBorder = PetrolPastelBorder,
            icon = Icons.Default.LocalGasStation,
            badgeLabel = if (event.isFullTank) "Full Tank" else null
        )

        // 4. Petrol Reserve
        event.isPetrolReserve -> EventCardContent(
            title = "Petrol Reserve",
            coloredLine = "Low Reserve Level Active",
            subtitle = "Refill recommended (~5L remaining in tank)",
            accentColor = PetrolAccent,
            pastelBg = PetrolPastelBg,
            pastelBorder = PetrolPastelBorder,
            icon = Icons.Default.WarningAmber,
            badgeLabel = "Reserve"
        )

        // 5. Odometer Update
        event.isOdometerUpdate -> EventCardContent(
            title = "Odometer Calibration",
            coloredLine = "${String.format(Locale.US, "%,.0f km", event.odometerKm)} Cluster Reading",
            subtitle = event.stationName ?: "Manual cluster calibration",
            accentColor = SlateTextMuted,
            pastelBg = SurfaceSubtle,
            pastelBorder = SlateSoft,
            icon = Icons.Default.Speed,
            badgeLabel = "Calibrated"
        )

        // 6. Manual Fuel Switch
        event.isManualFuelSwitch -> EventCardContent(
            title = "Manual Fuel Switch",
            coloredLine = "Switched to ${if (event.fuelType == FuelType.CNG) "CNG" else "Petrol"}",
            subtitle = "Manual fuel selector toggle",
            accentColor = SwitchLavenderAccent,
            pastelBg = SwitchLavenderBg,
            pastelBorder = SwitchLavenderBorder,
            icon = Icons.Default.SwapHoriz,
            badgeLabel = if (event.fuelType == FuelType.CNG) "CNG Active" else "Petrol Active"
        )

        else -> EventCardContent(
            title = "Vehicle Event",
            coloredLine = "${String.format(Locale.US, "%,.0f km", event.odometerKm)} recorded",
            subtitle = "System event log",
            accentColor = SlateTextMuted,
            pastelBg = SurfaceSubtle,
            pastelBorder = SlateSoft,
            icon = Icons.Default.Tune,
            badgeLabel = null
        )
    }
}

@Composable
private fun TimelineEventCard(
    event: FuelEvent,
    dateFormatter: SimpleDateFormat,
    onDelete: () -> Unit
) {
    val content = getEventCardContent(event)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Timeline node pin & vertical track
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite)
                    .border(3.dp, content.accentColor, CircleShape)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(78.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(content.accentColor.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )
        }

        // Event Card with Accent Color Border
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(2.dp, Rounded2xl, spotColor = Color(0x060F172A))
                .clip(Rounded2xl)
                .background(SurfaceWhite)
                .border(1.2.dp, content.accentColor, Rounded2xl)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left Column: Icon + Title, colored line description, subtitle description
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Icon + Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedSm)
                                .background(content.pastelBg)
                                .border(1.dp, content.pastelBorder, RoundedSm),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = content.icon,
                                contentDescription = null,
                                tint = content.accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Text(
                            text = content.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextMain
                        )

                        content.badgeLabel?.let { badge ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(content.pastelBg)
                                    .border(1.dp, content.pastelBorder, CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = content.accentColor
                                )
                            }
                        }
                    }

                    // Colored line description
                    Text(
                        text = content.coloredLine,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = content.accentColor
                    )

                    // Subtitle description
                    Text(
                        text = content.subtitle,
                        fontSize = 11.sp,
                        color = SlateTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Right Column: Date time 12h format, Odometer, Delete
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Date time 12h format
                    Text(
                        text = dateFormatter.format(Date(event.timestamp)),
                        fontSize = 11.sp,
                        color = SlateTextFaint,
                        fontWeight = FontWeight.Medium
                    )

                    // Odometer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = SlateTextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = String.format(Locale.US, "%,.0f km", event.odometerKm),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextMain
                        )
                    }

                    // Delete
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Event",
                            tint = SlateTextFaint,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

