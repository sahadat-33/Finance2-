with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

if lines[-1].strip() == "}":
    lines = lines[:-1]

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.writelines(lines)
