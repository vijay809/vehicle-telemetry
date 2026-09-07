package com.antigravity.telemetry.feature.diagnostics

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.telemetry.core.designsystem.AlertAccent
import com.antigravity.telemetry.core.designsystem.AlertPastelBorder
import com.antigravity.telemetry.core.designsystem.CanvasLavender
import com.antigravity.telemetry.core.designsystem.CngAccent
import com.antigravity.telemetry.core.designsystem.CngBadge
import com.antigravity.telemetry.core.designsystem.CngPastelBg
import com.antigravity.telemetry.core.designsystem.CngPastelBorder
import com.antigravity.telemetry.core.designsystem.Rounded2xl
import com.antigravity.telemetry.core.designsystem.Rounded3xl
import com.antigravity.telemetry.core.designsystem.RoundedSm
import com.antigravity.telemetry.core.designsystem.SlateSoft
import com.antigravity.telemetry.core.designsystem.SlateTextFaint
import com.antigravity.telemetry.core.designsystem.SlateTextMain
import com.antigravity.telemetry.core.designsystem.SlateTextMuted
import com.antigravity.telemetry.core.designsystem.SurfaceWhite
import com.antigravity.telemetry.core.model.HardwareStatus
import com.antigravity.telemetry.core.model.VehicleHardwareState
import com.antigravity.telemetry.core.telemetry.AutoTelemetryLogger
import java.util.Locale

@Composable
fun AutoDiagnosticsBottomSheet(
    hardwareState: VehicleHardwareState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isConnected = hardwareState.isConnected

    var logSizeText by remember { mutableStateOf(AutoTelemetryLogger.getLogSizeFormatted(context)) }
    var recentLogs by remember { mutableStateOf(AutoTelemetryLogger.getRecentLogs()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(Rounded3xl)
            .background(SurfaceWhite)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(SlateSoft)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Header Row
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) CngPastelBg else Color(0xFFF1F5F9))
                            .border(1.dp, if (isConnected) CngPastelBorder else SlateSoft, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = if (isConnected) CngAccent else SlateTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Vehicle Telemetry Grid",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextMain
                        )
                        Text(
                            text = if (isConnected) "Android Auto Live Connected" else "Android Auto Disconnected",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isConnected) CngAccent else SlateTextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SlateTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Connection & Settings Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { launchAndroidAutoSettings(context) },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedSm,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateSoft)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = SlateTextMain,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Auto Settings",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextMain
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedSm)
                        .background(if (isConnected) CngPastelBg else Color(0xFFF1F5F9))
                        .border(1.dp, if (isConnected) CngPastelBorder else SlateSoft, RoundedSm)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isConnected) CngAccent else Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isConnected) "Host: PROJECTION" else "No Car Host",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) CngAccent else SlateTextMain
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 1: ALL VEHICLE DATA GRID
            Text(
                text = "ALL VEHICLE HARDWARE TELEMETRY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextFaint,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Grid Row 1: Make/Model & Fuel Profile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryDataCard(
                    modifier = Modifier.weight(1f),
                    label = "VEHICLE MODEL",
                    value = listOfNotNull(
                        hardwareState.manufacturer?.takeIf { it.isNotBlank() },
                        hardwareState.modelName?.takeIf { it.isNotBlank() },
                        hardwareState.modelYear?.toString()
                    ).joinToString(" ").ifBlank { "Maruti Suzuki Vitara" },
                    status = hardwareState.modelStatus
                )

                TelemetryDataCard(
                    modifier = Modifier.weight(1f),
                    label = "FUEL PROFILE",
                    value = if (hardwareState.fuelTypes.isNotEmpty()) {
                        hardwareState.fuelTypes.joinToString(", ")
                    } else {
                        "CNG, Petrol"
                    },
                    status = hardwareState.profileStatus
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid Row 2: Odometer & Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryDataCard(
                    modifier = Modifier.weight(1f),
                    label = "ODOMETER",
                    value = hardwareState.odometerKm?.let {
                        String.format(Locale.US, "%,.1f km", it)
                    } ?: "--",
                    subtitle = hardwareState.odometerMetersRaw?.let { "Raw: ${it.toInt()} m" },
                    status = hardwareState.mileageStatus
                )

                TelemetryDataCard(
                    modifier = Modifier.weight(1f),
                    label = "SPEED",
                    value = hardwareState.speedKmh?.let {
                        String.format(Locale.US, "%.0f km/h", it)
                    } ?: "--",
                    subtitle = hardwareState.speedMpsRaw?.let { String.format(Locale.US, "%.1f m/s", it) },
                    status = hardwareState.speedStatus
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid Row 3: Fuel Level & Range Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryDataCard(
                    modifier = Modifier.weight(1f),
                    label = "FUEL LEVEL",
                    value = hardwareState.fuelPercent?.let {
                        String.format(Locale.US, "%.0f%%", it)
                    } ?: "--",
                    subtitle = if (hardwareState.isLowFuel == true) "WARNING: Low Fuel" else "Level Normal",
                    status = hardwareState.energyStatus
                )

                TelemetryDataCard(
                    modifier = Modifier.weight(1f),
                    label = "RANGE REMAINING",
                    value = hardwareState.rangeRemainingKm?.let {
                        String.format(Locale.US, "%.0f km", it)
                    } ?: "--",
                    subtitle = hardwareState.batteryPercent?.let { "EV Batt: ${it.toInt()}%" },
                    status = hardwareState.energyStatus
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid Row 4: Compass & Hardware GPS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryDataCard(
                    modifier = Modifier.weight(1f),
                    label = "COMPASS ORIENTATION",
                    value = hardwareState.compassYaw?.let {
                        String.format(Locale.US, "%.1f°", it)
                    } ?: "--",
                    subtitle = if (hardwareState.compassPitch != null) {
                        "Pitch: ${hardwareState.compassPitch.toInt()}° Roll: ${hardwareState.compassRoll?.toInt()}°"
                    } else null,
                    status = hardwareState.compassStatus
                )

                TelemetryDataCard(
                    modifier = Modifier.weight(1f),
                    label = "VEHICLE HARDWARE GPS",
                    value = if (hardwareState.latitude != null && hardwareState.longitude != null) {
                        String.format(Locale.US, "%.4f, %.4f", hardwareState.latitude, hardwareState.longitude)
                    } else "--",
                    subtitle = hardwareState.altitude?.let { String.format(Locale.US, "Alt: %.0f m", it) },
                    status = hardwareState.locationStatus
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid Row 5: Accelerometer & Gyroscope
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryDataCard(
                    modifier = Modifier.weight(1f),
                    label = "ACCELEROMETER",
                    value = if (hardwareState.accelX != null) {
                        String.format(Locale.US, "X:%.1f Y:%.1f", hardwareState.accelX, hardwareState.accelY ?: 0f)
                    } else "--",
                    subtitle = hardwareState.accelZ?.let { String.format(Locale.US, "Z: %.1f m/s²", it) },
                    status = hardwareState.accelStatus
                )

                TelemetryDataCard(
                    modifier = Modifier.weight(1f),
                    label = "GYROSCOPE",
                    value = if (hardwareState.gyroX != null) {
                        String.format(Locale.US, "X:%.2f Y:%.2f", hardwareState.gyroX, hardwareState.gyroY ?: 0f)
                    } else "--",
                    subtitle = hardwareState.gyroZ?.let { String.format(Locale.US, "Z: %.2f rad/s", it) },
                    status = hardwareState.gyroStatus
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid Row 6: Toll Card & EV Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryDataCard(
                    modifier = Modifier.weight(1f),
                    label = "TOLL CARD STATE",
                    value = hardwareState.tollCardState ?: "--",
                    status = hardwareState.tollStatus
                )

                TelemetryDataCard(
                    modifier = Modifier.weight(1f),
                    label = "EV CHARGE PORT",
                    value = if (hardwareState.evPortOpen != null) {
                        if (hardwareState.evPortOpen == true) "PORT OPEN" else "CLOSED"
                    } else "--",
                    subtitle = if (hardwareState.evPortConnected == true) "Cable Plugged" else null,
                    status = hardwareState.evStatus
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 2: STREAM LOGGING & EMAIL EXPORT
            Text(
                text = "STREAM TELEMETRY LOG & EMAIL EXPORT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextFaint,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Rounded2xl)
                    .background(CanvasLavender)
                    .border(1.dp, SlateSoft, Rounded2xl)
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "auto_telemetry_stream.log",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SlateTextMain,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Persistent streaming log file • Size: $logSizeText",
                                fontSize = 11.sp,
                                color = SlateTextMuted
                            )
                        }

                        IconButton(
                            onClick = {
                                AutoTelemetryLogger.clearLog(context)
                                logSizeText = AutoTelemetryLogger.getLogSizeFormatted(context)
                                recentLogs = AutoTelemetryLogger.getRecentLogs()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear Log",
                                tint = AlertAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Live Log Feed Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedSm)
                            .background(Color(0xFF0F172A))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (recentLogs.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                recentLogs.takeLast(6).forEach { line ->
                                    Text(
                                        text = line,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.5.sp,
                                        color = if (line.contains("[ERROR]")) Color(0xFFF87171) else Color(0xFF34D399)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Awaiting streaming events from vehicle...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Primary Action: Email / Share Log File
                    Button(
                        onClick = {
                            AutoTelemetryLogger.shareLogFile(context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedSm,
                        colors = ButtonDefaults.buttonColors(containerColor = CngAccent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Email / Share Telemetry Log",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // First-Time Android Auto Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Rounded2xl)
                    .background(Color(0xFFFEF3C7).copy(alpha = 0.5f))
                    .border(1.dp, Color(0xFFFDE68A), Rounded2xl)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(18.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "If Vehicle Telemetry is not showing on car display:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = "Tap 'Auto Settings' above → tap 'Version' 10 times → 3-dots menu → Developer settings → check 'Unknown sources'.",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryDataCard(
    label: String,
    value: String,
    status: HardwareStatus,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val (badgeBg, badgeBorder, badgeText) = when (status) {
        HardwareStatus.SUCCESS -> Triple(CngPastelBg, CngPastelBorder, CngAccent)
        HardwareStatus.UNSUPPORTED_BY_CAR -> Triple(Color(0xFFFEF3C7), Color(0xFFFDE68A), Color(0xFFB45309))
        HardwareStatus.UNAVAILABLE -> Triple(Color(0xFFFEE2E2), Color(0xFFFECACA), AlertAccent)
        HardwareStatus.AWAITING -> Triple(Color(0xFFF1F5F9), SlateSoft, SlateTextMuted)
    }

    Box(
        modifier = modifier
            .clip(RoundedSm)
            .background(CanvasLavender)
            .border(1.dp, SlateSoft, RoundedSm)
            .padding(9.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextFaint,
                    letterSpacing = 0.4.sp
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(badgeBg)
                        .border(1.dp, badgeBorder, CircleShape)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = status.label,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeText
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (value == "--") SlateTextMuted else SlateTextMain
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateTextMuted
                )
            }
        }
    }
}

private fun launchAndroidAutoSettings(context: Context) {
    val candidateIntents = listOf(
        Intent("com.google.android.gms.car.SETTINGS"),
        Intent().apply {
            setClassName(
                "com.google.android.projection.gearhead",
                "com.google.android.projection.gearhead.companion.settings.DefaultSettingsActivity"
            )
        },
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", "com.google.android.projection.gearhead", null)
        }
    )

    for (intent in candidateIntents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        } catch (_: Exception) {}
    }

    try {
        context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (_: Exception) {}
}
