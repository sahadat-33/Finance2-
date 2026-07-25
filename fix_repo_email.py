import re

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

content = content.replace("suspend fun updateEmail(newEmail: String): Boolean = withContext(Dispatchers.IO) {", "suspend fun updateEmail(newEmail: String): String = withContext(Dispatchers.IO) {")

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content2 = f.read()

content2 = content2.replace("suspend fun updateEmail(newEmail: String): Boolean {", "suspend fun updateEmail(newEmail: String): String {")

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content2)
