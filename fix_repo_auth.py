import re

auth_file = 'app/src/main/java/com/example/data/FirebaseAuthManager.kt'
with open(auth_file, 'r') as f:
    auth_content = f.read()

auth_content = auth_content.replace("val isUserSignedIn: Boolean\n        get() = auth?.currentUser != null", "val isUserSignedIn: Boolean\n        get() = auth?.currentUser != null\n\n    val currentUserId: String?\n        get() = auth?.currentUser?.uid")

with open(auth_file, 'w') as f:
    f.write(auth_content)

repo_file = 'app/src/main/java/com/example/data/FinanceRepository.kt'
with open(repo_file, 'r') as f:
    repo_content = f.read()

repo_content = repo_content.replace("val isUserSignedIn: Boolean\n        get() = authManager.isUserSignedIn", "val isUserSignedIn: Boolean\n        get() = authManager.isUserSignedIn\n\n    val currentUserId: String?\n        get() = authManager.currentUserId")

with open(repo_file, 'w') as f:
    f.write(repo_content)

