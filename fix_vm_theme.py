import re

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

new_props = """    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _appTheme = MutableStateFlow(sharedPrefs.getString("app_theme", "Mint Fresh") ?: "Mint Fresh")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    fun setAppTheme(themeName: String) {
        sharedPrefs.edit().putString("app_theme", themeName).apply()
        _appTheme.value = themeName
    }"""

content = re.sub(r'    private val _isDarkMode = MutableStateFlow.*?val isDarkMode: StateFlow<Boolean> = _isDarkMode\.asStateFlow\(\)', new_props, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
