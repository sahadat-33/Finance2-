import re

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('val expenseTransactions = monthlyTransactions.filter { it.type == "EXPENSE" }', 'val expenseTransactions = monthlyTransactions.filter { it.type == "EXPENSE" && it.categoryName != "Savings" }')

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
