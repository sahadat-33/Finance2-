import re

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('private val _appTheme = MutableStateFlow(sharedPrefs.getString("app_theme", "Mint Fresh") ?: "Mint Fresh")', 'private val _appTheme = MutableStateFlow(sharedPrefs.getString("app_theme", "Ocean Blue") ?: "Ocean Blue")')

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
