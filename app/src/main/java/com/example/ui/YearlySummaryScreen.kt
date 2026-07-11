package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.viewmodel.FinanceViewModel
import com.example.viewmodel.YearlySummaryRow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearlySummaryScreen(viewModel: FinanceViewModel) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.triggerFetchFromCloud()
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val currentCalendar by viewModel.selectedCalendar.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    
    val selectedYear = currentCalendar.get(Calendar.YEAR)
    
    // Compute yearly stats for selected year
    val summaryRows = remember(selectedYear, allTransactions) {
        viewModel.getYearlySummary(selectedYear)
    }
    
    val monthlyTransactions = remember(selectedYear, allTransactions) {
        allTransactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.YEAR) == selectedYear
        }.sortedBy { it.date }
    }

    var viewState by remember { mutableStateOf("YEARLY") }
    var selectedDetailMonth by remember { mutableStateOf(currentCalendar.get(Calendar.MONTH)) }

    val monthlyDetailTransactions = remember(selectedYear, selectedDetailMonth, allTransactions) {
        allTransactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedDetailMonth
        }.sortedBy { it.date }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Navigation Controls: 2-option Segmented Control / Toggle Tab
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                .padding(4.dp)
                .fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.Center
        ) {
            val options = listOf("Yearly", "Monthly")
            options.forEach { option ->
                val isSelected = option.uppercase() == viewState
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                            RoundedCornerShape(50)
                        )
                        .clickable { viewState = option.uppercase() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (viewState == "YEARLY") "Total Yearly Management - $selectedYear" else "Monthly Detail - $selectedYear",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Matrix Grid
        if (viewState == "YEARLY") {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TableCell("Month", weight = 1.2f, isHeader = true)
                        TableCell("Recv", weight = 1f, isHeader = true)
                        TableCell("Exp", weight = 1f, isHeader = true)
                        TableCell("Sav", weight = 1f, isHeader = true)
                        TableCell("Cash", weight = 1f, isHeader = true)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    LazyColumn {
                        items(summaryRows.monthlyData) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TableCell(row.month, weight = 1.2f)
                                TableCell(formatAmt(row.received), weight = 1f)
                                TableCell(formatAmt(row.expenses), weight = 1f)
                                TableCell(formatAmt(row.savings), weight = 1f)
                                TableCell(formatAmt(row.cash), weight = 1f, formatColor = row.cash >= 0)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SummaryRow("Total", summaryRows.totalRow)
                            SummaryRow("Average", summaryRows.averageRow)
                            SummaryRow("Max", summaryRows.maxRow)
                            SummaryRow("Min", summaryRows.minRow)
                        }
                    }
                }
            }
        } else {
            val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(monthNames.size) { index ->
                    val isSelected = selectedDetailMonth == index
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedDetailMonth = index }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = monthNames[index],
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TableCell("Date", weight = 1f, isHeader = true)
                        TableCell("Income", weight = 1.2f, isHeader = true)
                        TableCell("Amt", weight = 0.8f, isHeader = true)
                        TableCell("Expense", weight = 1.2f, isHeader = true)
                        TableCell("Amt", weight = 0.8f, isHeader = true)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    LazyColumn {
                        items(monthlyDetailTransactions) { tx ->
                            val isIncome = tx.type == "INCOME"
                            val dateStr = java.text.SimpleDateFormat("d-MMM", java.util.Locale.getDefault()).format(java.util.Date(tx.date))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell(dateStr, weight = 1f)
                                if (isIncome) {
                                    TableCell(tx.categoryName, weight = 1.2f)
                                    TableCell(formatAmt(tx.amount), weight = 0.8f)
                                    TableCell("-", weight = 1.2f)
                                    TableCell("-", weight = 0.8f)
                                } else {
                                    TableCell("-", weight = 1.2f)
                                    TableCell("-", weight = 0.8f)
                                    TableCell(tx.categoryName, weight = 1.2f)
                                    TableCell(formatAmt(tx.amount), weight = 0.8f)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }


    }

}

@Composable
fun SummaryRow(label: String, data: YearlySummaryRow) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TableCell(label, weight = 1.2f, isHeader = true)
        TableCell(formatAmt(data.received), weight = 1f, isHeader = true)
        TableCell(formatAmt(data.expenses), weight = 1f, isHeader = true)
        TableCell(formatAmt(data.savings), weight = 1f, isHeader = true)
        TableCell(formatAmt(data.cash), weight = 1f, isHeader = true, formatColor = data.cash >= 0)
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    formatColor: Boolean? = null
) {
    val color = if (formatColor == true) MaterialTheme.colorScheme.primary else if (formatColor == false) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        color = color
    )
}

fun formatAmt(amt: Double): String {
    val absAmt = Math.abs(amt)
    return if (absAmt >= 1000) {
        String.format("%.1fk", amt / 1000)
    } else {
        String.format("%.0f", amt)
    }
}

fun exportDataToUri(context: Context, uri: android.net.Uri, data: List<YearlySummaryRow>, year: Int, format: String) {
    try {
        val outStream = context.contentResolver.openOutputStream(uri) ?: return
        
        if (format == "CSV") {
            val content = buildString {
                append("Month,Received,Expenses,Savings,Cash\n")
                data.forEach { row ->
                    append("${row.month},${row.received},${row.expenses},${row.savings},${row.cash}\n")
                }
            }
            outStream.write(content.toByteArray())
        } else if (format == "JSON") {
            val content = buildString {
                append("[\n")
                data.forEachIndexed { index, row ->
                    append("  {\n")
                    append("    \"month\": \"${row.month}\",\n")
                    append("    \"received\": ${row.received},\n")
                    append("    \"expenses\": ${row.expenses},\n")
                    append("    \"savings\": ${row.savings},\n")
                    append("    \"cash\": ${row.cash}\n")
                    append("  }${if (index < data.size - 1) "," else ""}\n")
                }
                append("]")
            }
            outStream.write(content.toByteArray())
        } else if (format == "PDF") {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(400, 600, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            paint.color = Color.BLACK
            paint.textSize = 12f
            
            var yPos = 40f
            canvas.drawText("Yearly Finance Summary - $year", 20f, yPos, paint)
            yPos += 30f
            
            paint.textSize = 10f
            canvas.drawText("Month      Received      Expenses      Savings      Cash", 20f, yPos, paint)
            yPos += 20f
            
            data.forEach { row ->
                val line = String.format("%-10s %-13.0f %-13.0f %-12.0f %.0f", row.month, row.received, row.expenses, row.savings, row.cash)
                canvas.drawText(line, 20f, yPos, paint)
                yPos += 20f
            }
            
            document.finishPage(page)
            document.writeTo(outStream)
            document.close()
        }
        outStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun exportMonthlyDataToUri(context: Context, uri: android.net.Uri, data: List<com.example.data.Transaction>, year: Int, format: String) {
    try {
        val outStream = context.contentResolver.openOutputStream(uri) ?: return
        
        if (format == "CSV") {
            val content = buildString {
                append("Date,Type,Category,Amount,Note\n")
                data.forEach { tx ->
                    val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(tx.date))
                    append("${dateStr},${tx.type},${tx.categoryName},${tx.amount},${tx.note}\n")
                }
            }
            outStream.write(content.toByteArray())
        } else if (format == "JSON") {
            val content = buildString {
                append("[\n")
                data.forEachIndexed { index, tx ->
                    append("  {\n")
                    append("    \"date\": ${tx.date},\n")
                    append("    \"type\": \"${tx.type}\",\n")
                    append("    \"category\": \"${tx.categoryName}\",\n")
                    append("    \"amount\": ${tx.amount},\n")
                    append("    \"note\": \"${tx.note}\"\n")
                    append("  }${if (index < data.size - 1) "," else ""}\n")
                }
                append("]")
            }
            outStream.write(content.toByteArray())
        } else if (format == "PDF") {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(400, 600, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            paint.color = Color.BLACK
            paint.textSize = 12f
            
            var yPos = 40f
            canvas.drawText("Monthly Finance Detail - $year", 20f, yPos, paint)
            yPos += 30f
            
            paint.textSize = 10f
            canvas.drawText("Date            Type        Category         Amount", 20f, yPos, paint)
            yPos += 20f
            
            data.forEach { tx ->
                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(tx.date))
                val line = String.format("%-15s %-10s %-15s %.0f", dateStr, tx.type, tx.categoryName, tx.amount)
                canvas.drawText(line, 20f, yPos, paint)
                yPos += 20f
                if (yPos > 550f) {
                    // Quick layout limit for simplified PDF
                    canvas.drawText("... continued in CSV/JSON ...", 20f, yPos, paint)
                    return@forEach
                }
            }
            
            document.finishPage(page)
            document.writeTo(outStream)
            document.close()
        }
        outStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
