import re

for filename in ['app/src/main/java/com/example/ui/YearlySummaryScreen.kt', 'app/src/main/java/com/example/ui/SettingsScreen.kt']:
    with open(filename, 'r') as f:
        content = f.read()

    old_call = "viewModel.getYearlySummary(selectedYear)"
    new_call = "viewModel.getYearlySummary(selectedYear, allTransactions)"

    content = content.replace(old_call, new_call)

    with open(filename, 'w') as f:
        f.write(content)
