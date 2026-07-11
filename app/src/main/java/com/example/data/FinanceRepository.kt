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
        
    fun saveLastSyncTime() {
        sharedPrefs.edit().putLong("last_sync_timestamp", System.currentTimeMillis()).apply()
    }

    private fun schedulePeriodicSync() {
        if (!isCloudSyncEnabled) return
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()
        val syncRequest = androidx.work.PeriodicWorkRequestBuilder<SyncWorker>(12, java.util.concurrent.TimeUnit.HOURS)
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
                        saveLastSyncTime()
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
                initializeDatabaseIfEmpty()
                triggerImmediateSync()
            }
        }
        return@withContext success
    }

    suspend fun login(email: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        val success = authManager.login(email, pass)
        if (success) {
            cloudSyncManager.cleanupDuplicateCategories()
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

    suspend fun initializeDatabaseIfEmpty() {
        val categories = dao.getAllCategoriesFlow().first()
        if (categories.isEmpty()) {
            // 1. Insert Minimalist Categories
            val defaultCategories = listOf(
                Category(name = "Others", type = "INCOME", isDefault = true), // Need "Others" for initial wallet balance
                Category(name = "Salary", type = "INCOME", isDefault = true),
                Category(name = "Savings", type = "INCOME", isDefault = true), // For withdrawal
                
                Category(name = "Home Rent", type = "EXPENSE", isDefault = true),
                Category(name = "Food", type = "EXPENSE", isDefault = true),
                Category(name = "Others", type = "EXPENSE", isDefault = true),
                Category(name = "Savings", type = "EXPENSE", isDefault = true) // For contributions
            )
            for (category in defaultCategories) {
                dao.insertCategory(category)
            }

            // 2. Insert Base Assets to the Vault
            val defaultVault = listOf(
                SavingsVault(assetType = "Insurance", amount = 1500.0),
                SavingsVault(assetType = "Bank", amount = 8500.0)
            )
            for (vault in defaultVault) {
                dao.insertSavingsVault(vault)
            }
        }
    }

    private fun mayTime(baseCalendar: Calendar, day: Int): Long {
        val cal = baseCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, day)
        return cal.timeInMillis
    }
}
