import re

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Remove old showCurrencyDialog declaration
content = content.replace("    var showCurrencyDialog by remember { mutableStateOf(false) }\n", "")

# Insert it before showOthersDialog
content = content.replace(
    "    var showOthersDialog by remember { mutableStateOf(false) }",
    "    var showCurrencyDialog by remember { mutableStateOf(false) }\n    var showOthersDialog by remember { mutableStateOf(false) }"
)

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'w') as f:
    f.write(content)
