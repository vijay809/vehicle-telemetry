package com.antigravity.telemetry.feature.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.antigravity.telemetry.core.designsystem.PetrolAccent
import com.antigravity.telemetry.core.designsystem.Rounded2xl
import com.antigravity.telemetry.core.designsystem.Rounded3xl
import com.antigravity.telemetry.core.designsystem.RoundedSm
import com.antigravity.telemetry.core.designsystem.SlateSoft
import com.antigravity.telemetry.core.designsystem.SlateTextMain
import com.antigravity.telemetry.core.designsystem.SlateTextMuted
import com.antigravity.telemetry.core.designsystem.SurfaceSubtle
import com.antigravity.telemetry.core.designsystem.SurfaceWhite
import com.antigravity.telemetry.core.designsystem.AlertAccent
import com.antigravity.telemetry.core.designsystem.AlertPastelBg
import com.antigravity.telemetry.core.designsystem.AlertPastelBorder
import com.antigravity.telemetry.core.telemetry.TelemetryManager

@Composable
fun SimulatorBottomSheet(
    telemetryManager: TelemetryManager,
    onAdvanceOdometer: (Double) -> Unit,
    onResetActualData: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDriving by remember { mutableStateOf(telemetryManager.isSimulating()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(Rounded3xl)
            .background(SurfaceWhite)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(SlateSoft)
                    .align(Alignment.CenterHorizontally)
            )

            // Header
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
                            .size(36.dp)
                            .clip(RoundedSm)
                            .background(SurfaceSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Tune, null, tint = CngAccent, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = "ECU & Telemetry Simulator",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextMain
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = SlateTextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Text(
                text = "Use these controls to test AntiGravity algorithms and Android Auto triggers without a connected car.",
                fontSize = 12.sp,
                color = SlateTextMuted
            )

            // Control 1: Drive simulation toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Rounded2xl)
                    .background(SurfaceSubtle)
                    .border(1.dp, SlateSoft, Rounded2xl)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Drive Simulation",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextMain
                    )
                    Text(
                        text = "Increments odometer +0.1 km every 1s",
                        fontSize = 11.sp,
                        color = SlateTextMuted
                    )
                }

                Switch(
                    checked = isDriving,
                    onCheckedChange = {
                        isDriving = it
                        if (it) telemetryManager.startDriveSimulation() else telemetryManager.stopDriveSimulation()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CngAccent
                    )
                )
            }

            // Control 2: Stationary Refill Trigger
            Button(
                onClick = {
                    telemetryManager.triggerStationaryRefillSimulation(25.0)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = PetrolAccent)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.EvStation, null)
                    Text(
                        text = "Simulate Stationary Refill (+25%)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Control 3: Quick Odometer Advance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onAdvanceOdometer(10.0) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = CircleShape
                ) {
                    Text("+10 km", fontWeight = FontWeight.Bold, color = SlateTextMain)
                }
                OutlinedButton(
                    onClick = { onAdvanceOdometer(100.0) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = CircleShape
                ) {
                    Text("+100 km", fontWeight = FontWeight.Bold, color = SlateTextMain)
                }
            }

            // Control 4: Clean Slate / Reset Actual Data
            OutlinedButton(
                onClick = {
                    onResetActualData()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AlertAccent
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, AlertPastelBorder)
            ) {
                Text(
                    text = "Reset Actual Data (Clean Slate)",
                    fontWeight = FontWeight.Bold,
                    color = AlertAccent,
                    fontSize = 13.sp
                )
            }
        }
    }
}
