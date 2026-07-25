import re

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

old_sync = """    private val _lastSyncTimestamp = MutableStateFlow(sharedPrefs.getLong("last_sync_timestamp", 0L))
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()
    
    private val prefListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == "last_sync_timestamp") {
            _lastSyncTimestamp.value = sharedPreferences.getLong(key, 0L)
        }
    }"""

new_sync = """    private val _lastSyncTimestamp = MutableStateFlow(sharedPrefs.getLong("last_sync_timestamp", 0L))
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()
    
    private val _lastSyncCount = MutableStateFlow(sharedPrefs.getInt("last_sync_count", 0))
    val lastSyncCount: StateFlow<Int> = _lastSyncCount.asStateFlow()
    
    private val _lastSyncSize = MutableStateFlow(sharedPrefs.getInt("last_sync_size", 0))
    val lastSyncSize: StateFlow<Int> = _lastSyncSize.asStateFlow()
    
    private val _lastSyncType = MutableStateFlow(sharedPrefs.getString("last_sync_type", "Automatic") ?: "Automatic")
    val lastSyncType: StateFlow<String> = _lastSyncType.asStateFlow()
    
    private val prefListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == "last_sync_timestamp") {
            _lastSyncTimestamp.value = sharedPreferences.getLong(key, 0L)
        }
        if (key == "last_sync_count") {
            _lastSyncCount.value = sharedPreferences.getInt(key, 0)
        }
        if (key == "last_sync_size") {
            _lastSyncSize.value = sharedPreferences.getInt(key, 0)
        }
        if (key == "last_sync_type") {
            _lastSyncType.value = sharedPreferences.getString(key, "Automatic") ?: "Automatic"
        }
    }
    
    fun triggerManualSync(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.triggerManualSync()
            onComplete(result)
        }
    }"""

content = content.replace(old_sync, new_sync)

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
