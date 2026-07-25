package com.example.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val database = DatabaseProvider.getDatabase(applicationContext)
            
            val cloudSyncManager = CloudSyncManager(database.dao)
            val success = cloudSyncManager.syncToCloud()
            
            if (success) {
                val count = database.dao.getAllTransactions().size + database.dao.getAllCategories().size + database.dao.getAllSavingsVaults().size
                val size = count * 150
                applicationContext.getSharedPreferences("taka_tracker_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putLong("last_sync_timestamp", System.currentTimeMillis())
                    .putInt("last_sync_count", count)
                    .putInt("last_sync_size", size)
                    .putString("last_sync_type", "Automatic")
                    .apply()
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
