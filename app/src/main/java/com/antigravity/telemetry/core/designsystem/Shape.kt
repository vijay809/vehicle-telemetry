package com.antigravity.telemetry.core.designsystem

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val RoundedSm = RoundedCornerShape(8.dp)
val RoundedMd = RoundedCornerShape(16.dp)
val Rounded2xl = RoundedCornerShape(20.dp)
val Rounded3xl = RoundedCornerShape(28.dp)
val RoundedPill = CircleShape

val AntiGravityShapes = Shapes(
    small = RoundedSm,
    medium = RoundedMd,
    large = Rounded2xl,
    extraLarge = Rounded3xl
)
