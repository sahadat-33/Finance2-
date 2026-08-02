#!/bin/bash

# Find OutlinedTextField for amountStr in EditTransactionDialog
sed -i 's/val context = LocalContext.current/val context = LocalContext.current\n    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current/g' app/src/main/java/com/example/ui/EditTransactionDialog.kt

# Update the onValueChange to allow + and -
sed -i 's/if (input.all { it.isDigit() || it == '\''.'\'' }) amountStr = input/if (input.all { it.isDigit() || it == '\''.'\'' || it == '\''+'\'' || it == '\''-'\'' }) amountStr = input/' app/src/main/java/com/example/ui/EditTransactionDialog.kt

