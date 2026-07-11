import re

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

new_func = """
    fun triggerFetchFromCloud() {
        if (!isCloudSyncEnabled.value) return
        viewModelScope.launch {
            val success = repository.cloudSyncManager.fetchFromCloud()
            if (success) {
                repository.saveLastSyncTime()
            }
        }
    }
    
    fun setCloudSyncEnabled(enabled: Boolean) {
"""

content = content.replace('    fun setCloudSyncEnabled(enabled: Boolean) {', new_func)

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
