import re

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt.new', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if "var recv = 0.0" in line:
        break
    new_lines.append(line)

rest = """
                var recv = 0.0
                var exp = 0.0
                var sav = 0.0
                for (tx in monthTx) {
                    if (tx.type == "INCOME") {
                        recv += tx.amount
                    } else if (tx.type == "EXPENSE") {
                        if (tx.categoryName == "Savings") {
                            sav += tx.amount
                        } else {
                            exp += tx.amount
                        }
                    }
                }
                currentCarryover = recv - exp - sav
                
                m++
                if (m > 11) {
                    m = 0
                    y++
                }
            }
            resultList.sortedByDescending { it.date }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isUserSignedInFlow = MutableStateFlow(repository.authManager.isUserSignedIn)
    val isUserSignedInFlow: StateFlow<Boolean> = _isUserSignedInFlow.asStateFlow()

    private val _isEmailVerifiedFlow = MutableStateFlow(repository.authManager.isEmailVerified)
    val isEmailVerifiedFlow: StateFlow<Boolean> = _isEmailVerifiedFlow.asStateFlow()

    val allCategories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val allSavingsVault: StateFlow<List<SavingsVault>> = repository.getAllSavingsVault()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val dynamicVaultBalances: StateFlow<List<SavingsVault>> = allSavingsVault
        .map { vaults ->
            vaults.filter { it.amount > 0.0 || vaults.size == 1 }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyStatsFlow: StateFlow<MonthlyStats> = combine(
        allTransactions,
        _selectedCalendar
    ) { transactions, calendar ->
        val targetMonth = calendar.get(Calendar.MONTH)
        val targetYear = calendar.get(Calendar.YEAR)

        val monthlyTransactions = transactions.filter { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
            txCal.get(Calendar.MONTH) == targetMonth && txCal.get(Calendar.YEAR) == targetYear
        }.toMutableList()

        var totalEarnings = 0.0
        var totalExpenses = 0.0
        var totalSavingsContributed = 0.0

        for (tx in monthlyTransactions) {
            if (tx.type == "INCOME") {
                totalEarnings += tx.amount
            } else if (tx.type == "EXPENSE") {
                if (tx.categoryName == "Savings") {
                    totalSavingsContributed += tx.amount
                } else {
                    totalExpenses += tx.amount
                }
            }
        }

        val cashBalance = totalEarnings - totalExpenses - totalSavingsContributed

        val expenseTransactions = monthlyTransactions.filter { it.type == "EXPENSE" }
        val categoryExpenseMap = expenseTransactions.groupBy { it.categoryName }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        val sortedCategoryExpenses = categoryExpenseMap.entries
            .map { CategoryAmount(it.key, it.value) }
            .sortedByDescending { it.amount }

        val earningTransactions = monthlyTransactions.filter { it.type == "INCOME" }
        val categoryEarningMap = earningTransactions.groupBy { it.categoryName }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        val sortedCategoryEarnings = categoryEarningMap.entries
            .map { CategoryAmount(it.key, it.value) }
            .sortedByDescending { it.amount }

        MonthlyStats(
            totalEarnings = totalEarnings,
            totalExpenses = totalExpenses,
            totalSavingsContributed = totalSavingsContributed,
            cashBalance = cashBalance,
            transactions = monthlyTransactions,
            categoryExpenses = sortedCategoryExpenses,
            categoryEarnings = sortedCategoryEarnings
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlyStats(0.0, 0.0, 0.0, 0.0, emptyList(), emptyList(), emptyList()))

    fun refreshAuthState() {
        _isUserSignedInFlow.value = repository.authManager.isUserSignedIn
        _isEmailVerifiedFlow.value = repository.authManager.isEmailVerified
    }
    
    suspend fun createAccount(email: String, pass: String, username: String): Boolean {
        val success = repository.createAccount(email, pass, username)
        if (success) {
            refreshAuthState()
        }
        return success
    }

    suspend fun login(email: String, pass: String): Boolean {
        val success = repository.login(email, pass)
        if (success) {
            refreshAuthState()
        }
        return success
    }

    suspend fun sendPasswordReset(email: String): Boolean {
        return repository.sendPasswordReset(email)
    }

    suspend fun updateUsername(username: String): Boolean {
        return repository.updateUsername(username)
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            setOfflineGuest(false)
            refreshAuthState()
        }
    }
    
    suspend fun checkEmailVerification(): Boolean {
        val verified = repository.authManager.checkEmailVerification()
        _isEmailVerifiedFlow.value = verified
        return verified
    }
    
    val currentUserName: String?
        get() = repository.authManager.currentUser?.displayName ?: "User"

    val currentUserEmail: String?
        get() = repository.authManager.currentUser?.email

    fun nextMonth() {
        val next = _selectedCalendar.value.clone() as Calendar
        next.add(Calendar.MONTH, 1)
        _selectedCalendar.value = next
    }

    fun previousMonth() {
        val prev = _selectedCalendar.value.clone() as Calendar
        prev.add(Calendar.MONTH, -1)
        _selectedCalendar.value = prev
    }

    fun addTransaction(type: String, categoryName: String, amount: Double, date: Long, note: String, receiptImageUri: String? = null) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    type = type,
                    categoryName = categoryName,
                    amount = amount,
                    date = date,
                    note = note,
                    receiptImageUri = receiptImageUri
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addCategory(name: String, type: String) {
        viewModelScope.launch {
            repository.insertCategory(
                Category(name = name, type = type, isDefault = false)
            )
        }
    }

    fun deleteCategory(categoryId: Int) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
        }
    }

    fun addSavingsVault(assetType: String, amount: Double) {
        viewModelScope.launch {
            repository.insertSavingsVault(SavingsVault(assetType = assetType, amount = amount))
        }
    }

    fun deleteSavingsVault(id: Int) {
        viewModelScope.launch {
            repository.deleteSavingsVault(id)
        }
    }

    fun getYearlySummary(year: Int): YearlySummary {
        val allTx = allTransactions.value
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val monthlyData = mutableListOf<YearlySummaryRow>()
        
        var totalRecv = 0.0
        var totalExp = 0.0
        var totalSav = 0.0
        var totalCash = 0.0
        
        var maxCash = Double.MIN_VALUE
        var minCash = Double.MAX_VALUE
        
        for (monthIndex in 0..11) {
            val monthTx = allTx.filter { tx ->
                val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
                cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == monthIndex
            }
            
            var recv = 0.0
            var exp = 0.0
            var sav = 0.0
            
            for (tx in monthTx) {
                if (tx.type == "INCOME") {
                    recv += tx.amount
                } else if (tx.type == "EXPENSE") {
                    if (tx.categoryName == "Savings") {
                        sav += tx.amount
                    } else {
                        exp += tx.amount
                    }
                }
            }
            
            val cash = recv - exp - sav
            
            totalRecv += recv
            totalExp += exp
            totalSav += sav
            totalCash = totalRecv - totalExp - totalSav
            
            if (cash > maxCash) maxCash = cash
            if (cash < minCash) minCash = cash
            
            monthlyData.add(YearlySummaryRow(months[monthIndex], recv, exp, sav, cash))
        }
        
        val avgRow = YearlySummaryRow("Average", totalRecv/12, totalExp/12, totalSav/12, totalCash/12)
        val maxRow = YearlySummaryRow("Max", monthlyData.maxOf { it.received }, monthlyData.maxOf { it.expenses }, monthlyData.maxOf { it.savings }, maxCash)
        val minRow = YearlySummaryRow("Min", monthlyData.minOf { it.received }, monthlyData.minOf { it.expenses }, monthlyData.minOf { it.savings }, minCash)
        val ttlRow = YearlySummaryRow("Total", totalRecv, totalExp, totalSav, totalCash)
        
        return YearlySummary(monthlyData, ttlRow, avgRow, maxRow, minRow)
    }

    fun startNewYear(carryoverCash: Double, context: android.content.Context) {
        val currCal = _selectedCalendar.value
        val year = currCal.get(Calendar.YEAR)
        
        try {
            val summary = getYearlySummary(year)
            val file = java.io.File(context.filesDir, "archive_$year.json")
            val content = buildString {
                append("[\\n")
                summary.monthlyData.forEachIndexed { index, row ->
                    append("  {\\n")
                    append("    \\"month\\": \\"${row.month}\\",\\n")
                    append("    \\"received\\": ${row.received},\\n")
                    append("    \\"expenses\\": ${row.expenses},\\n")
                    append("    \\"savings\\": ${row.savings},\\n")
                    append("    \\"cash\\": ${row.cash}\\n")
                    append("  }${if (index < summary.monthlyData.size - 1) "," else ""}\\n")
                }
                append("]")
            }
            file.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        viewModelScope.launch {
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
        }
    }
}

data class YearlySummaryRow(
    val month: String,
    val received: Double,
    val expenses: Double,
    val savings: Double,
    val cash: Double
)

data class YearlySummary(
    val monthlyData: List<YearlySummaryRow>,
    val totalRow: YearlySummaryRow,
    val averageRow: YearlySummaryRow,
    val maxRow: YearlySummaryRow,
    val minRow: YearlySummaryRow
)

data class CategoryAmount(
    val categoryName: String,
    val amount: Double
)

data class MonthlyStats(
    val totalEarnings: Double,
    val totalExpenses: Double,
    val totalSavingsContributed: Double,
    val cashBalance: Double,
    val transactions: List<Transaction>,
    val categoryExpenses: List<CategoryAmount>,
    val categoryEarnings: List<CategoryAmount>
)
"""

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.writelines(new_lines)
    f.write(rest)
