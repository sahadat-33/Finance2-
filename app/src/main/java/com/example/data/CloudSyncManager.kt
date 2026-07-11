package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class CloudSyncManager(private val dao: FinanceDao) {
    
    private val firestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            null
        }
    }
    
    private val auth by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            null
        }
    }

    suspend fun syncToCloud(): Boolean {
        if (firestore == null || auth == null) return false
        val user = auth?.currentUser ?: return false
        try { user.reload().await() } catch(e: Exception) {}
        if (!user.isEmailVerified) return false

        try {
            val transactions = dao.getAllTransactions()
            val vaults = dao.getAllSavingsVaults()
            val categories = dao.getAllCategories()
            
            // Upload Transactions using local room id
            val txRef = firestore!!.collection("users").document(user.uid).collection("transactions")
            for (tx in transactions) {
                val docId = tx.uuid
                txRef.document(docId).set(tx.toMap(), SetOptions.merge()).await()
            }

            // Upload Vaults/Savings
            val svRef = firestore!!.collection("users").document(user.uid).collection("savings")
            for (sv in vaults) {
                val docId = sv.uuid
                svRef.document(docId).set(sv.toMap(), SetOptions.merge()).await()
            }

            // Categories
            val catRef = firestore!!.collection("users").document(user.uid).collection("categories")
            for (cat in categories) {
                val docId = cat.uuid
                catRef.document(docId).set(cat.toMap(), SetOptions.merge()).await()
            }

            Log.d("CloudSync", "Successfully pushed data to cloud flat collections.")
            return true
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to sync to cloud: ${e.message}")
            return false
        }
    }

    suspend fun deleteDocument(collectionPath: String, docId: String) {
        if (firestore == null || auth == null) return
        val user = auth?.currentUser ?: return
        if (!user.isEmailVerified) return
        try {
            firestore!!.collection("users").document(user.uid)
                .collection(collectionPath).document(docId).delete().await()
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to delete document: ${e.message}")
        }
    }

    suspend fun updateUserCurrency(symbol: String) {
        if (firestore == null || auth == null) return
        val user = auth?.currentUser ?: return
        try { user.reload().await() } catch(e: Exception) {}
        if (!user.isEmailVerified) return

        try {
            val userRef = firestore!!.collection("users").document(user.uid)
            userRef.set(mapOf("currency_symbol" to symbol), SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to update currency: ${e.message}")
        }
    }

    suspend fun fetchUserProfileCurrency(): String? {
        if (firestore == null || auth == null) return null
        val user = auth?.currentUser ?: return null
        try { user.reload().await() } catch(e: Exception) {}
        if (!user.isEmailVerified) return null

        return try {
            val userRef = firestore!!.collection("users").document(user.uid)
            val snap = userRef.get().await()
            snap.getString("currency_symbol")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchFromCloud() {
        if (firestore == null || auth == null) return
        val user = auth?.currentUser ?: return
        try { user.reload().await() } catch(e: Exception) {}
        if (!user.isEmailVerified) return

        try {
            // Upload local transactions first to ensure no wipe
            syncToCloud()

            // Pull remaining from cloud
            val transactionsSnapshot = firestore!!.collection("users").document(user.uid).collection("transactions").get().await()
            val existingTxUuids = dao.getAllTransactions().map { it.uuid }.toSet()
            for (doc in transactionsSnapshot.documents) {
                val data = doc.data ?: continue
                try {
                    val tx = Transaction.fromMap(data)
                    if (!existingTxUuids.contains(tx.uuid)) {
                        dao.insertTransaction(tx.copy(id = 0))
                    }
                } catch (e: Exception) { }
            }

            val savingsSnapshot = firestore!!.collection("users").document(user.uid).collection("savings").get().await()
            val existingSvUuids = dao.getAllSavingsVaults().map { it.uuid }.toSet()
            for (doc in savingsSnapshot.documents) {
                val data = doc.data ?: continue
                try {
                    val sv = SavingsVault.fromMap(data)
                    if (!existingSvUuids.contains(sv.uuid)) {
                        dao.insertSavingsVault(sv.copy(id = 0))
                    }
                } catch(e: Exception) {}
            }

            val categorySnapshot = firestore!!.collection("users").document(user.uid).collection("categories").get().await()
            val existingCatUuids = dao.getAllCategories().map { it.uuid }.toSet()
            for (doc in categorySnapshot.documents) {
                val data = doc.data ?: continue
                try {
                    val cat = Category.fromMap(data)
                    if (!existingCatUuids.contains(cat.uuid)) {
                        dao.insertCategory(cat.copy(id = 0))
                    }
                } catch(e: Exception) {}
            }

            Log.d("CloudSync", "Successfully pulled data from cloud flat collections.")
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to fetch from cloud: ${e.message}")
        }
    }

    suspend fun cleanupDuplicateCategories() {
        if (firestore == null || auth == null) return
        val user = auth?.currentUser ?: return
        if (!user.isEmailVerified) return
        
        try {
            val catRef = firestore!!.collection("users").document(user.uid).collection("categories")
            val snapshot = catRef.get().await()
            
            // Group by name + type
            val categoryGroups = mutableMapOf<String, MutableList<Category>>()
            for (doc in snapshot.documents) {
                val data = doc.data ?: continue
                try {
                    val cat = Category.fromMap(data)
                    val key = "${cat.name}_${cat.type}"
                    if (!categoryGroups.containsKey(key)) {
                        categoryGroups[key] = mutableListOf()
                    }
                    categoryGroups[key]!!.add(cat)
                } catch(e: Exception) {}
            }
            
            for ((_, cats) in categoryGroups) {
                if (cats.size > 1) {
                    // Keep the first one (surviving one), delete the rest
                    // (Note: Transactions use categoryName as String, so no re-linking is technically required in the DB schema)
                    val duplicates = cats.drop(1)
                    
                    for (dup in duplicates) {
                        catRef.document(dup.uuid).delete().await()
                        
                        // Delete from local DB too to keep in sync
                        dao.getAllCategories().find { it.uuid == dup.uuid }?.let { localCat ->
                            dao.deleteCategoryById(localCat.id)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to cleanup duplicates: ${e.message}")
        }
    }
}
