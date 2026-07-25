import re

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('val themes = listOf("Mint Fresh", "Midnight Dark", "Ocean Blue", "Sunset Warm", "Lavender Calm", "Rose Soft")', 'val themes = listOf("Mint Fresh", "Ocean Blue", "Sunset Warm", "Lavender Calm", "Rose Soft")')
content = content.replace('"Midnight Dark" -> Color(0xFF191C1B)\n', '')

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'w') as f:
    f.write(content)
