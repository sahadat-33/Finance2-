import re

with open('app/src/main/java/com/example/ui/OthersScreen.kt', 'r') as f:
    content = f.read()

# We need to replace the entire "Row" that contains Check for Updates
# Let's find it.
check_updates_start = content.find('Row(', content.find('Text("Change Your Currency"'))

if check_updates_start != -1:
    # find the matching closing bracket for the Row
    row_idx = check_updates_start
    open_brackets = 0
    found_first = False
    for i in range(check_updates_start, len(content)):
        if content[i] == '{':
            open_brackets += 1
            found_first = True
        elif content[i] == '}':
            open_brackets -= 1
        
        if found_first and open_brackets == 0:
            # We found the end of the Row... wait, Row() is a function call
            pass

# Let's just do a clean replacement by splitting and joining
lines = content.split('\n')
new_lines = []
skip = False
for line in lines:
    if 'if (!isCheckingUpdate) {' in line:
        # this is the start of the clickable block
        pass
        
    if 'var updateDialogMessage' in line or 'var updateDialogUrl' in line or 'var showUpdateDialog' in line or 'var isCheckingUpdate' in line:
        continue
        
    if 'if (showUpdateDialog) {' in line:
        skip = True
    
    if skip and line.strip() == '}':
        # wait, the showUpdateDialog block has multiple nested braces
        pass
