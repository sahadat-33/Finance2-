import re

with open('app/src/main/java/com/example/data/ModelsAndDao.kt', 'r') as f:
    content = f.read()

# Replace multiple updatedAt lines with a single one
content = re.sub(r'(updatedAt = \(map\["updatedAt"\] as\? Number\)\?\.toLong\(\) \?\: System\.currentTimeMillis\(\),\s*)+', r'updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),\n            ', content)

with open('app/src/main/java/com/example/data/ModelsAndDao.kt', 'w') as f:
    f.write(content)
