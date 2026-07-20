import re

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Replace the showOthersDialog click action
content = content.replace("showOthersDialog = true", "onNavigateToOthers()")
# In the declaration, we must add onNavigateToOthers parameter to SettingsScreen
# fun SettingsScreen(viewModel: FinanceViewModel, onNavigateToProfile: () -> Unit, onNavigateToAuth: () -> Unit) {
content = content.replace("onNavigateToAuth: () -> Unit", "onNavigateToAuth: () -> Unit, onNavigateToOthers: () -> Unit")

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'w') as f:
    f.write(content)

