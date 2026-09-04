package com.antigravity.telemetry.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = CngBadge,
    onPrimaryContainer = CngAccent,
    secondary = PetrolAccent,
    onSecondary = SurfaceWhite,
    secondaryContainer = PetrolBadge,
    onSecondaryContainer = PetrolAccent,
    background = CanvasLavender,
    onBackground = SlateTextMain,
    surface = SurfaceWhite,
    onSurface = SlateTextMain,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = SlateTextMuted,
    outline = SlateSoft,
    outlineVariant = CngPastelBorder,
    error = AlertAccent,
    onError = SurfaceWhite
)

@Composable
fun AntiGravityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CanvasLavender.toArgb()
            window.navigationBarColor = CanvasLavender.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AntiGravityTypography,
        shapes = AntiGravityShapes,
        content = content
    )
}
