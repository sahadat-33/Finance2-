import re

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

# find the exact loop using regex
pattern = re.compile(r'var recv = 0\.0.*?val cash = recv \+ carry - exp - sav', re.DOTALL)
match = pattern.search(content)
if match:
    new_loop = """var recv = 0.0
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
    content = content.replace(match.group(0), new_loop)
    with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Not found")
