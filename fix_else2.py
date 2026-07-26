with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    content = f.read()

old_end = "        Spacer(modifier = Modifier.height(24.dp))\n    }\n}"
new_end = "        Spacer(modifier = Modifier.height(24.dp))\n    }\n    }\n}"

if old_end in content:
    content = content.replace(old_end, new_end)
else:
    print("Could not find the end block!")

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.write(content)
