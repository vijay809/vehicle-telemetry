package com.antigravity.telemetry.feature.cngempty

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Propane
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.antigravity.telemetry.core.designsystem.components.ColdStartStepper

@Composable
fun CngEmptyTriggerScreen(
    viewModel: CngEmptyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedSm)
                            .background(SurfaceWhite)
                            .border(1.dp, SlateSoft, RoundedSm)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SlateTextMain)
                    }

                    Text(
                        text = "CNG Empty Alert",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextMain
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CngBadge),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = CngAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status preamble badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PetrolPastelBg)
                        .border(1.dp, PetrolPastelBorder, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PetrolAccent)
                    )
                    Text(
                        text = "TELEMETRY LIVE • ANDROID AUTO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = PetrolAccent
                    )
                }

                Text(
                    text = "14:02 IST",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlateTextMuted
                )
            }

            // Main Calming Safety Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, Rounded3xl, spotColor = Color(0x0F0F172A))
                    .clip(Rounded3xl)
                    .background(SurfaceWhite)
                    .border(1.dp, SlateSoft.copy(alpha = 0.8f), Rounded3xl)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Alert Header Block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedSm)
                                .background(PetrolPastelBg)
                                .border(1.dp, PetrolPastelBorder, RoundedSm),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Propane,
                                contentDescription = null,
                                tint = PetrolAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(PetrolBadge)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "COCKPIT ALERT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp,
                                    color = PetrolAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Mark CNG Exhausted",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextMain
                            )
                            Text(
                                text = "Vehicle automatically switched or switching to Petrol mode.",
                                fontSize = 12.sp,
                                color = SlateTextMuted,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Verified Odometer Tile
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Rounded2xl)
                            .background(SurfaceSubtle)
                            .border(1.dp, SlateSoft, Rounded2xl)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        imageVector = Icons.Default.Sensors,
                                        contentDescription = null,
                                        tint = CngAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "SYNC ODOMETER",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = SlateTextMuted
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(CngBadge)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Android Auto Verified",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CngAccent
                                    )
                                }
                            }

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
                                        text = String.format("%,.0f", state.odometerKm),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SlateTextMain
                                    )
                                    Text(
                                        text = "KM",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateTextFaint,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(PetrolPastelBg)
                                        .border(1.dp, PetrolPastelBorder, CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = null,
                                        tint = PetrolAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "+${String.format("%.0f", state.cngTripKm)} km on CNG",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PetrolAccent
                                    )
                                }
                            }
                        }
                    }

                    // Cold-Start Compensation Stepper
                    ColdStartStepper(
                        count = state.coldStartsCount,
                        warmupCoeffKm = state.warmupCoeffKm,
                        onIncrement = { viewModel.incrementColdStarts() },
                        onDecrement = { viewModel.decrementColdStarts() }
                    )

                    // Quick Telemetry Snapshot Rail
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // CNG Pressure
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(Rounded2xl)
                                .background(PetrolPastelBg)
                                .border(1.dp, PetrolPastelBorder, Rounded2xl)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "CNG PRESSURE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = PetrolAccent
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = String.format("%.1f", state.cngPressureBar),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PetrolAccent
                                    )
                                    Text(
                                        text = "bar",
                                        fontSize = 11.sp,
                                        color = SlateTextMuted,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }
                        }

                        // Petrol Reserve
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(Rounded2xl)
                                .background(CngPastelBg)
                                .border(1.dp, CngPastelBorder, Rounded2xl)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "PETROL RESERVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = CngAccent
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%.0f", state.petrolReservePercent)}%",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CngAccent
                                    )
                                    Text(
                                        text = "remaining",
                                        fontSize = 11.sp,
                                        color = SlateTextMuted,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Primary Action Button
                    Button(
                        onClick = { viewModel.confirmCngEmpty(onSuccess = onBack) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0x30D97706)),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PetrolAccent)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Text(
                                text = "Confirm CNG Ran Out",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
