import re

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Let's verify our anchors
print("Vault anchor found: ", 'Button(onClick = { showVaultDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Close") }\n                }\n            }\n        }\n    }' in content)
print("Theme block anchor found: ", '// App Theme Selection' in content)
