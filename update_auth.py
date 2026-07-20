import re

auth_file = 'app/src/main/java/com/example/data/FirebaseAuthManager.kt'
with open(auth_file, 'r') as f:
    content = f.read()

new_methods = """
    suspend fun reauthenticate(password: String): Boolean {
        val user = auth?.currentUser ?: return false
        val email = user.email ?: return false
        return try {
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateEmail(newEmail: String): Boolean {
        val user = auth?.currentUser ?: return false
        return try {
            user.updateEmail(newEmail).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteAccount(): Boolean {
        val user = auth?.currentUser ?: return false
        return try {
            user.delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
"""

content = content.replace("}\n", new_methods + "\n", 1) # This replaces the LAST closing brace of the file? No, we need to be careful.

# Let's do it safely
content = re.sub(r'}\s*$', new_methods, content)

with open(auth_file, 'w') as f:
    f.write(content)
