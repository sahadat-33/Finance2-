import re

repo_file = 'app/src/main/java/com/example/data/FinanceRepository.kt'
with open(repo_file, 'r') as f:
    content = f.read()

repo_methods = """
    suspend fun reauthenticate(password: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext authManager.reauthenticate(password)
    }

    suspend fun updateEmail(newEmail: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext authManager.updateEmail(newEmail)
    }

    suspend fun deleteAccount(): Boolean = withContext(Dispatchers.IO) {
        return@withContext authManager.deleteAccount()
    }
}"""
content = re.sub(r'}\s*$', repo_methods, content)
with open(repo_file, 'w') as f:
    f.write(content)

vm_file = 'app/src/main/java/com/example/viewmodel/FinanceViewModel.kt'
with open(vm_file, 'r') as f:
    content = f.read()

vm_methods = """
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
}"""
content = re.sub(r'}\s*$', vm_methods, content)
with open(vm_file, 'w') as f:
    f.write(content)

