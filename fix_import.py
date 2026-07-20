import re

with open('app/src/main/java/com/example/ui/OthersScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.BorderStroke")

with open('app/src/main/java/com/example/ui/OthersScreen.kt', 'w') as f:
    f.write(content)
