package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.Transaction
import com.example.ui.theme.EarningGreen
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.SavingsWithdrawalPink
import com.example.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TimelineScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.triggerFetchFromCloud()
    }

    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val stats by viewModel.monthlyStatsFlow.collectAsState()
    val transactions = stats.transactions // Flow filtered by active month

    // Format utility
    fun formatTaka(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        return "$currencySymbol${formatter.format(amount)}"
    }

    // Search & Filter State
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("All") } // "All", "Income", "Expense", "Savings Withdrawals"
    var expandedFilter by remember { mutableStateOf(false) }
    var specificDate by remember { mutableStateOf("") }

    val filteredTransactions = remember(transactions, searchQuery, filterType, specificDate) {
        transactions.filter { tx ->
            val searchLower = searchQuery.lowercase()
            val matchesSearch = tx.note.lowercase().contains(searchLower) || 
                                tx.categoryName.lowercase().contains(searchLower) ||
                                tx.amount.toString().contains(searchLower)
            
            val matchesFilter = when (filterType) {
                "Income" -> tx.type == "INCOME" && tx.categoryName != "Savings"
                "Expense" -> tx.type == "EXPENSE" && tx.categoryName != "Savings"
                "Savings Withdrawals" -> tx.type == "INCOME" && tx.categoryName == "Savings"
                else -> true
            }

            val matchesDate = if (specificDate.isNotBlank()) {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.format(Date(tx.date)) == specificDate
            } else true

            matchesSearch && matchesFilter && matchesDate
        }
    }

    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.sortedByDescending { it.date }.groupBy { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            val sdf = SimpleDateFormat("MMMM d, yyyy - EEEE", Locale.getDefault())
            sdf.format(cal.time)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("timeline_screen")
    ) {
        // Timeline Header Summary Info Box
        val isDark = MaterialTheme.colorScheme.background.red < 0.5f
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            border = if (isDark) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${filteredTransactions.size} entries this month",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "Balance: ${formatTaka(stats.cashBalance)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (stats.cashBalance >= 0) EarningGreen else ExpenseRed
                )
            }
        }

        // Search & Filter Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search note or category", style = MaterialTheme.typography.bodySmall) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                trailingIcon = {
                    IconButton(onClick = { expandedFilter = !expandedFilter }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Advanced Filters")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            )

            if (expandedFilter) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Filters", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var expandedType by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = { expandedType = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text(filterType, style = MaterialTheme.typography.labelSmall)
                                }
                                DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                                    listOf("All", "Income", "Expense", "Savings Withdrawals").forEach { ft ->
                                        DropdownMenuItem(text = { Text(ft) }, onClick = { filterType = ft; expandedType = false })
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = specificDate,
                                onValueChange = { specificDate = it },
                                placeholder = { Text("DD/MM/YYYY") },
                                textStyle = MaterialTheme.typography.labelSmall,
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.TextButton(
                            onClick = { 
                                filterType = "All"
                                searchQuery = ""
                                specificDate = ""
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Clear Filters", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        if (groupedTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No log records found.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "Use the quick-add FAB below to log.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Safety offset for FAB & Navigation bars
            ) {
                groupedTransactions.forEach { (dateHeader, txs) ->
                    item {
                        // Date grouping banner label
                        Text(
                            text = dateHeader,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // Table Columns Headers
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(vertical = 6.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Type/Cat.",
                                modifier = Modifier.weight(1.2f),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Note",
                                modifier = Modifier.weight(1.5f),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Amount",
                                modifier = Modifier.weight(1.1f),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End
                            )
                            Spacer(modifier = Modifier.width(44.dp)) // Offset to align with delete button row column
                        }
                    }

                    items(txs) { tx ->
                        TimelineRowItem(
                            transaction = tx,
                            formatTaka = ::formatTaka,
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineRowItem(
    transaction: Transaction,
    formatTaka: (Double) -> String,
    onDelete: () -> Unit
) {
    // Determine Savings Withdrawal (Income categorization of Savings)
    val isSavingsWithdrawal = transaction.type == "INCOME" && transaction.categoryName == "Savings"
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    
    // Highlight background for dynamic status feedback
    val rowBg = when {
        isSavingsWithdrawal -> if (isDark) Color(0xFF3B2022) else Color(0xFFFFF1F2)
        transaction.type == "INCOME" -> if (isDark) Color(0xFF1C2E21) else Color(0xFFEBFBEB)
        else -> MaterialTheme.colorScheme.surface
    }

    val chipColor = when {
        isSavingsWithdrawal -> SavingsWithdrawalPink
        transaction.type == "INCOME" -> EarningGreen
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timeline_row_${transaction.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = rowBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (isDark) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type & Category
            Row(
                modifier = Modifier.weight(1.2f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(chipColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isSavingsWithdrawal -> Icons.Default.RemoveCircleOutline
                            transaction.type == "INCOME" -> Icons.Default.ArrowUpward
                            else -> Icons.Default.ArrowDownward
                        },
                        contentDescription = transaction.type,
                        tint = chipColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    val displayType = when {
                        isSavingsWithdrawal -> "Withdrawal"
                        transaction.type == "INCOME" -> "Income"
                        else -> "Expense"
                    }
                    Text(
                        text = displayType,
                        style = MaterialTheme.typography.labelSmall,
                        color = chipColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = transaction.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Note/Source and Receipt
            Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                var previewReceipt by remember { mutableStateOf<String?>(null) }

                if (transaction.receiptImageUri != null) {
                    AsyncImage(
                        model = transaction.receiptImageUri,
                        contentDescription = "Receipt Thumbnail",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { previewReceipt = transaction.receiptImageUri },
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                
                if (previewReceipt != null) {
                    Dialog(onDismissRequest = { previewReceipt = null }) {
                        Box(modifier = Modifier.fillMaxWidth().height(500.dp).background(Color.Black).clickable { previewReceipt = null }, contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = previewReceipt,
                                contentDescription = "Receipt Image",
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentScale = ContentScale.Fit
                            )
                            IconButton(
                                onClick = { previewReceipt = null },
                                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                }

                Text(
                    text = transaction.note.ifEmpty { "-" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontStyle = if (transaction.note.isEmpty()) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                )
            }

            // Amount
            Text(
                text = formatTaka(transaction.amount),
                modifier = Modifier.weight(1.1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSavingsWithdrawal) SavingsWithdrawalPink else if (transaction.type == "INCOME") EarningGreen else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End
            )

            // Quick Delete icon
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .padding(start = 8.dp)
                    .testTag("delete_tx_btn_${transaction.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete transaction records",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
