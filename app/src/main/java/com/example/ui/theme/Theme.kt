package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA2C2FC), // Soft and cool lite blue
    secondary = Color(0xFFBAC6E4), // Grayish-blue
    tertiary = Color(0xFF2C3E5D), // Dark slate blue container
    background = Color(0xFF111418), // Deep cool gray
    surface = Color(0xFF1B1F26), // Dark slate blue surface
    onBackground = Color(0xFFE2E4E9),
    onSurface = Color(0xFFE2E4E9),
    surfaceVariant = Color(0xFF232A35), // Card Variant
    outline = Color(0xFF3A4454),
    primaryContainer = Color(0xFF1E2D4A), // Deep navy
    onPrimaryContainer = Color(0xFFD3E0FA) // Light blue tint
)

private val LightColorScheme = lightColorScheme(
    primary = MinimalPrimary,
    secondary = MinimalSecondary,
    tertiary = MinimalTertiary,
    background = MinimalBackground,
    surface = MinimalSurface,
    onBackground = MinimalOnBackground,
    onSurface = MinimalOnSurface,
    surfaceVariant = MinimalSurfaceVariant,
    outline = MinimalBorder,
    primaryContainer = MinimalTertiary,
    onPrimaryContainer = MinimalOnTertiary
)

@Composable
fun FinanceTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to force our beautiful Emerald theme
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
