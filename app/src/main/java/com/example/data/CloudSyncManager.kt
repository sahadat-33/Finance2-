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

    suspend fun fetchFromCloud(): Boolean {
        if (firestore == null || auth == null) return false
        val user = auth?.currentUser ?: return false
        try { user.reload().await() } catch(e: Exception) {}
        if (!user.isEmailVerified) return false
        
        try {
            // First fetch cloud data, then merge.
            cleanupDuplicates()
            val transactionsSnapshot = firestore!!.collection("users").document(user.uid).collection("transactions").get().await()
            val localTx = dao.getAllTransactions().associateBy { it.uuid }
            for (doc in transactionsSnapshot.documents) {
                val data = doc.data ?: continue
                try {
                    val remoteTx = Transaction.fromMap(data, doc.id)
                    val local = localTx[remoteTx.uuid]
                    if (local == null) {
                        dao.insertTransaction(remoteTx.copy(id = 0))
                    } else if (remoteTx.updatedAt > local.updatedAt) {
                        dao.insertTransaction(remoteTx.copy(id = local.id))
                    }
                } catch (e: Exception) { }
            }

            val savingsSnapshot = firestore!!.collection("users").document(user.uid).collection("savings").get().await()
            val localSv = dao.getAllSavingsVaults().associateBy { it.uuid }
            for (doc in savingsSnapshot.documents) {
                val data = doc.data ?: continue
                try {
                    val remoteSv = SavingsVault.fromMap(data, doc.id)
                    val local = localSv[remoteSv.uuid]
                    if (local == null) {
                        dao.insertSavingsVault(remoteSv.copy(id = 0))
                    } else if (remoteSv.updatedAt > local.updatedAt) {
                        dao.insertSavingsVault(remoteSv.copy(id = local.id))
                    }
                } catch(e: Exception) {}
            }

            val categorySnapshot = firestore!!.collection("users").document(user.uid).collection("categories").get().await()
            val localCat = dao.getAllCategories().associateBy { it.uuid }
            for (doc in categorySnapshot.documents) {
                val data = doc.data ?: continue
                try {
                    val remoteCat = Category.fromMap(data, doc.id)
                    val local = localCat[remoteCat.uuid]
                    if (local == null) {
                        dao.insertCategory(remoteCat.copy(id = 0))
                    } else if (remoteCat.updatedAt > local.updatedAt) {
                        dao.insertCategory(remoteCat.copy(id = local.id))
                    }
                } catch(e: Exception) {}
            }

            // Sync the merged local back to cloud 
            val syncSuccess = syncToCloud()
            Log.d("CloudSync", "Successfully pulled data from cloud flat collections.")
            return syncSuccess
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to fetch from cloud: ${e.message}")
            return false
        }
    }

    suspend fun cleanupDuplicates() {
        if (firestore == null || auth == null) return
        val user = auth?.currentUser ?: return
        if (!user.isEmailVerified) return
        
        try {
            // Cleanup Categories
            val catRef = firestore!!.collection("users").document(user.uid).collection("categories")
            val catSnapshot = catRef.get().await()
            val categoryGroups = mutableMapOf<String, MutableList<Category>>()
            for (doc in catSnapshot.documents) {
                val data = doc.data ?: continue
                try {
                    val cat = Category.fromMap(data, doc.id)
                    val key = "${cat.name}_${cat.type}"
                    if (!categoryGroups.containsKey(key)) categoryGroups[key] = mutableListOf()
                    categoryGroups[key]!!.add(cat)
                } catch(e: Exception) {}
            }
            for ((_, cats) in categoryGroups) {
                if (cats.size > 1) {
                    val sorted = cats.sortedByDescending { it.updatedAt }
                    val duplicates = sorted.drop(1)
                    for (dup in duplicates) {
                        catRef.document(dup.uuid).delete().await()
                        dao.getAllCategories().find { it.uuid == dup.uuid }?.let { dao.deleteCategoryById(it.id) }
                    }
                }
            }

            // Cleanup Vaults
            val svRef = firestore!!.collection("users").document(user.uid).collection("savings")
            val svSnapshot = svRef.get().await()
            val vaultGroups = mutableMapOf<String, MutableList<SavingsVault>>()
            for (doc in svSnapshot.documents) {
                val data = doc.data ?: continue
                try {
                    val sv = SavingsVault.fromMap(data, doc.id)
                    val key = sv.assetType
                    if (!vaultGroups.containsKey(key)) vaultGroups[key] = mutableListOf()
                    vaultGroups[key]!!.add(sv)
                } catch(e: Exception) {}
            }
            for ((_, svs) in vaultGroups) {
                if (svs.size > 1) {
                    val sorted = svs.sortedByDescending { it.updatedAt }
                    val duplicates = sorted.drop(1)
                    for (dup in duplicates) {
                        svRef.document(dup.uuid).delete().await()
                        dao.getAllSavingsVaults().find { it.uuid == dup.uuid }?.let { dao.deleteSavingsVaultById(it.id) }
                    }
                }
            }

            // Cleanup Transactions
            val txRef = firestore!!.collection("users").document(user.uid).collection("transactions")
            val txSnapshot = txRef.get().await()
            val txGroups = mutableMapOf<String, MutableList<Transaction>>()
            for (doc in txSnapshot.documents) {
                val data = doc.data ?: continue
                try {
                    val tx = Transaction.fromMap(data, doc.id)
                    val key = "${tx.type}_${tx.categoryName}_${tx.amount}_${tx.date}_${tx.note}"
                    if (!txGroups.containsKey(key)) txGroups[key] = mutableListOf()
                    txGroups[key]!!.add(tx)
                } catch(e: Exception) {}
            }
            for ((_, txs) in txGroups) {
                if (txs.size > 1) {
                    val sorted = txs.sortedByDescending { it.updatedAt }
                    val duplicates = sorted.drop(1)
                    for (dup in duplicates) {
                        txRef.document(dup.uuid).delete().await()
                        dao.getAllTransactions().find { it.uuid == dup.uuid }?.let { dao.deleteTransactionById(it.id) }
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to cleanup duplicates: ${e.message}")
        }
    }
}
