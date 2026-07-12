import re

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

# Fix dynamicVaultBalances
old_dynamic = """    val dynamicVaultBalances: StateFlow<List<SavingsVault>> = combine(
        allTransactions,
        allSavingsVault
    ) { transactions, vaults ->
        vaults.map { vault ->
            var currentBalance = 0.0
            transactions.filter { it.categoryName == "Savings" }.forEach { tx ->
                val matchTo = Regex("^To (.*) Vault").find(tx.note)
                val matchFrom = Regex("^From (.*) Vault").find(tx.note)
                val targetVaultName = matchTo?.groupValues?.get(1) ?: matchFrom?.groupValues?.get(1)
                
                if (targetVaultName == vault.assetType) {
                    if (tx.type == "EXPENSE") {
                        currentBalance += tx.amount
                    } else if (tx.type == "INCOME") {
                        currentBalance -= tx.amount
                    }
                }
            }
            vault.copy(amount = currentBalance)
        }.filter { it.amount > 0.0 || vaults.size == 1 } // Optionally filter out 0 balances if needed, or keep all.
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())"""

new_dynamic = """    val dynamicVaultBalances: StateFlow<List<SavingsVault>> = allSavingsVault
        .map { vaults ->
            vaults.filter { it.amount > 0.0 || vaults.size == 1 }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())"""

content = content.replace(old_dynamic, new_dynamic)

# Fix addTransaction redundancy
old_add = """                    receiptImageUri = receiptImageUri
                )
            )
            
            if (categoryName == "Savings") {
                val matchTo = Regex("^To (.*) Vault").find(note)
                val matchFrom = Regex("^From (.*) Vault").find(note)
                val targetVaultName = matchTo?.groupValues?.get(1) ?: matchFrom?.groupValues?.get(1)
                
                if (targetVaultName != null) {
                    val vaults = allSavingsVault.value
                    val vault = vaults.find { it.assetType == targetVaultName }
                    if (vault != null) {
                        val newBalance = if (type == "EXPENSE") {
                            vault.amount + amount
                        } else {
                            vault.amount - amount
                        }
                        repository.insertSavingsVault(vault.copy(amount = newBalance, updatedAt = System.currentTimeMillis()))
                    }
                }
            }
        }
    }"""

new_add = """                    receiptImageUri = receiptImageUri
                )
            )
        }
    }"""

content = content.replace(old_add, new_add)

# Add missing import for map if necessary
if 'import kotlinx.coroutines.flow.map' not in content:
    content = content.replace('import kotlinx.coroutines.flow.combine', 'import kotlinx.coroutines.flow.combine\nimport kotlinx.coroutines.flow.map')

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
