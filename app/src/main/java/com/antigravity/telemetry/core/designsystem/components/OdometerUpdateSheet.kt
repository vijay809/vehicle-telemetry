package com.antigravity.telemetry.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.telemetry.core.designsystem.CngAccent
import com.antigravity.telemetry.core.designsystem.Rounded2xl
import com.antigravity.telemetry.core.designsystem.Rounded3xl
import com.antigravity.telemetry.core.designsystem.RoundedSm
import com.antigravity.telemetry.core.designsystem.SlateSoft
import com.antigravity.telemetry.core.designsystem.SlateTextFaint
import com.antigravity.telemetry.core.designsystem.SlateTextMain
import com.antigravity.telemetry.core.designsystem.SlateTextMuted
import com.antigravity.telemetry.core.designsystem.SurfaceSubtle
import com.antigravity.telemetry.core.designsystem.SurfaceWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun OdometerUpdateSheet(
    currentOdometerKm: Double,
    onConfirmOdometer: (Double) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var odoValue by remember(currentOdometerKm) {
        mutableDoubleStateOf(if (currentOdometerKm > 0) currentOdometerKm else 9403.0)
    }
    var inputText by remember(odoValue) {
        mutableStateOf(String.format(Locale.US, "%.0f", odoValue))
    }

    val deltaKm = odoValue - currentOdometerKm
    val timeFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val currentTimeString = remember { timeFormatter.format(Date()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(Rounded3xl)
            .background(SurfaceWhite)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            .clip(RoundedSm)
                            .background(SurfaceSubtle)
                            .border(1.dp, SlateSoft, RoundedSm),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = SlateTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Update Odometer",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextMain
                        )
                        Text(
                            text = currentTimeString,
                            fontSize = 11.sp,
                            color = SlateTextFaint
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
                        tint = SlateTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Current Reading vs Delta Summary Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Rounded2xl)
                    .background(SurfaceSubtle)
                    .border(1.dp, SlateSoft.copy(alpha = 0.6f), Rounded2xl)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PREVIOUS READING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextFaint,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${String.format(Locale.US, "%,.0f", currentOdometerKm)} km",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateTextMain
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "DELTA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextFaint,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = when {
                                deltaKm > 0 -> "+${String.format(Locale.US, "%.0f", deltaKm)} km"
                                deltaKm < 0 -> "${String.format(Locale.US, "%.0f", deltaKm)} km"
                                else -> "No change"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (deltaKm >= 0) CngAccent else SlateTextMuted
                        )
                    }
                }
            }

            // Big Number Text Field
            OutlinedTextField(
                value = inputText,
                onValueChange = { newVal ->
                    val clean = newVal.filter { it.isDigit() }
                    inputText = clean
                    clean.toDoubleOrNull()?.let { odoValue = it }
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = SlateTextMain
                ),
                suffix = {
                    Text("km", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SlateTextMuted)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = Rounded2xl,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CngAccent,
                    unfocusedBorderColor = SlateSoft,
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite
                )
            )

            // Tactile Quick Step Pills Row
            Text(
                text = "QUICK ADJUSTMENT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = SlateTextFaint
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(-100, -10, -1, 1, 10, 100).forEach { step ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(SurfaceSubtle)
                            .border(1.dp, SlateSoft, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                val next = (odoValue + step).coerceAtLeast(0.0)
                                odoValue = next
                                inputText = String.format(Locale.US, "%.0f", next)
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (step > 0) "+$step" else "$step",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (step > 0) CngAccent else SlateTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Confirm CTA Button
            Button(
                onClick = {
                    if (odoValue > 0) {
                        onConfirmOdometer(odoValue)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CngAccent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Save Odometer (${String.format(Locale.US, "%,.0f", odoValue)} km)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
