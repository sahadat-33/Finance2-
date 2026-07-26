import re

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

# Fix 1: startNewYear
old_launch = """        viewModelScope.launch {
            for (tx in allTransactions.value) {
                repository.deleteTransaction(tx)
            }
            
            val nextYearCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year + 1)
                set(Calendar.MONTH, Calendar.JANUARY)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            repository.insertTransaction(
                Transaction(
                    type = "INCOME",
                    categoryName = "Last Month Carryover",
                    amount = carryoverCash,
                    date = nextYearCal.timeInMillis,
                    note = "New year starting balance"
                )
            )
            _selectedCalendar.value = nextYearCal
        }"""
new_launch = """        viewModelScope.launch {
    // Batch-delete directly through DAO to avoid triggering one sync per transaction.
    // Categories and savings vaults are left completely untouched.
    repository.dao.deleteAllTransactions()
    val nextYearCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year + 1)
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    // Insert the single carryover entry directly via DAO (no sync trigger)
    repository.dao.insertTransaction(
        Transaction(
            type = "INCOME",
            categoryName = "Last Month Carryover",
            amount = carryoverCash,
            date = nextYearCal.timeInMillis,
            note = "New year starting balance"
        )
    )
    // One single sync for the entire operation
    repository.triggerManualSync()
    _selectedCalendar.value = nextYearCal
}"""

content = content.replace(old_launch, new_launch)

# Fix 2: getYearlySummary
old_summary = """    fun getYearlySummary(year: Int): YearlySummary {
        val allTx = allTransactions.value"""
new_summary = """    fun getYearlySummary(year: Int, transactions: List<Transaction> = allTransactions.value): YearlySummary {
        val allTx = transactions"""
content = content.replace(old_summary, new_summary)

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
