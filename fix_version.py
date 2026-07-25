import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

content = content.replace('versionName = "4.0.6"', 'versionName = "4.0.7"')

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
