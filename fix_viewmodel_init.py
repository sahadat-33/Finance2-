import re

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("        repository.initializeDatabaseIfEmpty()\n", "")

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
