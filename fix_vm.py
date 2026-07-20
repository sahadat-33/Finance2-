import re

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

# Let's find "fun startNewYear" and the end of it
# We can just insert our methods before "fun startNewYear"
methods_to_insert = """
    suspend fun reauthenticate(password: String): Boolean {
        return repository.reauthenticate(password)
    }

    suspend fun updateEmail(newEmail: String): Boolean {
        val success = repository.updateEmail(newEmail)
        if (success) {
            _currentUserEmail.value = newEmail
        }
        return success
    }

    suspend fun deleteAccount(): Boolean {
        return repository.deleteAccount()
    }
    
    val currentUserId: String?
        get() = repository.currentUserId
"""

content = content.replace("    fun startNewYear", methods_to_insert + "\n    fun startNewYear")

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
