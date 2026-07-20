import re

with open('app/src/main/java/com/example/AccountSettingsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import com.google.firebase.firestore.FirebaseFirestore", "import com.google.firebase.firestore.FirebaseFirestore\nimport kotlinx.coroutines.tasks.await")

with open('app/src/main/java/com/example/AccountSettingsScreen.kt', 'w') as f:
    f.write(content)
