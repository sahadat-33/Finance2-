import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('androidx.compose.material.icons.Icons.Rounded.LightMode', 'androidx.compose.material.icons.Icons.Rounded.WbSunny')
content = content.replace('androidx.compose.material.icons.Icons.Rounded.DarkMode', 'androidx.compose.material.icons.Icons.Rounded.ModeNight')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
