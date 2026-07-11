package com.example.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.viewmodel.FinanceViewModel
import com.example.viewmodel.MonthlyStats
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.triggerFetchFromCloud()
    }

    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val selectedCalendar by viewModel.selectedCalendar.collectAsState()
    val stats by viewModel.monthlyStatsFlow.collectAsState()
    val savingsVault by viewModel.dynamicVaultBalances.collectAsState()

    val monthNameFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val formattedMonth = monthNameFormatter.format(selectedCalendar.time)

    // Format utility for amounts
    fun formatTaka(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        return "$currencySymbol${formatter.format(amount)}"
    }

    var isVaultExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Safe gap at top for edge-to-edge content
        Spacer(modifier = Modifier.height(16.dp))

        // No month selector row here since it is synchronized globally in the top app bar

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
                Text(
                    text = "Income Spent Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

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
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 12.dp.toPx()
                            // Leftover / Remaining backing circle (Lite Green)
                            drawCircle(
                                color = Color(0xFFBBECC4),
                                style = Stroke(stroke)
                            )
                            val sweepAngle = (percentSpentVal * 360f).toFloat().coerceIn(0f, 360f)
                            // Spent foreground arc (Lite Blue)
                            drawArc(
                                color = Color(0xFFA2C2FC),
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
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
                            Text("Leftover: ${formatTaka((stats.totalEarnings - stats.totalExpenses).coerceAtLeast(0.0))}", style = MaterialTheme.typography.bodyMedium)
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(getCategoryColor(index))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = exp.categoryName,
                                        style = MaterialTheme.typography.bodyMedium,
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
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${percent.toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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

        // Summary Statistics (Left/Right Layout)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Side (2 Columns Stacked)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactSummaryCard(
                    title = "Total Earnings",
                    amount = formatTaka(stats.totalEarnings),
                    color = EarningGreen,
                    modifier = Modifier.weight(1f)
                )
                CompactSummaryCard(
                    title = "Total Expenses",
                    amount = formatTaka(stats.totalExpenses),
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }

            // Right Side (1 High-Priority Column - Cash Balance)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = CardStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Cash Balance",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTaka(stats.cashBalance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Two-Column Breakdowns (Left Column Earnings, Right Column Expenses)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Column (Earning breakdown)
            Card(
                modifier = Modifier.weight(1f).heightIn(min = 150.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Earnings Src",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EarningGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (stats.categoryEarnings.isNotEmpty()) {
                        stats.categoryEarnings.forEach { earn ->
                            Column {
                                Text(
                                    text = earn.categoryName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatTaka(earn.amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EarningGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    } else {
                        Text(
                            text = "No income logged",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Right Column (Expense category breakdowns)
            Card(
                modifier = Modifier.weight(1f).heightIn(min = 150.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Expense Cat",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (stats.categoryExpenses.isNotEmpty()) {
                        stats.categoryExpenses.take(5).forEach { exp ->
                            Column {
                                Text(
                                    text = exp.categoryName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatTaka(exp.amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    } else {
                        Text(
                            text = "No expenses logged",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }

        // Bottom Section (Savings Vault) Expandable Card
        val totalSavingsAmount = savingsVault.sumOf { it.amount }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isVaultExpanded = !isVaultExpanded }
                .testTag("savings_vault_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = "Savings Vault Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Savings Vault",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Offline Emergency Fund Ledger",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatTaka(totalSavingsAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isVaultExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand/Collapse details"
                        )
                    }
                }

                // If expanded, show the beautiful breakdown
                if (isVaultExpanded) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))

                    // Draw a gorgeous multi-segmented bar representing vault contributions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        val sum = totalSavingsAmount.coerceAtLeast(1.0)
                        savingsVault.forEachIndexed { index, asset ->
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight((asset.amount / sum).toFloat().coerceAtLeast(0.01f))
                                    .background(getVaultColor(index))
                            )
                        }
                    }

                    // Vault items lists
                    if (savingsVault.isNotEmpty()) {
                        savingsVault.forEachIndexed { index, asset ->
                            val assetLabel = asset.assetType
                            val assetIcon = Icons.Default.AccountBalance
    
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(getVaultColor(index).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = assetIcon,
                                            contentDescription = assetLabel,
                                            tint = getVaultColor(index),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = assetLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    text = formatTaka(asset.amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No Vaults Found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Space at the bottom for safety
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: String,
    subLabel: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    
    // Resolve clean minimal utility colors based on the card title
    val (containerColor, contentColor, highlightColor) = remember(title, isDark) {
        if (isDark) {
            when {
                title.contains("Earning", ignoreCase = true) -> Triple(Color(0xFF22362E), Color(0xFFBBECC4), Color(0xFFBBECC4))
                title.contains("Expense", ignoreCase = true) -> Triple(Color(0xFF3B2323), Color(0xFFFFC6C6), Color(0xFFFFC6C6))
                title.contains("Saving", ignoreCase = true) -> Triple(Color(0xFF1B2A40), Color(0xFF9BF2EC), Color(0xFF9BF2EC))
                else -> Triple(Color(0xFF202936), Color(0xFFD3E0FA), Color(0xFFA2C2FC))
            }
        } else {
            when {
                title.contains("Earning", ignoreCase = true) -> Triple(EarningCardBg, EarningCardText, EarningCardText)
                title.contains("Expense", ignoreCase = true) -> Triple(ExpenseCardBg, ExpenseCardText, ExpenseCardText)
                title.contains("Saving", ignoreCase = true) -> Triple(SavingsCardBg, SavingsCardText, SavingsCardText)
                else -> Triple(BalanceCardBg, BalanceCardText, BalanceCardHighlight)
            }
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isDark) null else CardStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = highlightColor
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun CompactSummaryCard(
    title: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val (containerColor, contentColor, highlightColor) = remember(title, isDark) {
        if (isDark) {
            when {
                title.contains("Earning", ignoreCase = true) -> Triple(Color(0xFF22362E), Color(0xFFBBECC4), Color(0xFFBBECC4))
                title.contains("Expense", ignoreCase = true) -> Triple(Color(0xFF3B2323), Color(0xFFFFC6C6), Color(0xFFFFC6C6))
                else -> Triple(Color(0xFF202936), Color(0xFFD3E0FA), Color(0xFFA2C2FC))
            }
        } else {
            when {
                title.contains("Earning", ignoreCase = true) -> Triple(EarningCardBg, EarningCardText, EarningCardText)
                title.contains("Expense", ignoreCase = true) -> Triple(ExpenseCardBg, ExpenseCardText, ExpenseCardText)
                else -> Triple(BalanceCardBg, BalanceCardText, BalanceCardHighlight)
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isDark) null else CardStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = highlightColor
            )
        }
    }
}

// Spark colors for expense category lists using lite pastel scheme
fun getCategoryColor(index: Int): Color {
    val colors = listOf(
        Color(0xFFA2C2FC), // Lite Blue
        Color(0xFF9BF2EC), // Lite Cyan
        Color(0xFFBBECC4), // Lite Green
        Color(0xFFFFC6C6), // Lite Red
        Color(0xFFFFDAB9), // Lite Orange
        Color(0xFFFFF2AF), // Lite Yellow
        Color(0xFFE1D5EC)  // Lite Lavender
    )
    return colors[index % colors.size]
}

// Spark colors for Vault Items segment bar using lite pastel scheme
fun getVaultColor(index: Int): Color {
    val colors = listOf(
        Color(0xFFA2C2FC), // Lite Blue
        Color(0xFF9BF2EC), // Lite Cyan
        Color(0xFFBBECC4), // Lite Green
        Color(0xFFFFDAB9)  // Lite Orange
    )
    return colors[index % colors.size]
}

@Composable
fun CardStroke(width: androidx.compose.ui.unit.Dp, color: Color) = 
    androidx.compose.foundation.BorderStroke(width, color)
