import re

content = """package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MintPrimary = Color(0xFF30BA8C)

// 1. Mint Fresh
val MintLight = lightColorScheme(
    primary = MintPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5F2E8),
    onPrimaryContainer = Color(0xFF093729),
    background = Color(0xFFF8FDFB),
    onBackground = Color(0xFF191C1B),
    surface = Color.White,
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDBE5E0),
    onSurfaceVariant = Color(0xFF3F4945),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF6F7975)
)
val MintDark = darkColorScheme(
    primary = MintPrimary,
    onPrimary = Color(0xFF003827),
    primaryContainer = Color(0xFF005139),
    onPrimaryContainer = Color(0xFF8CF8C7),
    background = Color(0xFF191C1B),
    onBackground = Color(0xFFE1E3E0),
    surface = Color(0xFF1E201F),
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = Color(0xFF3F4945),
    onSurfaceVariant = Color(0xFFBFC9C4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF89938E)
)

// 2. Midnight Dark (Always Dark)
val MidnightDarkScheme = darkColorScheme(
    primary = MintPrimary,
    onPrimary = Color(0xFF003827),
    primaryContainer = Color(0xFF005139),
    onPrimaryContainer = Color(0xFF8CF8C7),
    background = Color(0xFF0A0C0B),
    onBackground = Color(0xFFE1E3E0),
    surface = Color(0xFF151716),
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = Color(0xFF232A27),
    onSurfaceVariant = Color(0xFFBFC9C4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF89938E)
)

// 3. Ocean Blue
val BlueLight = lightColorScheme(
    primary = Color(0xFF2196F3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    background = Color(0xFFF4F9FF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF42474E),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF73777F)
)
val BlueDark = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    background = Color(0xFF111418),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1E2228),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC3C7CF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8D9199)
)

// 4. Sunset Warm
val WarmLight = lightColorScheme(
    primary = Color(0xFFFF9800),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCC1),
    onPrimaryContainer = Color(0xFF2E1500),
    background = Color(0xFFFFF9F3),
    onBackground = Color(0xFF201A16),
    surface = Color.White,
    onSurface = Color(0xFF201A16),
    surfaceVariant = Color(0xFFF4DFD2),
    onSurfaceVariant = Color(0xFF52443C),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF85736B)
)
val WarmDark = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color(0xFF4A2800),
    primaryContainer = Color(0xFF693C00),
    onPrimaryContainer = Color(0xFFFFDCC1),
    background = Color(0xFF181411),
    onBackground = Color(0xFFECE0DA),
    surface = Color(0xFF241E1A),
    onSurface = Color(0xFFECE0DA),
    surfaceVariant = Color(0xFF52443C),
    onSurfaceVariant = Color(0xFFD7C2B6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF9F8D84)
)

// 5. Lavender Calm
val LavenderLight = lightColorScheme(
    primary = Color(0xFF9C27B0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6D9FF),
    onPrimaryContainer = Color(0xFF320046),
    background = Color(0xFFFCF6FF),
    onBackground = Color(0xFF1D1B20),
    surface = Color.White,
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFEADDFF),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF79747E)
)
val LavenderDark = darkColorScheme(
    primary = Color(0xFFBA68C8),
    onPrimary = Color(0xFF520070),
    primaryContainer = Color(0xFF7600A0),
    onPrimaryContainer = Color(0xFFF6D9FF),
    background = Color(0xFF161118),
    onBackground = Color(0xFFE6E0E9),
    surface = Color(0xFF221A24),
    onSurface = Color(0xFFE6E0E9),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF938F99)
)

// 6. Rose Soft
val RoseLight = lightColorScheme(
    primary = Color(0xFFE91E63),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E2),
    onPrimaryContainer = Color(0xFF3E001D),
    background = Color(0xFFFFF5F8),
    onBackground = Color(0xFF201A1B),
    surface = Color.White,
    onSurface = Color(0xFF201A1B),
    surfaceVariant = Color(0xFFF4DDDF),
    onSurfaceVariant = Color(0xFF524345),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF857375)
)
val RoseDark = darkColorScheme(
    primary = Color(0xFFF06292),
    onPrimary = Color(0xFF5E002B),
    primaryContainer = Color(0xFF870042),
    onPrimaryContainer = Color(0xFFFFD9E2),
    background = Color(0xFF181113),
    onBackground = Color(0xFFECE0E1),
    surface = Color(0xFF241A1D),
    onSurface = Color(0xFFECE0E1),
    surfaceVariant = Color(0xFF524345),
    onSurfaceVariant = Color(0xFFD7C1C3),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF9F8C8E)
)

@Composable
fun FinanceTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeName: String = "Mint Fresh",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "Midnight Dark" -> MidnightDarkScheme
        "Ocean Blue" -> if (darkTheme) BlueDark else BlueLight
        "Sunset Warm" -> if (darkTheme) WarmDark else WarmLight
        "Lavender Calm" -> if (darkTheme) LavenderDark else LavenderLight
        "Rose Soft" -> if (darkTheme) RoseDark else RoseLight
        else -> if (darkTheme) MintDark else MintLight // Default to Mint Fresh
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
"""
with open('app/src/main/java/com/example/ui/theme/Theme.kt', 'w') as f:
    f.write(content)
