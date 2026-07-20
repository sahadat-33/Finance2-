import re

with open('app/src/main/java/com/example/ui/OthersScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("fun OthersScreen(viewModel: FinanceViewModel, onBack: () -> Unit) {", "fun OthersScreen(viewModel: FinanceViewModel, onBack: () -> Unit, onNavigateToUpdate: () -> Unit = {}) {")

# Find the "Check for Updates" Row
# Let's just use regex to replace it
check_for_updates_block_regex = r"Row\([^)]*clickable\s*\{[^}]*isCheckingUpdate[^}]*\}[^)]*\)\s*\{\s*Text\(\"Check for Updates\"[^\}]+\}\s*\}"

import re

# It's better to rewrite it via a script
