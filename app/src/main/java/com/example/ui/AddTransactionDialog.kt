package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import com.example.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    viewModel: FinanceViewModel
) {
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val context = LocalContext.current
    val categories by viewModel.allCategories.collectAsState()
    val vaults by viewModel.allSavingsVault.collectAsState()

    // Dialog state
    var txType by remember { mutableStateOf("EXPENSE") } // "INCOME", "EXPENSE", "SAVINGS_TRANSFER"
    var amountStr by remember { mutableStateOf("") }
    var noteStr by remember { mutableStateOf("") }
    
    // Date holding state (defaults to matching today's day/time, aligned to the active month/year state of the app)
    val appActiveMonth = viewModel.selectedCalendar.collectAsState().value
    var selectedDateMs by remember { 
        mutableStateOf(
            Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.YEAR, appActiveMonth.get(Calendar.YEAR))
                set(Calendar.MONTH, appActiveMonth.get(Calendar.MONTH))
            }.timeInMillis
        ) 
    }
    val dateFormatter = remember { SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()) }

    // Categories filtered by active type
    val filteredCategories = remember(categories, txType) {
        if (txType == "INCOME") {
            categories.filter { it.type == "INCOME" && it.name != "Savings" }
        } else {
            categories.filter { it.type == "EXPENSE" && it.name != "Savings" }
        }
    }

    var selectedCategoryName by remember { mutableStateOf("") }
    var selectedVaultAsset by remember { mutableStateOf(vaults.firstOrNull()?.assetType ?: "") } // For Savings Transfer/Withdrawal
    
    // Auto-update if vaults change or reset when type changes
    LaunchedEffect(txType, filteredCategories, vaults) {
        if (txType == "SAVINGS_TRANSFER") {
            selectedCategoryName = "Savings"
            if (vaults.isNotEmpty() && vaults.none { it.assetType == selectedVaultAsset }) {
                selectedVaultAsset = vaults.first().assetType
            } else if (vaults.isEmpty()) {
                selectedVaultAsset = ""
            }
        } else {
            selectedCategoryName = filteredCategories.firstOrNull()?.name ?: "Others"
        }
    }

    // Dropdown expanded states
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var vaultDropdownExpanded by remember { mutableStateOf(false) }
    
    var receiptImageUri by remember { mutableStateOf<String?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            receiptImageUri = uri.toString()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight()
                .testTag("add_transaction_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Transaction",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Type Toggle Segmented Buttons (Income / Expense / Savings Transfer)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        "INCOME" to "Income",
                        "EXPENSE" to "Expense",
                        "SAVINGS_TRANSFER" to "Savings"
                    ).forEach { (typeVal, label) ->
                        val isSelected = txType == typeVal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { txType = typeVal }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Amount Text Field (Dynamic Prefix/Suffix styled elegantly)
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) amountStr = input
                    },
                    label = { Text("Amount ($currencySymbol)") },
                    leadingIcon = {
                        Text(
                            text = currencySymbol,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Date Picker Field
                val dateCalendar = Calendar.getInstance().apply { timeInMillis = selectedDateMs }
                val datePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val chosen = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            }
                            selectedDateMs = chosen.timeInMillis
                        },
                        dateCalendar.get(Calendar.YEAR),
                        dateCalendar.get(Calendar.MONTH),
                        dateCalendar.get(Calendar.DAY_OF_MONTH)
                    )
                }

                OutlinedTextField(
                    value = dateFormatter.format(Date(selectedDateMs)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Choose Date")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                        .testTag("date_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category Selection (Or Vault destination selector if Savings Transfer)
                if (txType == "SAVINGS_TRANSFER") {
                    // Savings Transfer -> Vault Dropdown Selection
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedVaultAsset,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Transfer To Vault") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Expand vault list",
                                    modifier = Modifier.clickable { vaultDropdownExpanded = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vaultDropdownExpanded = true }
                                .testTag("vault_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = vaultDropdownExpanded,
                            onDismissRequest = { vaultDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            if (vaults.isNotEmpty()) {
                                vaults.forEach { vault ->
                                    DropdownMenuItem(
                                        text = { Text(vault.assetType) },
                                        onClick = {
                                            selectedVaultAsset = vault.assetType
                                            vaultDropdownExpanded = false
                                        }
                                    )
                                }
                            } else {
                                DropdownMenuItem(
                                    text = { Text("No Vaults Available") },
                                    onClick = { }
                                )
                            }
                        }
                    }
                } else {
                    // Income or Expense Categories dropdown list
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Expand categories list",
                                    modifier = Modifier.clickable { categoryDropdownExpanded = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryDropdownExpanded = true }
                                .testTag("category_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            // Standard Category items
                            filteredCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryName = category.name
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                            
                            // For Income Type, optionally support Savings Withdrawal category name
                            if (txType == "INCOME") {
                                DropdownMenuItem(
                                    text = { Text("Savings (Withdrawal)") },
                                    onClick = {
                                        selectedCategoryName = "Savings"
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // If user selected "INCOME" and the category name is "Savings", we prompt them to select which vault they are withdrawing from.
                if (txType == "INCOME" && selectedCategoryName == "Savings") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedVaultAsset,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Withdraw Source Vault") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Expand source vault",
                                    modifier = Modifier.clickable { vaultDropdownExpanded = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vaultDropdownExpanded = true }
                                .testTag("source_vault_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = vaultDropdownExpanded,
                            onDismissRequest = { vaultDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            if (vaults.isNotEmpty()) {
                                vaults.forEach { vault ->
                                    DropdownMenuItem(
                                        text = { Text(vault.assetType) },
                                        onClick = {
                                            selectedVaultAsset = vault.assetType
                                            vaultDropdownExpanded = false
                                        }
                                    )
                                }
                            } else {
                                DropdownMenuItem(
                                    text = { Text("No Vaults Available") },
                                    onClick = { }
                                )
                            }
                        }
                    }
                }

                // Note/Source Text Field
                OutlinedTextField(
                    value = noteStr,
                    onValueChange = { noteStr = it },
                    label = { 
                        Text(
                            if (txType == "SAVINGS_TRANSFER") "Optional transfer notes" 
                            else "Note/Source (e.g., Salary, Groceries, Gift)"
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { 
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(if (receiptImageUri == null) "Attach Receipt" else "Change Receipt")
                    }
                    if (receiptImageUri != null) {
                        Spacer(Modifier.width(8.dp))
                        AsyncImage(
                            model = receiptImageUri,
                            contentDescription = "Receipt thumbnail",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(onClick = { receiptImageUri = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove receipt")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Actions buttons
                Button(
                    onClick = {
                        val amountVal = amountStr.toDoubleOrNull() ?: 0.0
                        if (amountVal > 0.0) {
                            // Savings Transfer logic represents Expense under Category "Savings"
                            val finalType = if (txType == "SAVINGS_TRANSFER") "EXPENSE" else txType
                            val finalCategory = if (txType == "SAVINGS_TRANSFER") "Savings" else selectedCategoryName
                            
                            // Note holds vault tracking info + optional user notes
                            val finalNote = when {
                                txType == "SAVINGS_TRANSFER" -> {
                                    "To $selectedVaultAsset Vault: $noteStr".trimEnd(' ', ':')
                                }
                                txType == "INCOME" && selectedCategoryName == "Savings" -> {
                                    "From $selectedVaultAsset Vault: $noteStr".trimEnd(' ', ':')
                                }
                                else -> noteStr
                            }

                            viewModel.addTransaction(
                                type = finalType,
                                categoryName = finalCategory,
                                amount = amountVal,
                                date = selectedDateMs,
                                note = finalNote,
                                receiptImageUri = receiptImageUri
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_transaction_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save Transaction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cancel_transaction_button")
                ) {
                    Text("Cancel", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
