import re

file_path = "app/src/main/java/com/example/ui/EditTransactionDialog.kt"

with open(file_path, "r") as f:
    content = f.read()

content = content.replace("fun AddTransactionDialog(", "fun EditTransactionDialog(")
content = content.replace(
    "viewModel: FinanceViewModel\n)",
    "viewModel: FinanceViewModel,\n    transaction: com.example.data.Transaction\n)"
)

content = content.replace(
    'var txType by remember { mutableStateOf("EXPENSE") } // "INCOME", "EXPENSE", "SAVINGS_TRANSFER"',
    '''var txType by remember {
        mutableStateOf(
            when {
                transaction.type == "INCOME" -> "INCOME"
                transaction.categoryName == "Savings" -> "SAVINGS_TRANSFER"
                else -> "EXPENSE"
            }
        )
    }'''
)

content = content.replace(
    'var amountStr by remember { mutableStateOf("") }',
    'var amountStr by remember { mutableStateOf(transaction.amount.toString()) }'
)

content = content.replace(
    'var noteStr by remember { mutableStateOf("") }',
    'var noteStr by remember { mutableStateOf(transaction.note) }'
)

content = content.replace(
    '''    var selectedDateMs by remember { 
        mutableStateOf(
            Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.YEAR, appActiveMonth.get(Calendar.YEAR))
                set(Calendar.MONTH, appActiveMonth.get(Calendar.MONTH))
            }.timeInMillis
        ) 
    }''',
    '    var selectedDateMs by remember { mutableStateOf(transaction.date) }'
)

content = content.replace(
    'var selectedCategoryName by remember { mutableStateOf("") }',
    'var selectedCategoryName by remember { mutableStateOf(transaction.categoryName) }'
)

content = content.replace(
    'var receiptImageUri by remember { mutableStateOf<String?>(null) }',
    'var receiptImageUri by remember { mutableStateOf(transaction.receiptImageUri) }'
)

content = content.replace(
    'Text("Add Transaction"',
    'Text("Edit Transaction"'
)

content = content.replace(
    '''viewModel.addTransaction(
                                type = finalType,
                                categoryName = finalCategory,
                                amount = amountVal,
                                date = selectedDateMs,
                                note = finalNote,
                                receiptImageUri = receiptImageUri
                            )''',
    '''viewModel.editTransaction(
                                transaction.copy(
                                    type = finalType,
                                    categoryName = finalCategory,
                                    amount = amountVal,
                                    date = selectedDateMs,
                                    note = finalNote,
                                    receiptImageUri = receiptImageUri,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )'''
)

with open(file_path, "w") as f:
    f.write(content)
