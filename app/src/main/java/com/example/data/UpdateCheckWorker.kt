package com.example.data

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class UpdateCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val currentVersion = BuildConfig.VERSION_NAME
            val updateInfo = UpdateChecker.checkForUpdate(currentVersion)
            
            val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            
            if (updateInfo != null) {
                prefs.edit()
                    .putBoolean(KEY_IS_UPDATE_AVAILABLE, true)
                    .putString(KEY_AVAILABLE_VERSION, updateInfo.version)
                    .putString(KEY_DOWNLOAD_URL, updateInfo.downloadUrl)
                    .putString(KEY_RELEASE_NOTES, updateInfo.releaseNotes)
                    .putBoolean(KEY_FORCE_UPDATE, updateInfo.forceUpdate)
                    .putInt(KEY_DOWNLOAD_COUNT, updateInfo.downloadCount)
                    .putLong(KEY_LAST_CHECK_TIME, now)
                    .apply()
            } else {
                prefs.edit()
                    .putBoolean(KEY_IS_UPDATE_AVAILABLE, false)
                    .putString(KEY_AVAILABLE_VERSION, "")
                    .putString(KEY_DOWNLOAD_URL, "")
                    .putString(KEY_RELEASE_NOTES, "")
                    .putBoolean(KEY_FORCE_UPDATE, false)
                    .putInt(KEY_DOWNLOAD_COUNT, 0)
                    .putLong(KEY_LAST_CHECK_TIME, now)
                    .apply()
            }
            
            Result.success()
        } catch (e: Exception) {
            // Fail with retry so exponential backoff takes effect
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.success()
            }
        }
    }

    companion object {
        const val PREFS_NAME = "UpdatePreferences"
        const val KEY_IS_UPDATE_AVAILABLE = "is_update_available"
        const val KEY_AVAILABLE_VERSION = "available_update_version"
        const val KEY_DOWNLOAD_URL = "available_update_url"
        const val KEY_RELEASE_NOTES = "available_update_release_notes"
        const val KEY_FORCE_UPDATE = "available_update_force_update"
        const val KEY_DOWNLOAD_COUNT = "available_update_download_count"
        const val KEY_LAST_CHECK_TIME = "last_check_time"
        const val KEY_LAST_DISMISSED_VERSION = "last_dismissed_version"

        fun schedulePeriodicUpdateCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            // 12-hour periodic check with exponential backoff and battery/network constraints
            val updateRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
                
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "PeriodicUpdateCheckWork",
                ExistingPeriodicWorkPolicy.KEEP,
                updateRequest
            )
        }
    }
}
