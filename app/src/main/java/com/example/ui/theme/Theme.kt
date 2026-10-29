package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CadPrimary,
    onPrimary = CadDarkBg,
    primaryContainer = CadPrimaryVariant,
    onPrimaryContainer = CadTextPrimary,
    secondary = CadSecondary,
    onSecondary = CadDarkBg,
    tertiary = CadAccent,
    background = CadDarkBg,
    onBackground = CadTextPrimary,
    surface = CadSurface,
    onSurface = CadTextPrimary,
    surfaceVariant = CadSurfaceVariant,
    onSurfaceVariant = CadTextSecondary,
    outline = CadBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CadLightPrimary,
    onPrimary = CadLightBg,
    secondary = CadSecondary,
    background = CadLightBg,
    onBackground = CadLightTextPrimary,
    surface = CadLightSurface,
    onSurface = CadLightTextPrimary,
    outline = CadLightBorder
)

@Composable
fun SmartDraftsmanTheme(
    darkTheme: Boolean = true, // CAD blueprints look best in precision dark by default!
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
