package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "INCOME" or "EXPENSE"
    val isDefault: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val uuid: String = java.util.UUID.randomUUID().toString()
) {
    fun toMap(): Map<String, Any> = mapOf("id" to id, "name" to name, "type" to type, "isDefault" to isDefault, "updatedAt" to updatedAt, "uuid" to uuid)
    companion object {
        fun fromMap(map: Map<String, Any>, docId: String? = null): Category = Category(
            id = (map["id"] as? Number)?.toInt() ?: 0,
            name = map["name"] as? String ?: "",
            type = map["type"] as? String ?: "",
            isDefault = map["isDefault"] as? Boolean ?: false,
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            uuid = map["uuid"] as? String ?: docId ?: java.util.UUID.randomUUID().toString()
        )
    }
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val categoryName: String,
    val amount: Double,
    val date: Long, // timestamp
    val note: String = "", // e.g. "Abbu", "Transport"
    val receiptImageUri: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val uuid: String = java.util.UUID.randomUUID().toString()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id, "type" to type, "categoryName" to categoryName, "amount" to amount,
        "date" to date, "note" to note, "receiptImageUri" to (receiptImageUri ?: ""), "updatedAt" to updatedAt, "uuid" to uuid
    )
    companion object {
        fun fromMap(map: Map<String, Any>, docId: String? = null): Transaction = Transaction(
            id = (map["id"] as? Number)?.toInt() ?: 0,
            type = map["type"] as? String ?: "",
            categoryName = map["categoryName"] as? String ?: "",
            amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
            date = (map["date"] as? Number)?.toLong() ?: 0L,
            note = map["note"] as? String ?: "",
            receiptImageUri = (map["receiptImageUri"] as? String).takeIf { !it.isNullOrBlank() },
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            uuid = map["uuid"] as? String ?: docId ?: java.util.UUID.randomUUID().toString()
        )
    }
}

@Entity(tableName = "savings_vault")
data class SavingsVault(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val assetType: String,
    val amount: Double,
    val updatedAt: Long = System.currentTimeMillis(),
    val uuid: String = java.util.UUID.randomUUID().toString()
) {
    fun toMap(): Map<String, Any> = mapOf("id" to id, "assetType" to assetType, "amount" to amount, "updatedAt" to updatedAt, "uuid" to uuid)
    companion object {
        fun fromMap(map: Map<String, Any>, docId: String? = null): SavingsVault = SavingsVault(
            id = (map["id"] as? Number)?.toInt() ?: 0,
            assetType = map["assetType"] as? String ?: "",
            amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            uuid = map["uuid"] as? String ?: docId ?: java.util.UUID.randomUUID().toString()
        )
    }
}

@Dao
interface FinanceDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactionsFlow(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAllCategoriesFlow(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?
    
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)

    @Query("SELECT * FROM savings_vault")
    fun getAllSavingsVaultFlow(): Flow<List<SavingsVault>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsVault(vault: SavingsVault)

    @Query("UPDATE savings_vault SET amount = :amount, updatedAt = :updatedAt WHERE assetType = :assetType")
    suspend fun updateSavingsAmount(assetType: String, amount: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM savings_vault WHERE id = :id")
    suspend fun getSavingsVaultById(id: Int): SavingsVault?
    
    @Query("DELETE FROM savings_vault WHERE id = :id")
    suspend fun deleteSavingsVaultById(id: Int)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactions(): List<Transaction>

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM savings_vault")
    suspend fun getAllSavingsVaults(): List<SavingsVault>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Query("DELETE FROM savings_vault")
    suspend fun deleteAllSavingsVaults()

    suspend fun deleteAllData() {
        deleteAllTransactions()
        deleteAllCategories()
        deleteAllSavingsVaults()
    }

    @Query("SELECT * FROM savings_vault WHERE assetType = :assetType LIMIT 1")
    suspend fun getSavingsVaultByAssetType(assetType: String): SavingsVault?
}

@Database(entities = [Category::class, Transaction::class, SavingsVault::class], version = 6, exportSchema = false)
abstract class FinanceDatabase : RoomDatabase() {
    abstract val dao: FinanceDao
}
