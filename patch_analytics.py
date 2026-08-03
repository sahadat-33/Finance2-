import re

with open('app/src/main/java/com/example/ui/AnalyticsScreen.kt', 'r') as f:
    content = f.read()

# First add missing imports
imports_to_add = """
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import java.util.Calendar
""".strip()

# Insert after existing imports, just find the last import
last_import_idx = content.rfind("import ")
end_of_last_import = content.find("\n", last_import_idx) + 1
content = content[:end_of_last_import] + imports_to_add + "\n" + content[end_of_last_import:]

# Add MonthBarData and LegendDot
data_classes = """
data class MonthBarData(
    val label: String,
    val income: Double,
    val expense: Double,
    val month: Int,
    val year: Int
)

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
    }
}
"""

content = content + "\n" + data_classes

# Rewrite AnalyticsScreen composable
old_analytics_screen = """@Composable
fun AnalyticsScreen(viewModel: FinanceViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IncomeSpentAnalysisCard(viewModel = viewModel)
    }
}"""

new_analytics_screen = """@Composable
fun AnalyticsScreen(viewModel: FinanceViewModel) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val selectedCalendar by viewModel.selectedCalendar.collectAsState()
    val selectedYear = remember(selectedCalendar) { selectedCalendar.get(Calendar.YEAR) }
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    fun formatTaka(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        return "$currencySymbol${formatter.format(amount)}"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IncomeSpentAnalysisCard(viewModel = viewModel)

        val monthLabels = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        val barData = remember(allTransactions, selectedYear) {
            monthLabels.mapIndexed { index, label ->
                val monthTx = allTransactions.filter { tx ->
                    val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
                    cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == index
                }
                MonthBarData(
                    label = label,
                    income = monthTx.filter { it.type == "INCOME" }.sumOf { it.amount },
                    expense = monthTx.filter { it.type == "EXPENSE" && it.categoryName != "Savings" }.sumOf { it.amount },
                    month = index,
                    year = selectedYear
                )
            }
        }
        val maxValue = remember(barData) { barData.maxOf { maxOf(it.income, it.expense) }.coerceAtLeast(1.0) }

        Spacer(Modifier.height(20.dp))
        Text(
            "$selectedYear Overview",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            LegendDot(color = MaterialTheme.colorScheme.primary, label = "Income")
            LegendDot(color = MaterialTheme.colorScheme.error, label = "Expenses")
        }

        val animatedProgress by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(900, easing = FastOutSlowInEasing),
            label = "bar_grow"
        )
        val incomeColor = MaterialTheme.colorScheme.primary
        val expenseColor = MaterialTheme.colorScheme.error

        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Canvas(
                modifier = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val slotWidth = chartWidth / 12f
                val barWidth = (slotWidth * 0.32f).coerceAtLeast(4f)
                val labelPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 26f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                barData.forEachIndexed { i, data ->
                    val centerX = slotWidth * i + slotWidth / 2f
                    val incomeH = ((data.income / maxValue) * chartHeight * 0.85f * animatedProgress).toFloat()
                    drawRoundRect(
                        color = incomeColor,
                        topLeft = Offset(centerX - barWidth - 2f, chartHeight - incomeH),
                        size = Size(barWidth, incomeH),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    val expenseH = ((data.expense / maxValue) * chartHeight * 0.85f * animatedProgress).toFloat()
                    drawRoundRect(
                        color = expenseColor,
                        topLeft = Offset(centerX + 2f, chartHeight - expenseH),
                        size = Size(barWidth, expenseH),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawContext.canvas.nativeCanvas.drawText(data.label, centerX, chartHeight + 26f, labelPaint)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Monthly Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            barData.forEach { data ->
                val net = data.income - data.expense
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp).width(110.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(data.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Text("In: ${formatTaka(data.income)}", style = MaterialTheme.typography.bodySmall, color = incomeColor)
                        Text("Out: ${formatTaka(data.expense)}", style = MaterialTheme.typography.bodySmall, color = expenseColor)
                        Text(
                            "Net: ${if (net >= 0) "+" else ""}${formatTaka(net)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (net >= 0) incomeColor else expenseColor
                        )
                    }
                }
            }
        }
    }
}"""

content = content.replace(old_analytics_screen, new_analytics_screen)

# Let's clean up any duplicate imports using a small set logic for imports
lines = content.split('\n')
import_set = set()
clean_lines = []
for line in lines:
    if line.startswith('import '):
        if line not in import_set:
            import_set.add(line)
            clean_lines.append(line)
    else:
        clean_lines.append(line)

with open('app/src/main/java/com/example/ui/AnalyticsScreen.kt', 'w') as f:
    f.write('\n'.join(clean_lines))

