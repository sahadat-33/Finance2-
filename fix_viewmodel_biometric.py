import re

file_path = "app/src/main/java/com/example/viewmodel/FinanceViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add isBiometricEnabled
content = content.replace(
    'private val _isPinEnabled = MutableStateFlow(sharedPrefs.getBoolean("pin_enabled", false))',
    '''private val _isBiometricEnabled = MutableStateFlow(sharedPrefs.getBoolean("biometric_enabled", false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()
    
    fun setBiometricEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("biometric_enabled", enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    private val _isPinEnabled = MutableStateFlow(sharedPrefs.getBoolean("pin_enabled", false))'''
)

# disablePin update
content = content.replace(
    '''sharedPrefs.edit().putBoolean("pin_enabled", false).apply()
            _isPinEnabled.value = false''',
    '''sharedPrefs.edit().putBoolean("pin_enabled", false).apply()
            sharedPrefs.edit().putBoolean("biometric_enabled", false).apply()
            _isPinEnabled.value = false
            _isBiometricEnabled.value = false'''
)

with open(file_path, "w") as f:
    f.write(content)
