package com.antigravity.telemetry.feature.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Propane
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.antigravity.telemetry.core.designsystem.AlertAccent
import com.antigravity.telemetry.core.designsystem.AlertPastelBg
import com.antigravity.telemetry.core.designsystem.AlertPastelBorder
import com.antigravity.telemetry.core.designsystem.CanvasLavender
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
import com.antigravity.telemetry.core.designsystem.components.PastelFilterChip
import com.antigravity.telemetry.core.model.EventType
import com.antigravity.telemetry.core.model.FuelEvent
import com.antigravity.telemetry.core.model.FuelType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FuelLedgerScreen(
    viewModel: FuelLedgerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CanvasLavender,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedSm)
                            .background(SurfaceWhite)
                            .border(1.dp, SlateSoft, RoundedSm)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SlateTextMain)
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedSm)
                            .background(CngPastelBg)
                            .border(1.dp, CngPastelBorder, RoundedSm),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = CngAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Victoris CNG",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextMain
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(CngAccent)
                            )
                            Text(
                                text = "Synced via Android Auto",
                                fontSize = 11.sp,
                                color = CngAccent
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .border(1.dp, SlateSoft, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = SlateTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = String.format("%,.0f", state.odometerKm),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextMain
                    )
                    Text(
                        text = "km",
                        fontSize = 10.sp,
                        color = SlateTextFaint
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Screen Context Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fuel Ledger & Events",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextMain,
                            letterSpacing = (-0.5).sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CngPastelBg)
                                .border(1.dp, CngPastelBorder, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "OBD-II Verified",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CngAccent
                            )
                        }
                    }
                    Text(
                        text = "Historical fill-ups, switchovers & calculated efficiencies",
                        fontSize = 13.sp,
                        color = SlateTextMuted
                    )
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
                        label = "CNG Refills",
                        isSelected = state.selectedFilter == LedgerFilter.CNG,
                        count = state.cngCount,
                        indicatorColor = CngAccent,
                        onClick = { viewModel.setFilter(LedgerFilter.CNG) }
                    )
                    PastelFilterChip(
                        label = "Petrol Refills",
                        isSelected = state.selectedFilter == LedgerFilter.PETROL,
                        count = state.petrolCount,
                        indicatorColor = PetrolAccent,
                        onClick = { viewModel.setFilter(LedgerFilter.PETROL) }
                    )
                    PastelFilterChip(
                        label = "Switchovers",
                        isSelected = state.selectedFilter == LedgerFilter.SWITCHOVER,
                        count = state.switchCount,
                        indicatorColor = AlertAccent,
                        onClick = { viewModel.setFilter(LedgerFilter.SWITCHOVER) }
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

            // Empty state if no events exist
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
                                text = "No Events Logged Yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextMain
                            )
                            Text(
                                text = "Refill events and CNG switchovers will appear here.",
                                fontSize = 12.sp,
                                color = SlateTextMuted
                            )
                        }
                    }
                }
            }

            // Timeline Items
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
    }
}

@Composable
private fun TimelineEventCard(
    event: FuelEvent,
    dateFormatter: SimpleDateFormat,
    onDelete: () -> Unit
) {
    val isCng = event.fuelType == FuelType.CNG
    val isPetrol = event.fuelType == FuelType.PETROL
    val isSwitch = event.type == EventType.CNG_EMPTY

    val nodeColor = when {
        isCng -> CngAccent
        isPetrol -> PetrolAccent
        else -> AlertAccent
    }

    val icon = when {
        isCng -> Icons.Default.EvStation
        isPetrol -> Icons.Default.LocalGasStation
        else -> Icons.Default.Propane
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeline node pin
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite)
                    .border(3.dp, nodeColor, CircleShape)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(90.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(nodeColor.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )
        }

        // Event Card
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(2.dp, Rounded2xl, spotColor = Color(0x060F172A))
                .clip(Rounded2xl)
                .background(SurfaceWhite)
                .border(1.dp, SlateSoft.copy(alpha = 0.8f), Rounded2xl)
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Top row: Type + Timestamp
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
                                .size(24.dp)
                                .clip(RoundedSm)
                                .background(
                                    when {
                                        isCng -> CngPastelBg
                                        isPetrol -> PetrolPastelBg
                                        else -> AlertPastelBg
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = nodeColor, modifier = Modifier.size(14.dp))
                        }

                        Text(
                            text = when {
                                isCng -> "CNG Refill"
                                isPetrol -> "Petrol Refill"
                                isSwitch -> "CNG Exhausted"
                                else -> "Event"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextMain
                        )
                    }

                    Text(
                        text = dateFormatter.format(Date(event.timestamp)),
                        fontSize = 11.sp,
                        color = SlateTextFaint
                    )
                }

                // Middle: Quantity, Cost & Odometer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (event.quantity != null && event.totalCost != null) {
                        Column {
                            Text(
                                text = "${String.format("%.2f", event.quantity)} ${if (isCng) "kg" else "L"}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextMain
                            )
                            Text(
                                text = event.stationName ?: "Fuel Station",
                                fontSize = 11.sp,
                                color = SlateTextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${String.format("%.0f", event.totalCost)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = nodeColor
                            )
                            Text(
                                text = "₹${String.format("%.1f", event.pricePerUnit ?: 0.0)}/${if (isCng) "kg" else "L"}",
                                fontSize = 11.sp,
                                color = SlateTextFaint
                            )
                        }
                    } else if (isSwitch) {
                        Column {
                            Text(
                                text = "Auto-switched to Petrol",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AlertAccent
                            )
                            Text(
                                text = "${event.coldStartsSinceLastRefill} cold starts deducted",
                                fontSize = 11.sp,
                                color = SlateTextMuted
                            )
                        }
                    }
                }

                // Bottom row: Odometer & Delete
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Odo: ${String.format("%,.0f", event.odometerKm)} km",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateTextFaint
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = SlateTextFaint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
