import re

with open('app/src/main/java/com/example/data/ModelsAndDao.kt', 'r') as f:
    content = f.read()

# Update Category toMap
content = content.replace('mapOf("id" to id, "name" to name, "type" to type, "isDefault" to isDefault, "uuid" to uuid)', 'mapOf("id" to id, "name" to name, "type" to type, "isDefault" to isDefault, "updatedAt" to updatedAt, "uuid" to uuid)')
# Update Category fromMap
content = content.replace('uuid = map["uuid"] as? String ?: java.util.UUID.randomUUID().toString()', 'updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),\n            uuid = map["uuid"] as? String ?: java.util.UUID.randomUUID().toString()')

# Update Transaction toMap
content = content.replace('"date" to date, "note" to note, "receiptImageUri" to (receiptImageUri ?: ""), "uuid" to uuid', '"date" to date, "note" to note, "receiptImageUri" to (receiptImageUri ?: ""), "updatedAt" to updatedAt, "uuid" to uuid')
# Update Transaction fromMap
content = content.replace('uuid = map["uuid"] as? String ?: java.util.UUID.randomUUID().toString()', 'updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),\n            uuid = map["uuid"] as? String ?: java.util.UUID.randomUUID().toString()')

# Update SavingsVault toMap
content = content.replace('mapOf("id" to id, "assetType" to assetType, "amount" to amount, "uuid" to uuid)', 'mapOf("id" to id, "assetType" to assetType, "amount" to amount, "updatedAt" to updatedAt, "uuid" to uuid)')
# Update SavingsVault fromMap
content = content.replace('uuid = map["uuid"] as? String ?: java.util.UUID.randomUUID().toString()', 'updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),\n            uuid = map["uuid"] as? String ?: java.util.UUID.randomUUID().toString()')

# Update Database version
content = content.replace('version = 5', 'version = 6')

with open('app/src/main/java/com/example/data/ModelsAndDao.kt', 'w') as f:
    f.write(content)
