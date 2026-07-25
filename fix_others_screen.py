import re

with open('app/src/main/java/com/example/ui/OthersScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('Text("Check for Updates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)', 'Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)')

with open('app/src/main/java/com/example/ui/OthersScreen.kt', 'w') as f:
    f.write(content)
