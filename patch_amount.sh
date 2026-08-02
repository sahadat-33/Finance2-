#!/bin/bash

# Find OutlinedTextField for amountStr in AddTransactionDialog
# We will use sed to inject focus handling and keyboard actions
sed -i 's/val context = LocalContext.current/val context = LocalContext.current\n    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current/g' app/src/main/java/com/example/ui/AddTransactionDialog.kt

# Replace keyboardOptions with keyboardOptions and keyboardActions, and onFocusChanged
# We will do a multiline replacement.
