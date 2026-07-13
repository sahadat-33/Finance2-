import re

with open('app/src/main/java/com/example/data/CloudSyncManager.kt', 'r') as f:
    content = f.read()

# Pass doc.id
content = content.replace('val remoteTx = Transaction.fromMap(data)', 'val remoteTx = Transaction.fromMap(data, doc.id)')
content = content.replace('val remoteSv = SavingsVault.fromMap(data)', 'val remoteSv = SavingsVault.fromMap(data, doc.id)')
content = content.replace('val remoteCat = Category.fromMap(data)', 'val remoteCat = Category.fromMap(data, doc.id)')

# Replace cleanupDuplicateCategories with cleanupDuplicates
old_cleanup = """    suspend fun cleanupDuplicateCategories() {
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
    }"""

new_cleanup = """    suspend fun cleanupDuplicates() {
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
    }"""

content = content.replace(old_cleanup, new_cleanup)

with open('app/src/main/java/com/example/data/CloudSyncManager.kt', 'w') as f:
    f.write(content)
