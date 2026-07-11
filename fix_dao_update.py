import re
with open('app/src/main/java/com/example/data/ModelsAndDao.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'SET amount = :amount, updatedAt = strftime("%s", "now") * 1000 WHERE',
    'SET amount = :amount, updatedAt = :updatedAt WHERE'
)
content = content.replace(
    'suspend fun updateSavingsAmount(assetType: String, amount: Double)',
    'suspend fun updateSavingsAmount(assetType: String, amount: Double, updatedAt: Long = System.currentTimeMillis())'
)

with open('app/src/main/java/com/example/data/ModelsAndDao.kt', 'w') as f:
    f.write(content)
