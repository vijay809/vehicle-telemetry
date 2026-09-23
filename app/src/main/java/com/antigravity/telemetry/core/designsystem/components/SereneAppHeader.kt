package com.antigravity.telemetry.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.telemetry.core.designsystem.CanvasLavender
import com.antigravity.telemetry.core.designsystem.CngAccent
import com.antigravity.telemetry.core.designsystem.CngBadge
import com.antigravity.telemetry.core.designsystem.CngPastelBg
import com.antigravity.telemetry.core.designsystem.CngPastelBorder
import com.antigravity.telemetry.core.designsystem.RoundedSm
import com.antigravity.telemetry.core.designsystem.SlateSoft
import com.antigravity.telemetry.core.designsystem.SlateTextMain
import com.antigravity.telemetry.core.designsystem.SlateTextMuted
import com.antigravity.telemetry.core.designsystem.SurfaceWhite

@Composable
fun SereneAppHeader(
    isConnected: Boolean,
    vehicleName: String = "Victoris CNG",
    onOpenSimulator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CanvasLavender)
            .statusBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.clip(RoundedSm)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedSm)
                    .background(if (isConnected) CngBadge else Color(0xFFF1F5F9))
                    .border(1.dp, if (isConnected) CngPastelBorder else SlateSoft, RoundedSm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VC",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isConnected) CngAccent else SlateTextMuted
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = vehicleName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextMain
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isConnected) CngPastelBg else Color(0xFFF1F5F9))
                            .border(1.dp, if (isConnected) CngPastelBorder else SlateSoft, CircleShape)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) CngAccent else SlateTextMuted)
                                    .then(if (isConnected) Modifier.alpha(pulseAlpha) else Modifier)
                            )
                            Text(
                                text = if (isConnected) "Live Sync" else "Disconnected",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) CngAccent else SlateTextMuted
                            )
                        }
                    }
                }
                Text(
                    text = if (isConnected) "Synced via Android Auto" else "Android Auto Disconnected",
                    fontSize = 11.sp,
                    color = SlateTextMuted,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Simulator Trigger Button
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(SurfaceWhite)
                .border(1.dp, SlateSoft, CircleShape)
                .clickable(onClick = onOpenSimulator),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Simulator",
                tint = SlateTextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
