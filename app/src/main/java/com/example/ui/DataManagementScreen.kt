package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.viewmodel.FinanceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onNavigateToSummary: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val currentCalendar by viewModel.selectedCalendar.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val selectedYear = currentCalendar.get(Calendar.YEAR)
    
    val summaryRows = remember(selectedYear, allTransactions) {
        viewModel.getYearlySummary(selectedYear, allTransactions)
    }
    
    val monthlyTransactions = remember(selectedYear, allTransactions) {
        allTransactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.YEAR) == selectedYear
        }.sortedBy { it.date }
    }

    var showArchiveDialog by remember { mutableStateOf(false) }
    val archiveYears = remember {
        context.filesDir
            .listFiles()
            ?.filter { it.name.startsWith("archive_") && it.name.endsWith(".json") }
            ?.mapNotNull { it.name.removePrefix("archive_").removeSuffix(".json").toIntOrNull() }
            ?.sortedDescending()
            ?: emptyList()
    }
    
    var exportScopeDialog by remember { mutableStateOf<String?>(null) } // null, "YEARLY", "MONTHLY"
    var tier1DialogExpanded by remember { mutableStateOf(false) }
    var showNewYearDialog by remember { mutableStateOf(false) }

    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(selectedUri)
                    val lines = inputStream?.bufferedReader()?.readLines() ?: emptyList()
                    inputStream?.close()
                    var imported = 0
                    // Expected CSV header: Date,Type,Category,Amount,Note
                    lines.drop(1).forEach { line ->
                        val parts = line.split(",").map { it.trim() }
                        if (parts.size >= 4) {
                            try {
                                val type = parts[1].uppercase()
                                if (type != "INCOME" && type != "EXPENSE") return@forEach
                                val category = parts[2]
                                val amount = parts[3].toDoubleOrNull() ?: return@forEach
                                val note = if (parts.size > 4) parts[4] else ""
                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                val date = try { sdf.parse(parts[0])?.time ?: System.currentTimeMillis() }
                                           catch (e: Exception) { System.currentTimeMillis() }
                                viewModel.addTransaction(type, category, amount, date, note)
                                imported++
                            } catch (e: Exception) { /* skip malformed rows */ }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(
                            context,
                            if (imported > 0) "Imported $imported transactions." else "No valid rows found.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "Import failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    
    val csvLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { destUri ->
            coroutineScope.launch(Dispatchers.IO) {
                if (exportScopeDialog == "YEARLY") exportDataToUri(context, destUri, summaryRows.monthlyData, selectedYear, "CSV")
                else exportMonthlyDataToUri(context, destUri, monthlyTransactions, selectedYear, "CSV")
                exportScopeDialog = null
                withContext(Dispatchers.Main) { android.widget.Toast.makeText(context, "File saved successfully to storage!", android.widget.Toast.LENGTH_SHORT).show() }
            }
        }
    }

    val pdfLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { destUri ->
            coroutineScope.launch(Dispatchers.IO) {
                if (exportScopeDialog == "YEARLY") exportDataToUri(context, destUri, summaryRows.monthlyData, selectedYear, "PDF")
                else exportMonthlyDataToUri(context, destUri, monthlyTransactions, selectedYear, "PDF")
                exportScopeDialog = null
                withContext(Dispatchers.Main) { android.widget.Toast.makeText(context, "File saved successfully to storage!", android.widget.Toast.LENGTH_SHORT).show() }
            }
        }
    }

    val jsonLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { destUri ->
            coroutineScope.launch(Dispatchers.IO) {
                if (exportScopeDialog == "YEARLY") exportDataToUri(context, destUri, summaryRows.monthlyData, selectedYear, "JSON")
                else exportMonthlyDataToUri(context, destUri, monthlyTransactions, selectedYear, "JSON")
                exportScopeDialog = null
                withContext(Dispatchers.Main) { android.widget.Toast.makeText(context, "File saved successfully to storage!", android.widget.Toast.LENGTH_SHORT).show() }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { tier1DialogExpanded = true }, 
                modifier = Modifier.fillMaxWidth(), 
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer, 
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Export Data")
            }
            Button(
                onClick = { importLauncher.launch("application/json") }, 
                modifier = Modifier.fillMaxWidth(), 
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer, 
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Import Backup (JSON)")
            }
            Button(
                onClick = { showNewYearDialog = true }, 
                modifier = Modifier.fillMaxWidth(), 
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Close & Start New Year")
            }
            
            Button(
                onClick = onNavigateToSummary, 
                modifier = Modifier.fillMaxWidth(), 
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer, 
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Summary")
            }
            
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showArchiveDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Past Year Archives", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(
                                if (archiveYears.isEmpty()) "No archives yet" else "${archiveYears.size} year(s) archived",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }
        }
    }

    if (showNewYearDialog) {
        AlertDialog(
            onDismissRequest = { showNewYearDialog = false },
            title = { Text("Start New Year") },
            text = { Text("This will archive current year's matrix to local JSON history, clear active logs, and carry over final cash balance (${formatAmt(summaryRows.totalRow.cash)}) to Jan 1st of the new year as 'Last Month Carryover'. Your custom categories and savings vault totals will remain intact. Proceed?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.startNewYear(summaryRows.totalRow.cash, context)
                    showNewYearDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewYearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (tier1DialogExpanded) {
        AlertDialog(
            onDismissRequest = { tier1DialogExpanded = false },
            title = { Text("Select Scope of Export Data") },
            text = { 
                Column {
                    Button(onClick = { exportScopeDialog = "YEARLY"; tier1DialogExpanded = false }, modifier = Modifier.fillMaxWidth()) { Text("Yearly Summary Data") }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { exportScopeDialog = "MONTHLY"; tier1DialogExpanded = false }, modifier = Modifier.fillMaxWidth()) { Text("Monthly Detailed Breakdown") }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { tier1DialogExpanded = false }) { Text("Cancel") } }
        )
    }

    if (exportScopeDialog != null && !tier1DialogExpanded) {
        AlertDialog(
            onDismissRequest = { exportScopeDialog = null },
            title = { Text("Select File Destination Format") },
            text = { 
                Column {
                    Button(onClick = { csvLauncher.launch("finance_export.csv") }, modifier = Modifier.fillMaxWidth()) { Text("CSV Spreadsheet .csv") }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { pdfLauncher.launch("finance_export.pdf") }, modifier = Modifier.fillMaxWidth()) { Text("Print-Ready Document .pdf") }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { jsonLauncher.launch("finance_export.json") }, modifier = Modifier.fillMaxWidth()) { Text("JSON Payload .json") }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { exportScopeDialog = null }) { Text("Cancel") } }
        )
    }

    if (showArchiveDialog) {
        Dialog(
            onDismissRequest = { showArchiveDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(max = 580.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Past Year Archives", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (archiveYears.isEmpty()) {
                        Text(
                            "No archived years found.\nUse 'Start New Year' to archive the current year before resetting.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    } else {
                        var selectedYear by remember { mutableStateOf(archiveYears.first()) }
                        var archiveContent by remember {
                            mutableStateOf(
                                java.io.File(context.filesDir, "archive_${archiveYears.first()}.json")
                                    .takeIf { it.exists() }?.readText() ?: ""
                            )
                        }
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(archiveYears.size) { i ->
                                val yr = archiveYears[i]
                                FilterChip(
                                    selected = selectedYear == yr,
                                    onClick = {
                                        selectedYear = yr
                                        val f = java.io.File(context.filesDir, "archive_$yr.json")
                                        archiveContent = if (f.exists()) f.readText() else "No data found."
                                    },
                                    label = { Text("$yr") }
                                )
                            }
                        }
                        if (archiveContent.isNotBlank()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = archiveContent,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                    Button(onClick = { showArchiveDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
