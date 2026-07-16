import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('androidx.compose.material.icons.Icons.Default.WbSunny', 'Icons.Default.WbSunny')
content = content.replace('androidx.compose.material.icons.Icons.Default.ModeNight', 'Icons.Default.ModeNight')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

