import re

with open('app/src/main/java/com/example/data/SyncWorker.kt', 'r') as f:
    content = f.read()

old_success = """            if (success) {
                applicationContext.getSharedPreferences("taka_tracker_prefs", Context.MODE_PRIVATE)
                    .edit().putLong("last_sync_timestamp", System.currentTimeMillis()).apply()
            }"""

new_success = """            if (success) {
                val count = database.dao.getAllTransactions().size + database.dao.getAllCategories().size + database.dao.getAllSavingsVaults().size
                val size = count * 150
                applicationContext.getSharedPreferences("taka_tracker_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putLong("last_sync_timestamp", System.currentTimeMillis())
                    .putInt("last_sync_count", count)
                    .putInt("last_sync_size", size)
                    .putString("last_sync_type", "Automatic")
                    .apply()
            }"""

content = content.replace(old_success, new_success)

with open('app/src/main/java/com/example/data/SyncWorker.kt', 'w') as f:
    f.write(content)
