import re

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('append("[\\n")' , 'append("[\\n")')
# Wait, let's just replace the whole getYearlySummary string writing with proper Kotlin syntax.

new_str = '''            val content = buildString {
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
            }'''

# find val content = buildString { ... }
import re
pattern = re.compile(r'val content = buildString \{.*?append\("\]"\)\n            \}', re.DOTALL)
content = pattern.sub(new_str, content)

with open('app/src/main/java/com/example/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
