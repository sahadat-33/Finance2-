import re

# Fix 1: Theme.kt
file_path_theme = "app/src/main/java/com/example/ui/theme/Theme.kt"
with open(file_path_theme, "r") as f:
    content = f.read()

content = content.replace(
    '''    val colorScheme = when (themeName) {        
        "Ocean Blue" -> if (darkTheme) BlueDark else BlueLight
        "Sunset Warm" -> if (darkTheme) WarmDark else WarmLight
        "Lavender Calm" -> if (darkTheme) LavenderDark else LavenderLight
        "Rose Soft" -> if (darkTheme) RoseDark else RoseLight
        else -> if (darkTheme) BlueDark else BlueLight
    }''',
    '''    val colorScheme = when (themeName) {
        "Mint Fresh" -> if (darkTheme) MintDark else MintLight
        "Midnight Dark" -> MidnightDarkScheme
        "Ocean Blue" -> if (darkTheme) BlueDark else BlueLight
        "Sunset Warm" -> if (darkTheme) WarmDark else WarmLight
        "Lavender Calm" -> if (darkTheme) LavenderDark else LavenderLight
        "Rose Soft" -> if (darkTheme) RoseDark else RoseLight
        else -> if (darkTheme) BlueDark else BlueLight
    }'''
)

with open(file_path_theme, "w") as f:
    f.write(content)

# Fix 2: DashboardScreen.kt leftover
file_path_dashboard = "app/src/main/java/com/example/ui/DashboardScreen.kt"
with open(file_path_dashboard, "r") as f:
    content = f.read()

content = content.replace(
    'Text("Leftover: ${formatTaka((stats.totalEarnings - stats.totalExpenses).coerceAtLeast(0.0))}", style = MaterialTheme.typography.bodyMedium)',
    'Text("Leftover: ${formatTaka((stats.totalEarnings - stats.totalExpenses - stats.totalSavingsContributed).coerceAtLeast(0.0))}", style = MaterialTheme.typography.bodyMedium)'
)

with open(file_path_dashboard, "w") as f:
    f.write(content)

