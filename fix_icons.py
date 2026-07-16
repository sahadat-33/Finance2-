import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('androidx.compose.material.icons.Icons.Rounded.WbSunny', 'androidx.compose.material.icons.filled.WbSunny')
content = content.replace('androidx.compose.material.icons.Icons.Rounded.ModeNight', 'androidx.compose.material.icons.filled.ModeNight')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
