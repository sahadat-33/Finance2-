with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

old_monthly_loop = """        var totalEarnings = 0.0
        var totalExpenses = 0.0
        var totalSavingsContributed = 0.0
        var carryover = 0.0

        for (tx in monthlyTransactions) {
            if (tx.type == "INCOME") {
                if (tx.categoryName == "Savings") {
                    totalSavingsContributed -= tx.amount
                } else if (tx.categoryName == "Last Month Carryover") {
                    carryover += tx.amount
                } else {
                    totalEarnings += tx.amount
                }
            } else if (tx.type == "EXPENSE") {
                if (tx.categoryName == "Savings") {
                    totalSavingsContributed += tx.amount
                } else {
                    totalExpenses += tx.amount
                }
            }
        }

        val cashBalance = totalEarnings + carryover - totalExpenses - totalSavingsContributed"""

new_monthly_loop = """        var totalEarnings = 0.0
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

        val cashBalance = totalEarnings - totalExpenses - totalSavingsContributed"""

content = content.replace(old_monthly_loop, new_monthly_loop)

old_yearly_loop = """            var recv = 0.0
            var exp = 0.0
            var sav = 0.0
            var carry = 0.0

            for (tx in monthTx) {
                if (tx.type == "INCOME") {
                    if (tx.categoryName == "Savings") {
                        sav -= tx.amount
                    } else if (tx.categoryName == "Last Month Carryover") {
                        carry += tx.amount
                    } else {
                        recv += tx.amount
                    }
                } else if (tx.type == "EXPENSE") {
                    if (tx.categoryName == "Savings") {
                        sav += tx.amount
                    } else {
                        exp += tx.amount
                    }
                }
            }

            val cash = recv + carry - exp - sav"""

new_yearly_loop = """            var recv = 0.0
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

            val cash = recv - exp - sav"""

content = content.replace(old_yearly_loop, new_yearly_loop)

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
