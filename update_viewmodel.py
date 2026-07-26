import re
file_path = "app/src/main/java/com/example/viewmodel/FinanceViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()
content = content.replace(
    '''    fun lockApp() {
        if (_isPinEnabled.value) {
            _isAppLocked.value = true
        }
    }''',
    '''    fun lockApp() {
        if (_isPinEnabled.value) {
            _isAppLocked.value = true
        }
    }
    
    fun unlockApp() {
        _isAppLocked.value = false
    }'''
)
with open(file_path, "w") as f:
    f.write(content)
