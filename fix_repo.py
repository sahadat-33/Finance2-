import re

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

# Change periodic work request from 12 hours to 24 hours
content = content.replace("PeriodicWorkRequestBuilder<SyncWorker>(12, java.util.concurrent.TimeUnit.HOURS)", "PeriodicWorkRequestBuilder<SyncWorker>(24, java.util.concurrent.TimeUnit.HOURS)")

# modify saveLastSyncTime
old_save = """    fun saveLastSyncTime() {
        sharedPrefs.edit().putLong("last_sync_timestamp", System.currentTimeMillis()).apply()
    }"""
new_save = """    suspend fun saveLastSyncTime(type: String = "Automatic") {
        val count = dao.getAllTransactions().size + dao.getAllCategories().size + dao.getAllSavingsVaults().size
        val size = count * 150
        sharedPrefs.edit()
            .putLong("last_sync_timestamp", System.currentTimeMillis())
            .putInt("last_sync_count", count)
            .putInt("last_sync_size", size)
            .putString("last_sync_type", type)
            .apply()
    }"""
content = content.replace(old_save, new_save)

# Now we need to pass "Automatic" for background syncs, "Immediate" for triggerImmediateSync? Wait, user asked for "Automatic" (periodic/immediate) vs "Manual". So triggerImmediateSync is "Automatic". Manual is a new button.
content = content.replace("saveLastSyncTime()", "saveLastSyncTime(\"Automatic\")")

# Add a public manual sync function
manual_sync = """    suspend fun triggerManualSync(): Boolean = withContext(Dispatchers.IO) {
        if (!isCloudSyncEnabled) return@withContext false
        try {
            val success = cloudSyncManager.syncToCloud()
            if (success) {
                saveLastSyncTime("Manual")
                return@withContext true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext false
    }"""
content = content.replace("    private fun schedulePeriodicSync()", manual_sync + "\n\n    private fun schedulePeriodicSync()")

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(content)
