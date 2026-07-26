import re

with open('app/src/main/java/com/example/ui/theme/Theme.kt', 'r') as f:
    content = f.read()

def replace_dark(content, scheme_name, old_bg):
    # This might be tricky, let's just do it manually for Warm, Lavender, Rose
    return content

# Update WarmDark
old_warm = """val WarmDark = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color(0xFF4A2800),
    primaryContainer = Color(0xFF693C00),
    onPrimaryContainer = Color(0xFFFFDCC1),
    background = Color(0xFF181411),
    onBackground = Color(0xFFECE0DA),
    surface = Color(0xFF241E1A),
    onSurface = Color(0xFFECE0DA),
    surfaceVariant = Color(0xFF52443C),
    onSurfaceVariant = Color(0xFFD7C2B6),"""
new_warm = """val WarmDark = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color(0xFF4A2800),
    primaryContainer = Color(0xFF693C00),
    onPrimaryContainer = Color(0xFFFFDCC1),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,"""
content = content.replace(old_warm, new_warm)

# Update LavenderDark
old_lavender = """val LavenderDark = darkColorScheme(
    primary = Color(0xFFBA68C8),
    onPrimary = Color(0xFF520070),
    primaryContainer = Color(0xFF7600A0),
    onPrimaryContainer = Color(0xFFF6D9FF),
    background = Color(0xFF161118),
    onBackground = Color(0xFFE6E0E9),
    surface = Color(0xFF221A24),
    onSurface = Color(0xFFE6E0E9),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),"""
new_lavender = """val LavenderDark = darkColorScheme(
    primary = Color(0xFFBA68C8),
    onPrimary = Color(0xFF520070),
    primaryContainer = Color(0xFF7600A0),
    onPrimaryContainer = Color(0xFFF6D9FF),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,"""
content = content.replace(old_lavender, new_lavender)

# Update RoseDark
old_rose = """val RoseDark = darkColorScheme(
    primary = Color(0xFFF06292),
    onPrimary = Color(0xFF5E002B),
    primaryContainer = Color(0xFF870042),
    onPrimaryContainer = Color(0xFFFFD9E2),
    background = Color(0xFF181113),
    onBackground = Color(0xFFECE0E1),
    surface = Color(0xFF241A1D),
    onSurface = Color(0xFFECE0E1),
    surfaceVariant = Color(0xFF524345),
    onSurfaceVariant = Color(0xFFD7C1C3),"""
new_rose = """val RoseDark = darkColorScheme(
    primary = Color(0xFFF06292),
    onPrimary = Color(0xFF5E002B),
    primaryContainer = Color(0xFF870042),
    onPrimaryContainer = Color(0xFFFFD9E2),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,"""
content = content.replace(old_rose, new_rose)

with open('app/src/main/java/com/example/ui/theme/Theme.kt', 'w') as f:
    f.write(content)
