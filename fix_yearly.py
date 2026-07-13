import re

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

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
