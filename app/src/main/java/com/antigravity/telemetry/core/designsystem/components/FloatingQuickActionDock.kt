package com.antigravity.telemetry.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.antigravity.telemetry.core.designsystem.SlateSoft
import com.antigravity.telemetry.core.designsystem.SlateTextMuted
import com.antigravity.telemetry.core.designsystem.SurfaceWhite

@Composable
fun FloatingQuickActionDock(
    currentRoute: String = "dashboard",
    onDashboardClick: () -> Unit = {},
    onRefillClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDashboard = currentRoute == "dashboard"
    val isHistory = currentRoute == "ledger" || currentRoute == "history"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 28.dp, end = 28.dp, bottom = 18.dp, top = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    spotColor = Color(0x250F172A),
                    ambientColor = Color(0x120F172A)
                )
                .clip(CircleShape)
                .background(SurfaceWhite.copy(alpha = 0.96f))
                .border(1.dp, SlateSoft.copy(alpha = 0.85f), CircleShape)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Action: Dashboard
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDashboardClick
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = "Dashboard",
                        tint = if (isDashboard) CngAccent else SlateTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Dashboard",
                        fontSize = 11.sp,
                        fontWeight = if (isDashboard) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isDashboard) CngAccent else SlateTextMuted
                    )
                }

                // Primary CTA: + Refill Fuel with Green Glass Effect
                Box(
                    modifier = Modifier
                        .weight(1.35f)
                        .height(48.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = Color(0x4510B981),
                            ambientColor = Color(0x2010B981)
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF34D399).copy(alpha = 0.92f),
                                    Color(0xFF059669).copy(alpha = 0.88f)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.40f), CircleShape)
                        .clickable(onClick = onRefillClick),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "+ Refill Fuel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Action: History
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onHistoryClick
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "History",
                        tint = if (isHistory) CngAccent else SlateTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "History",
                        fontSize = 11.sp,
                        fontWeight = if (isHistory) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isHistory) CngAccent else SlateTextMuted
                    )
                }
            }
        }
    }
}
