package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.viewmodel.FinanceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import java.util.Calendar

@Composable
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
}

@Composable
fun IncomeSpentAnalysisCard(viewModel: FinanceViewModel) {
    val stats by viewModel.monthlyStatsFlow.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val showAnalysisOnDashboard by viewModel.showAnalysisOnDashboard.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun formatTaka(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        return "$currencySymbol${formatter.format(amount)}"
    }

    // Charts Section Panel Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Income Spent Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(Color(0xFF424242), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Add to dashboard",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Switch(
                                checked = showAnalysisOnDashboard,
                                onCheckedChange = { isChecked ->
                                    viewModel.setShowAnalysisOnDashboard(isChecked)
                                    coroutineScope.launch {
                                        delay(300)
                                        showMenu = false
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Layout incorporating Donut Gauge Drawing
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Gauge Math
                val percentSpentVal = if (stats.totalEarnings > 0) {
                    (stats.totalExpenses / stats.totalEarnings)
                } else 0.0
                val percentInt = (percentSpentVal * 100).toInt().coerceIn(0, 100)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(4.dp)
                ) {
                    val rawSweep = (percentSpentVal * 360f).toFloat().coerceIn(0f, 360f)
                    val animatedSweep by animateFloatAsState(
                        targetValue = rawSweep,
                        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                        label = "donut_sweep"
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 12.dp.toPx()
                        // Leftover / Remaining backing circle (Lite Green)
                        drawCircle(
                            color = Color(0xFFBBECC4),
                            style = Stroke(stroke)
                        )
                        // Spent foreground arc (Lite Blue)
                        drawArc(
                            color = Color(0xFFA2C2FC),
                            startAngle = -90f,
                            sweepAngle = animatedSweep,
                            useCenter = false,
                            style = Stroke(stroke, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$percentInt%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Spent",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Stat explanations
                Column(
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFA2C2FC)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Spent: ${formatTaka(stats.totalExpenses)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFBBECC4)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Leftover: ${formatTaka((stats.totalEarnings - stats.totalExpenses - stats.totalSavingsContributed).coerceAtLeast(0.0))}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = "Monthly Savings: ${formatTaka(stats.totalSavingsContributed)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64B5F6)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Pie Chart / Colored weight list for Monthly Expenses by Category
            if (stats.categoryExpenses.isNotEmpty()) {
                Text(
                    text = "Monthly Expenses by Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Staggered bars (Visual segmented bar) representing each category weight
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                ) {
                    val totalExp = stats.totalExpenses.coerceAtLeast(1.0)
                    stats.categoryExpenses.forEachIndexed { index, exp ->
                        val weight = (exp.amount / totalExp).toFloat()
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(weight.coerceAtLeast(0.01f))
                                .background(getCategoryColor(index))
                        )
                    }
                }

                // List detailed breakdown
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stats.categoryExpenses.forEachIndexed { index, exp ->
                        val percent = if (stats.totalExpenses > 0) {
                            (exp.amount / stats.totalExpenses) * 100
                        } else 0.0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(getCategoryColor(index).copy(alpha = 0.13f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = exp.categoryName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = getCategoryColor(index),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = formatTaka(exp.amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${percent.toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses recorded this month.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}


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
