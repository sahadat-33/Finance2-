package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class FinanceRepository(private val context: Context) {
    private val database: FinanceDatabase by lazy {
        DatabaseProvider.getDatabase(context)
    }

    val dao: FinanceDao get() = database.dao

    val authManager by lazy { FirebaseAuthManager(context) }
    val cloudSyncManager by lazy { CloudSyncManager(dao) }

    private val sharedPrefs by lazy { context.getSharedPreferences("taka_tracker_prefs", Context.MODE_PRIVATE) }
    
    private val isCloudSyncEnabled: Boolean
        get() = sharedPrefs.getBoolean("cloud_sync_enabled", true)
        
    suspend fun saveLastSyncTime(type: String = "Automatic") {
        val count = dao.getAllTransactions().size + dao.getAllCategories().size + dao.getAllSavingsVaults().size
        val size = count * 150
        sharedPrefs.edit()
            .putLong("last_sync_timestamp", System.currentTimeMillis())
            .putInt("last_sync_count", count)
            .putInt("last_sync_size", size)
            .putString("last_sync_type", type)
            .apply()
    }

    suspend fun triggerManualSync(): Boolean = withContext(Dispatchers.IO) {
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
    }

    private fun schedulePeriodicSync() {
        if (!isCloudSyncEnabled) return
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()
        val syncRequest = androidx.work.PeriodicWorkRequestBuilder<SyncWorker>(24, java.util.concurrent.TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "PeriodicCloudSyncWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun triggerImmediateSync() {
        if (!isCloudSyncEnabled) return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                kotlinx.coroutines.withTimeout(5000L) {
                    val success = cloudSyncManager.syncToCloud()
                    if (success) {
                        saveLastSyncTime("Automatic")
                    }
                }
            } catch (e: Exception) {
                // Queue for later if immediate sync fails or times out
                val constraints = androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()
                val syncRequest = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(constraints)
                    .build()
                androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
                    "OfflineCloudSyncWork",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    syncRequest
                )
            }
        }
    }

    init {
        schedulePeriodicSync()
        // Run database initialization on background thread
        CoroutineScope(Dispatchers.IO).launch {
            val isClean = dao.getAllTransactions().isEmpty()
            if (authManager.isUserSignedIn && isClean) {
                cloudSyncManager.fetchFromCloud()
            } else if (authManager.isUserSignedIn && isCloudSyncEnabled) {
                // Background sync
                triggerImmediateSync()
            }
        }
    }

    suspend fun createAccount(email: String, pass: String, username: String): Boolean = withContext(Dispatchers.IO) {
        val success = authManager.createAccount(email, pass, username)
        if (success) {
            val isClean = dao.getAllTransactions().isEmpty() && dao.getAllCategoriesFlow().first().isEmpty()
            if (isClean) {
                triggerImmediateSync()
            }
        }
        return@withContext success
    }

    suspend fun login(email: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        val success = authManager.login(email, pass)
        if (success) {
            cloudSyncManager.cleanupDuplicates()
            cloudSyncManager.fetchFromCloud()
            triggerImmediateSync()
        }
        return@withContext success
    }

    suspend fun sendPasswordReset(email: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext authManager.sendPasswordReset(email)
    }

    suspend fun updateUsername(username: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext authManager.updateUsername(username)
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        authManager.signOut()
    }

    fun getAllTransactions(): Flow<List<Transaction>> = dao.getAllTransactionsFlow()
    fun getAllCategories(): Flow<List<Category>> = dao.getAllCategoriesFlow()
    fun getAllSavingsVault(): Flow<List<SavingsVault>> = dao.getAllSavingsVaultFlow()

    suspend fun insertTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        dao.insertTransaction(transaction)
        
        // Handle Savings trigger logic:
        if (transaction.categoryName == "Savings") {
            val targetAssetType = detectAssetType(transaction.note)
            if (targetAssetType != null) {
                val currentVault = dao.getSavingsVaultByAssetType(targetAssetType)
                if (currentVault != null) {
                    if (transaction.type == "EXPENSE") {
                        dao.updateSavingsAmount(targetAssetType, currentVault.amount + transaction.amount)
                    } else if (transaction.type == "INCOME") {
                        dao.updateSavingsAmount(targetAssetType, (currentVault.amount - transaction.amount).coerceAtLeast(0.0))
                    }
                }
            }
        }
        triggerImmediateSync()
    }

    suspend fun updateTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        dao.updateTransaction(transaction)
        triggerImmediateSync()
    }

    suspend fun deleteTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        dao.deleteTransaction(transaction)
        
        if (transaction.categoryName == "Savings") {
            val targetAssetType = detectAssetType(transaction.note)
            if (targetAssetType != null) {
                val currentVault = dao.getSavingsVaultByAssetType(targetAssetType)
                if (currentVault != null) {
                    if (transaction.type == "EXPENSE") {
                        dao.updateSavingsAmount(targetAssetType, (currentVault.amount - transaction.amount).coerceAtLeast(0.0))
                    } else if (transaction.type == "INCOME") {
                        dao.updateSavingsAmount(targetAssetType, currentVault.amount + transaction.amount)
                    }
                }
            }
        }
        cloudSyncManager.deleteDocument("transactions", transaction.uuid)
        triggerImmediateSync()
    }

    suspend fun deleteCategory(categoryId: Int) = withContext(Dispatchers.IO) {
        val cat = dao.getCategoryById(categoryId)
        dao.deleteCategoryById(categoryId)
        if (cat != null) {
            cloudSyncManager.deleteDocument("categories", cat.uuid)
        }
        triggerImmediateSync()
    }

    suspend fun insertSavingsVault(vault: SavingsVault) = withContext(Dispatchers.IO) {
        dao.insertSavingsVault(vault)
        triggerImmediateSync()
    }

    suspend fun deleteSavingsVault(vaultId: Int) = withContext(Dispatchers.IO) {
        val vault = dao.getSavingsVaultById(vaultId)
        dao.deleteSavingsVaultById(vaultId)
        if (vault != null) {
            cloudSyncManager.deleteDocument("savings", vault.uuid)
        }
        triggerImmediateSync()
    }

    suspend fun insertCategory(category: Category) = withContext(Dispatchers.IO) {
        dao.insertCategory(category)
        triggerImmediateSync()
    }

    suspend fun updateSavingsAmountDirectly(assetType: String, amount: Double) = withContext(Dispatchers.IO) {
        dao.updateSavingsAmount(assetType, amount)
        triggerImmediateSync()
    }

    private suspend fun detectAssetType(note: String): String? {
        val regex = Regex("(?:To|From) (.*?) Vault", RegexOption.IGNORE_CASE)
        val match = regex.find(note)
        if (match != null) {
            return match.groupValues[1]
        }
        
        val allVaults = dao.getAllSavingsVaultFlow().first()
        for (v in allVaults) {
            if (note.contains(v.assetType, ignoreCase = true)) {
                return v.assetType
            }
        }
        return allVaults.firstOrNull()?.assetType
    }



    private fun mayTime(baseCalendar: Calendar, day: Int): Long {
        val cal = baseCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, day)
        return cal.timeInMillis
    }

    suspend fun reauthenticate(password: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext authManager.reauthenticate(password)
    }

    suspend fun updateEmail(newEmail: String): String = withContext(Dispatchers.IO) {
        return@withContext authManager.updateEmail(newEmail)
    }

    suspend fun deleteAccount(): Boolean = withContext(Dispatchers.IO) {
        return@withContext authManager.deleteAccount()
    }
}