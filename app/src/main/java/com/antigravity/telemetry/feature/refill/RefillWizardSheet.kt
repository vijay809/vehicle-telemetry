package com.antigravity.telemetry.feature.refill

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Propane
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.antigravity.telemetry.core.model.FuelType

@Composable
fun RefillWizardSheet(
    viewModel: RefillViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showOdoDialog by remember { mutableStateOf(false) }
    var tempOdoInput by remember { mutableStateOf(state.odometerKm.toString()) }

    if (showOdoDialog) {
        AlertDialog(
            onDismissRequest = { showOdoDialog = false },
            title = { Text("Edit Odometer Reading", fontWeight = FontWeight.Bold) },
            text = {
                BasicTextField(
                    value = tempOdoInput,
                    onValueChange = { tempOdoInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = SlateTextMain),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedSm)
                        .background(SurfaceSubtle)
                        .padding(12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = tempOdoInput.toDoubleOrNull()
                        if (parsed != null) viewModel.setOdometer(parsed)
                        showOdoDialog = false
                    }
                ) {
                    Text("Confirm", color = CngAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOdoDialog = false }) {
                    Text("Cancel", color = SlateTextMuted)
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(Rounded3xl)
            .background(SurfaceWhite)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedSm)
                            .background(CngPastelBg)
                            .border(1.dp, CngPastelBorder, RoundedSm),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = CngAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Log Fuel Refill",
                            fontSize = 20.sp,
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
                                text = "Ready for telemetry sync",
                                fontSize = 11.sp,
                                color = SlateTextMuted
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceSubtle)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SlateTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Two button row: Left button dismiss (Secondary), right button save (Primary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateTextMuted),
                    border = BorderStroke(1.dp, SlateSoft)
                ) {
                    Text(
                        text = "Dismiss",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextMuted
                    )
                }

                val primaryColor = if (state.fuelType == FuelType.CNG) CngAccent else PetrolAccent
                Button(
                    onClick = { viewModel.saveRefill(onSuccess = onDismiss) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .shadow(4.dp, CircleShape, spotColor = primaryColor.copy(alpha = 0.3f)),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Text(
                            text = "Save",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Numeric Input Grid: Dispensed & Total Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dispensed
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(Rounded2xl)
                        .background(SurfaceSubtle)
                        .border(1.dp, SlateSoft, Rounded2xl)
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DISPENSED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = SlateTextMuted
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (state.fuelType == FuelType.CNG) CngBadge else PetrolBadge)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (state.fuelType == FuelType.CNG) "KG" else "L",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.fuelType == FuelType.CNG) CngAccent else PetrolAccent
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (state.quantityText.isEmpty()) {
                                    Text(
                                        text = "0.00",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateTextFaint
                                    )
                                }
                                BasicTextField(
                                    value = state.quantityText,
                                    onValueChange = { viewModel.onQuantityChange(it) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    textStyle = TextStyle(
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateTextMain
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Text(
                                text = if (state.fuelType == FuelType.CNG) "kg" else "L",
                                fontSize = 12.sp,
                                color = SlateTextMuted,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }

                        // Stepper (- / + 0.5)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(RoundedSm)
                                    .background(SurfaceWhite)
                                    .border(1.dp, SlateSoft, RoundedSm)
                                    .clickable { viewModel.stepQuantity(-0.5) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Remove, null, tint = SlateTextMain, modifier = Modifier.size(16.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(RoundedSm)
                                    .background(SurfaceWhite)
                                    .border(1.dp, SlateSoft, RoundedSm)
                                    .clickable { viewModel.stepQuantity(0.5) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, null, tint = SlateTextMain, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Total Cost
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(Rounded2xl)
                        .background(SurfaceSubtle)
                        .border(1.dp, SlateSoft, Rounded2xl)
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL COST",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = SlateTextMuted
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(PetrolBadge)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "INR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PetrolAccent
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PetrolAccent,
                                modifier = Modifier.padding(bottom = 3.dp, end = 2.dp)
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                if (state.totalCostText.isEmpty()) {
                                    Text(
                                        text = "0",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateTextFaint
                                    )
                                }
                                BasicTextField(
                                    value = state.totalCostText,
                                    onValueChange = { viewModel.onTotalCostChange(it) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    textStyle = TextStyle(
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateTextMain
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Quick Chips (+100, +500)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(RoundedSm)
                                    .background(SurfaceWhite)
                                    .border(1.dp, SlateSoft, RoundedSm)
                                    .clickable { viewModel.addCost(100.0) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+100", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateTextMain)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(RoundedSm)
                                    .background(SurfaceWhite)
                                    .border(1.dp, SlateSoft, RoundedSm)
                                    .clickable { viewModel.addCost(500.0) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+500", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateTextMain)
                            }
                        }
                    }
                }
            }

            // Numeric Input Grid Row 2: Unit Rate & Full Tank Auto-Cut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 3: Unit Rate
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(Rounded2xl)
                        .background(SurfaceSubtle)
                        .border(1.dp, SlateSoft, Rounded2xl)
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "UNIT RATE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = SlateTextMuted
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (state.fuelType == FuelType.CNG) CngBadge else PetrolBadge)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (state.fuelType == FuelType.CNG) "₹/KG" else "₹/L",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.fuelType == FuelType.CNG) CngAccent else PetrolAccent
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.fuelType == FuelType.CNG) CngAccent else PetrolAccent,
                                modifier = Modifier.padding(bottom = 3.dp, end = 2.dp)
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                if (state.unitRateText.isEmpty()) {
                                    Text(
                                        text = "0.00",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateTextFaint
                                    )
                                }
                                BasicTextField(
                                    value = state.unitRateText,
                                    onValueChange = { viewModel.onRateChange(it) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    textStyle = TextStyle(
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateTextMain
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Stepper (-1 / +1)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(RoundedSm)
                                    .background(SurfaceWhite)
                                    .border(1.dp, SlateSoft, RoundedSm)
                                    .clickable { viewModel.stepRate(-1.0) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Remove, null, tint = SlateTextMain, modifier = Modifier.size(16.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(RoundedSm)
                                    .background(SurfaceWhite)
                                    .border(1.dp, SlateSoft, RoundedSm)
                                    .clickable { viewModel.stepRate(1.0) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, null, tint = SlateTextMain, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Card 4: Full Tank Auto-Cut Toggle Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(Rounded2xl)
                        .background(SurfaceSubtle)
                        .border(
                            1.dp,
                            if (state.isFullTank) (if (state.fuelType == FuelType.CNG) CngAccent.copy(alpha = 0.4f) else PetrolAccent.copy(alpha = 0.4f)) else SlateSoft,
                            Rounded2xl
                        )
                        .clickable { viewModel.toggleFullTank() }
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AUTO-CUT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = SlateTextMuted
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (state.isFullTank) (if (state.fuelType == FuelType.CNG) CngBadge else PetrolBadge) else SlateSoft
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (state.isFullTank) "ON" else "OFF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.isFullTank) (if (state.fuelType == FuelType.CNG) CngAccent else PetrolAccent) else SlateTextMuted
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (state.isFullTank) "Full Tank" else "Partial Fill",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextMain
                                )
                                Text(
                                    text = if (state.isFullTank) "Auto-shutoff" else "Manual fill",
                                    fontSize = 11.sp,
                                    color = SlateTextMuted
                                )
                            }

                            Switch(
                                checked = state.isFullTank,
                                onCheckedChange = { viewModel.toggleFullTank() },
                                thumbContent = if (state.isFullTank) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(12.dp), tint = if (state.fuelType == FuelType.CNG) CngAccent else PetrolAccent) }
                                } else null,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = if (state.fuelType == FuelType.CNG) CngAccent else PetrolAccent
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedSm)
                                .background(
                                    if (state.isFullTank) (if (state.fuelType == FuelType.CNG) CngPastelBg else PetrolPastelBg) else SurfaceWhite
                                )
                                .border(
                                    1.dp,
                                    if (state.isFullTank) (if (state.fuelType == FuelType.CNG) CngPastelBorder else PetrolPastelBorder) else SlateSoft,
                                    RoundedSm
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (state.isFullTank) "Precision Tank-to-Tank" else "Tap to enable Auto-Cut",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.isFullTank) (if (state.fuelType == FuelType.CNG) CngAccent else PetrolAccent) else SlateTextMuted
                            )
                        }
                    }
                }
            }

            // Dual Fuel Pill Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(SurfaceSubtle)
                    .border(1.dp, SlateSoft, CircleShape)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // CNG Button
                val isCng = state.fuelType == FuelType.CNG
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(if (isCng) CngAccent else Color.Transparent)
                        .clickable { viewModel.setFuelType(FuelType.CNG) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Propane,
                            contentDescription = null,
                            tint = if (isCng) Color.White else SlateTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "CNG (KG)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCng) Color.White else SlateTextMuted
                        )
                    }
                }

                // Petrol Button
                val isPet = state.fuelType == FuelType.PETROL
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(if (isPet) PetrolAccent else Color.Transparent)
                        .clickable { viewModel.setFuelType(FuelType.PETROL) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = if (isPet) Color.White else SlateTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Petrol (L)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPet) Color.White else SlateTextMuted
                        )
                    }
                }
            }

            // Live Odometer Telemetry Card
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedSm)
                            .background(SurfaceWhite)
                            .border(1.dp, SlateSoft, RoundedSm),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SyncAlt,
                            contentDescription = null,
                            tint = CngAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "ODOMETER READING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = SlateTextMuted
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(CngBadge)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = CngAccent,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Live OBD",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CngAccent
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = String.format("%,.0f", state.odometerKm),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextMain
                            )
                            Text(
                                text = "KM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextFaint,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .border(1.dp, SlateSoft, CircleShape)
                        .clickable {
                            tempOdoInput = state.odometerKm.toString()
                            showOdoDialog = true
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Edit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextMain
                    )
                }
            }

            // Detected Station Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Rounded2xl)
                    .background(SurfaceSubtle)
                    .border(1.dp, SlateSoft, Rounded2xl)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedSm)
                        .background(SurfaceWhite)
                        .border(1.dp, SlateSoft, RoundedSm),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = CngAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DETECTED STATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = SlateTextMuted
                    )
                    BasicTextField(
                        value = state.stationName,
                        onValueChange = { viewModel.setStationName(it) },
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateTextMain
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Noida Metro Corridor",
                        fontSize = 11.sp,
                        color = SlateTextFaint
                    )
                }
            }
        }
    }
}
