import re

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

pattern = re.compile(r'val allTransactions: StateFlow<List<Transaction>> = repository\.getAllTransactions\(\).*?\.stateIn\(viewModelScope, SharingStarted\.WhileSubscribed\(5000\), emptyList\(\)\)', re.DOTALL)

new_code = """val allTransactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .map { rawTx ->
            rawTx.sortedByDescending { it.date }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())"""

content = re.sub(pattern, new_code, content)

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)

