with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if i == 573 and line.strip() == "}":
        new_lines.append(line) # append the one that closes DashboardScreen?
        # wait!
        pass
        
with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.writelines(lines)
