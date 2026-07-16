import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('Text(text = if (isDarkMode) "☀️" else "🌙")', 
'Icon(imageVector = if (isDarkMode) androidx.compose.material.icons.Icons.Rounded.LightMode else androidx.compose.material.icons.Icons.Rounded.DarkMode, contentDescription = "Toggle Theme", tint = MaterialTheme.colorScheme.primary)')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
