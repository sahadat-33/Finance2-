with open('app/src/main/java/com/example/data/CloudSyncManager.kt', 'r') as f:
    content = f.read()

new_fetch = """    suspend fun fetchFromCloud(): Boolean {
        if (firestore == null || auth == null) return false
        val user = auth?.currentUser ?: return false
        try { user.reload().await() } catch(e: Exception) {}
        if (!user.isEmailVerified) return false
        
        try {
            // First fetch cloud data, then merge.
            val transactionsSnapshot = firestore!!.collection("users").document(user.uid).collection("transactions").get().await()
            val localTx = dao.getAllTransactions().associateBy { it.uuid }
            for (doc in transactionsSnapshot.documents) {
                val data = doc.data ?: continue
                try {
                    val remoteTx = Transaction.fromMap(data)
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
                    val remoteSv = SavingsVault.fromMap(data)
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
                    val remoteCat = Category.fromMap(data)
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
    }"""

import re
content = re.sub(r'    suspend fun fetchFromCloud\(\).*?Log\.e\("CloudSync", "Failed to fetch from cloud: \$\{e\.message\}"\)\n        }\n    }', new_fetch, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/data/CloudSyncManager.kt', 'w') as f:
    f.write(content)
