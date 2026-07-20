import re

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'r') as f:
    lines = f.readlines()

def get_block_end(lines, start_idx):
    # simple brace matching
    open_braces = 0
    found_first = False
    for i in range(start_idx, len(lines)):
        open_braces += lines[i].count('{')
        open_braces -= lines[i].count('}')
        if lines[i].count('{') > 0:
            found_first = True
        if found_first and open_braces == 0:
            return i
    return -1

new_lines = []
skip_until = -1
for i, line in enumerate(lines):
    if i <= skip_until:
        continue
    
    if "val isPinEnabled by viewModel.isPinEnabled.collectAsState()" in line:
        continue
    if "var showPinDialog by remember { mutableStateOf(false) }" in line:
        continue
    if "var pinDialogMode by remember { mutableStateOf(\"SET\") }" in line:
        continue
    
    if "if (showPinDialog) {" in line:
        skip_until = get_block_end(lines, i)
        continue

    if "var showCurrencyDialog by remember" in line:
        continue
    if "var showOthersDialog by remember" in line:
        continue
    if "var updateDialogMessage by remember" in line:
        continue
    if "var updateDialogUrl by remember" in line:
        continue
    if "var showUpdateDialog by remember" in line:
        continue
    if "var isCheckingUpdate by remember" in line:
        continue

    if "if (showOthersDialog) {" in line:
        skip_until = get_block_end(lines, i)
        continue
        
    if "if (showUpdateDialog) {" in line:
        skip_until = get_block_end(lines, i)
        continue

    if "if (showCurrencyDialog) {" in line:
        skip_until = get_block_end(lines, i)
        continue

    new_lines.append(line)

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'w') as f:
    f.writelines(new_lines)

