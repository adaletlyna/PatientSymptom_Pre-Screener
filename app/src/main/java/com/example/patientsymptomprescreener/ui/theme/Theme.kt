package com.prescreener.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Color Palette ─────────────────────────────────────────────────────────
val PrescreenerBlue      = Color(0xFF1565C0)
val PrescreenerLightBlue = Color(0xFF42A5F5)
val PrescreenerTeal      = Color(0xFF00ACC1)
val PrescreenerBackground = Color(0xFFF5F7FA)
val SurfaceWhite         = Color(0xFFFFFFFF)

val UrgencyImmediate = Color(0xFFD32F2F)
val UrgencyUrgent    = Color(0xFFF57C00)
val UrgencySemiUrgent = Color(0xFFF9A825)
val UrgencyNonUrgent = Color(0xFF388E3C)

private val LightColorScheme = lightColorScheme(
    primary = PrescreenerBlue,
    secondary = PrescreenerTeal,
    tertiary = PrescreenerLightBlue,
    background = PrescreenerBackground,
    surface = SurfaceWhite,
    onPrimary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun PatientScreenerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}


