import re

with open('app/src/main/java/com/example/ui/AnalyticsScreen.kt', 'r') as f:
    content = f.read()

# I will replace the main AnalyticsScreen composable with two composables:
# 1. AnalyticsScreen
# 2. IncomeSpentAnalysisCard

new_content = """package com.example.ui

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

@Composable
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
"""

with open('app/src/main/java/com/example/ui/AnalyticsScreen.kt', 'w') as f:
    f.write(new_content)
