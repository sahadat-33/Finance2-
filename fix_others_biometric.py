import re

file_path = "app/src/main/java/com/example/ui/OthersScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Imports
if "androidx.biometric.BiometricManager" not in content:
    content = content.replace(
        "import androidx.compose.ui.text.input.KeyboardType",
        "import androidx.compose.ui.text.input.KeyboardType\nimport androidx.biometric.BiometricManager"
    )

# States
content = content.replace(
    "val isPinEnabled by viewModel.isPinEnabled.collectAsState(initial = false)",
    '''val isPinEnabled by viewModel.isPinEnabled.collectAsState(initial = false)
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState(initial = false)
    val canAuthenticateBiometric = remember {
        BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }'''
)

# UI
content = content.replace(
    '''                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))''',
    '''                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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

                    if (isPinEnabled && canAuthenticateBiometric) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Also unlock with fingerprint / face", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { checked ->
                                    viewModel.setBiometricEnabled(checked)
                                }
                            )
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))'''
)

with open(file_path, "w") as f:
    f.write(content)
