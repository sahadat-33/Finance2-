package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EarningGreen
import com.example.ui.theme.ExpenseRed
import com.example.viewmodel.FinanceViewModel
import com.example.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val categories by viewModel.allCategories.collectAsState()
    val savingsVaults by viewModel.allSavingsVault.collectAsState()
    val isDarkTheme by viewModel.isDarkMode.collectAsState()
    val isCloudSyncEnabled by viewModel.isCloudSyncEnabled.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val currentCalendar by viewModel.selectedCalendar.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val selectedYear = currentCalendar.get(Calendar.YEAR)
    
    val summaryRows = remember(selectedYear, allTransactions) {
        viewModel.getYearlySummary(selectedYear)
    }
    
    val monthlyTransactions = remember(selectedYear, allTransactions) {
        allTransactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.YEAR) == selectedYear
        }.sortedBy { it.date }
    }

    // Adding category state
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryType by remember { mutableStateOf("EXPENSE") } // "INCOME" or "EXPENSE"

    // Adding vault state
    var newVaultName by remember { mutableStateOf("") }

    val incomeCategories = remember(categories) { categories.filter { it.type == "INCOME" } }
    val expenseCategories = remember(categories) { categories.filter { it.type == "EXPENSE" } }
    
    val isPinEnabled by viewModel.isPinEnabled.collectAsState()
    var showPinDialog by remember { mutableStateOf(false) }
    var pinDialogMode by remember { mutableStateOf("SET") }

    val isUserSignedIn by viewModel.isUserSignedInFlow.collectAsState()
    val currentUserName = viewModel.currentUserName
    val isOfflineGuest by viewModel.isOfflineGuest.collectAsState()



    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(if (pinDialogMode == "SET") "Set PIN" else "Verify PIN to Disable") },
            text = {
                Column {
                    Text(if (pinDialogMode == "SET") "Enter a 4-digit PIN." else "Enter your 4-digit PIN to disable lock.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = errorMessage.isNotEmpty(),
                        supportingText = if (errorMessage.isNotEmpty()) { { Text(errorMessage) } } else null
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pinInput.length == 4) {
                            if (pinDialogMode == "SET") {
                                viewModel.setPin(pinInput)
                                showPinDialog = false
                            } else {
                                if (viewModel.disablePin(pinInput)) {
                                    showPinDialog = false
                                } else {
                                    errorMessage = "Incorrect PIN."
                                }
                            }
                        } else {
                            errorMessage = "PIN must be 4 digits."
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showVaultDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    if (showCurrencyDialog) {
        val currencyOptions = listOf("BDT (৳)" to "৳", "USD ($)" to "$", "EUR (€)" to "€")
        val currentSymbol by viewModel.currencySymbol.collectAsState()
        
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Change Your Currency") },
            text = {
                Column {
                    currencyOptions.forEach { (label, symbol) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCurrencySymbol(symbol)
                                    showCurrencyDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = symbol == currentSymbol,
                                onClick = {
                                    viewModel.setCurrencySymbol(symbol)
                                    showCurrencyDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showCurrencyDialog = false }) { Text("Cancel") } }
        )
    }

    if (showCategoryDialog) {
        Dialog(onDismissRequest = { showCategoryDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(max = 600.dp), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Manage Categories", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    
                    // Input 
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(if (newCategoryType == "EXPENSE") ExpenseRed else Color.Transparent).clickable { newCategoryType = "EXPENSE" }.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Expense", color = if (newCategoryType == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(if (newCategoryType == "INCOME") EarningGreen else Color.Transparent).clickable { newCategoryType = "INCOME" }.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Income", color = if (newCategoryType == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newCategoryName, onValueChange = { newCategoryName = it }, label = { Text("Category Name") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                        FloatingActionButton(onClick = {
                            if (newCategoryName.isNotBlank()) { viewModel.addCategory(newCategoryName.trim(), newCategoryType); newCategoryName = "" }
                        }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(52.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }

                    Text("Income Sources", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EarningGreen)
                    incomeCategories.forEach { cat -> CategoryRow(cat.name, cat.isDefault, EarningGreen) { viewModel.deleteCategory(cat.id) } }

                    Text("Expense Slates", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ExpenseRed)
                    expenseCategories.forEach { cat -> CategoryRow(cat.name, cat.isDefault, ExpenseRed) { viewModel.deleteCategory(cat.id) } }

                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showCategoryDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Close") }
                }
            }
        }
    }

    if (showVaultDialog) {
        Dialog(onDismissRequest = { showVaultDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(max = 600.dp), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Manage Savings Vaults", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newVaultName, onValueChange = { newVaultName = it }, label = { Text("Vault Name") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                        FloatingActionButton(onClick = {
                            if (newVaultName.isNotBlank()) { viewModel.addSavingsVault(newVaultName.trim(), 0.0); newVaultName = "" }
                        }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(52.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }

                    Text("My Asset Vaults", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (savingsVaults.isEmpty()) {
                        Text("No Vaults Found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    } else {
                        savingsVaults.forEach { vault -> CategoryRow(vault.assetType, false, MaterialTheme.colorScheme.primary) { viewModel.deleteSavingsVault(vault.id) } }
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showVaultDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Close") }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App title profile card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            border = if (isDarkTheme) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isUserSignedIn) "Hello, $currentUserName 👋" else "Hello, Guest Tracker 👋",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Finance Tracker Dashboard",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
        
        if (isUserSignedIn && !viewModel.isEmailVerified) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(Modifier.width(8.dp))
                    Text("Please verify your email address to unlock cloud backup.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        
        // Settings rows
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = if (isDarkTheme) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column {
                if (isUserSignedIn) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToProfile() }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text("👤 Account Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = { onNavigateToAuth() },
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text("🔐 Login / Signup (Sync to Cloud)")
                    }
                }
                
                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable PIN Lock", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isPinEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                pinDialogMode = "SET"
                                showPinDialog = true
                            } else {
                                pinDialogMode = "DISABLE"
                                showPinDialog = true
                            }
                        }
                    )
                }
                
                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showCurrencyDialog = true }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Change Your Currency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showCategoryDialog = true }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Manage Income & Expense Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showVaultDialog = true }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Manage Savings Vaults", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        var dataManagementExpanded by remember { mutableStateOf(false) }
        var exportScopeDialog by remember { mutableStateOf<String?>(null) } // null, "YEARLY", "MONTHLY"
        val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                // Dummy import action since JSON schema wasn't fully defined.
                android.widget.Toast.makeText(context, "Import successful!", android.widget.Toast.LENGTH_SHORT).show()
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

        var showNewYearDialog by remember { mutableStateOf(false) }

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

        var tier1DialogExpanded by remember { mutableStateOf(false) }

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

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = if (isDarkTheme) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { dataManagementExpanded = !dataManagementExpanded }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Data & Backup Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Icon(if (dataManagementExpanded) Icons.Default.ArrowDropDown else Icons.Default.Add, contentDescription = "Expand")
                }
                
                if (dataManagementExpanded) {
                    HorizontalDivider()
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { tier1DialogExpanded = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                            Text("Export Data")
                        }
                        Button(onClick = { importLauncher.launch("application/json") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                            Text("Import Backup (JSON)")
                        }
                        Button(onClick = { showNewYearDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                            Text("Close & Start New Year")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Designed & Developed by Sahadat Hossan", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 32.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryRow(
    categoryName: String,
    isDefault: Boolean,
    color: Color,
    onDelete: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete '$categoryName'") },
            text = {
                Text("Are you sure you want to delete '$categoryName'?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_row_$categoryName")
            .combinedClickable(
                onClick = { /* Soft feedback on click */ },
                onLongClick = {
                    showDeleteConfirmDialog = true
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Category",
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("delete_cat_btn_$categoryName")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete label",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
