import re

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

# Remove the function entirely
import re
content = re.sub(r'    suspend fun initializeDatabaseIfEmpty\(\) \{.*?\n    \}', '', content, flags=re.DOTALL)

# Also remove calls to it
content = content.replace("                initializeDatabaseIfEmpty()\n", "")

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(content)
