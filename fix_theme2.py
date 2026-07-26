import re

with open('app/src/main/java/com/example/ui/theme/Theme.kt', 'r') as f:
    content = f.read()

# Update MintDark
old_mint_dark = """val MintDark = darkColorScheme(
    primary = MintPrimary,
    onPrimary = Color(0xFF003827),
    primaryContainer = Color(0xFF005139),
    onPrimaryContainer = Color(0xFF8CF8C7),
    background = Color(0xFF191C1B),
    onBackground = Color(0xFFE1E3E0),
    surface = Color(0xFF1E201F),
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = Color(0xFF3F4945),
    onSurfaceVariant = Color(0xFFBFC9C4),"""
new_mint_dark = """val MintDark = darkColorScheme(
    primary = MintPrimary,
    onPrimary = Color(0xFF003827),
    primaryContainer = Color(0xFF005139),
    onPrimaryContainer = Color(0xFF8CF8C7),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,"""
content = content.replace(old_mint_dark, new_mint_dark)

# Update MidnightDarkScheme
old_midnight_dark = """val MidnightDarkScheme = darkColorScheme(
    primary = MintPrimary,
    onPrimary = Color(0xFF003827),
    primaryContainer = Color(0xFF005139),
    onPrimaryContainer = Color(0xFF8CF8C7),
    background = Color(0xFF0A0C0B),
    onBackground = Color(0xFFE1E3E0),
    surface = Color(0xFF151716),
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = Color(0xFF232A27),
    onSurfaceVariant = Color(0xFFBFC9C4),"""
new_midnight_dark = """val MidnightDarkScheme = darkColorScheme(
    primary = MintPrimary,
    onPrimary = Color(0xFF003827),
    primaryContainer = Color(0xFF005139),
    onPrimaryContainer = Color(0xFF8CF8C7),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,"""
content = content.replace(old_midnight_dark, new_midnight_dark)

with open('app/src/main/java/com/example/ui/theme/Theme.kt', 'w') as f:
    f.write(content)
