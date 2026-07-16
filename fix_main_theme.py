import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace("val isDarkMode by viewModel.isDarkMode.collectAsState()", 
"val isDarkMode by viewModel.isDarkMode.collectAsState()\n            val appTheme by viewModel.appTheme.collectAsState()")

content = content.replace("FinanceTrackerTheme(darkTheme = isDarkMode) {",
"FinanceTrackerTheme(darkTheme = isDarkMode, themeName = appTheme) {")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
