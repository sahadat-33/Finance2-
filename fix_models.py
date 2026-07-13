import re
with open('app/src/main/java/com/example/data/ModelsAndDao.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'fun fromMap(map: Map<String, Any>): Category = Category(',
    'fun fromMap(map: Map<String, Any>, docId: String? = null): Category = Category('
)
content = content.replace(
    'uuid = map["uuid"] as? String ?: java.util.UUID.randomUUID().toString()',
    'uuid = map["uuid"] as? String ?: docId ?: java.util.UUID.randomUUID().toString()'
)

content = content.replace(
    'fun fromMap(map: Map<String, Any>): Transaction = Transaction(',
    'fun fromMap(map: Map<String, Any>, docId: String? = null): Transaction = Transaction('
)

content = content.replace(
    'fun fromMap(map: Map<String, Any>): SavingsVault = SavingsVault(',
    'fun fromMap(map: Map<String, Any>, docId: String? = null): SavingsVault = SavingsVault('
)

with open('app/src/main/java/com/example/data/ModelsAndDao.kt', 'w') as f:
    f.write(content)
