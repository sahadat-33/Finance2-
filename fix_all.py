import re

# Fix AccountSettingsScreen.kt
with open('app/src/main/java/com/example/AccountSettingsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("delete().kotlinx.coroutines.tasks.await()", "delete().await()")

with open('app/src/main/java/com/example/AccountSettingsScreen.kt', 'w') as f:
    f.write(content)


# Fix FinanceViewModel.kt
with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

bad_update_email = """    suspend fun updateEmail(newEmail: String): Boolean {
        val success = repository.updateEmail(newEmail)
        if (success) {
            _currentUserEmail.value = newEmail
        }
        return success
    }"""
good_update_email = """    suspend fun updateEmail(newEmail: String): Boolean {
        return repository.updateEmail(newEmail)
    }"""

content = content.replace(bad_update_email, good_update_email)

bad_current_user_id = """    val currentUserId: String?
        get() = repository.currentUserId"""
good_current_user_id = """    val currentUserId: String?
        get() = repository.authManager.auth?.currentUser?.uid"""

content = content.replace(bad_current_user_id, good_current_user_id)

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)

