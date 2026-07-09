package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingBalanceScreen(
    viewModel: FinanceViewModel,
    onComplete: () -> Unit
) {
    var balanceInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to Finance Tracker",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Let's set your starting wallet balance.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                val currencyOptions = listOf("BDT (৳)" to "৳", "USD ($)" to "$", "EUR (€)" to "€")
                var selectedCurrency by remember { mutableStateOf("৳") }

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    currencyOptions.forEachIndexed { index, (label, symbol) ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = currencyOptions.size),
                            onClick = { selectedCurrency = symbol },
                            selected = selectedCurrency == symbol
                        ) {
                            Text(label)
                        }
                    }
                }
                
                OutlinedTextField(
                    value = balanceInput,
                    onValueChange = { balanceInput = it },
                    label = { Text("Initial Balance") },
                    leadingIcon = { Text(selectedCurrency, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Button(
                    onClick = {
                        viewModel.setCurrencySymbol(selectedCurrency)
                        val amount = balanceInput.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            scope.launch {
                                // Day 1 of the current active month and year
                                val cal = Calendar.getInstance()
                                cal.set(Calendar.DAY_OF_MONTH, 1)
                                cal.set(Calendar.HOUR_OF_DAY, 8)
                                cal.set(Calendar.MINUTE, 0)
                                cal.set(Calendar.SECOND, 0)
                                cal.set(Calendar.MILLISECOND, 0)
                                
                                viewModel.addTransaction(
                                    type = "INCOME",
                                    amount = amount,
                                    categoryName = "Others",
                                    note = "Initial Wallet Balance",
                                    date = cal.timeInMillis,
                                    receiptImageUri = null
                                )
                                viewModel.completeOnboarding()
                                onComplete()
                            }
                        } else {
                            viewModel.completeOnboarding()
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Continue to Dashboard", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
