import re

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'r') as f:
    content = f.read()

# 1. Insert `var showThemeDialog`
content = content.replace(
    'var showVaultDialog by remember { mutableStateOf(false) }',
    'var showVaultDialog by remember { mutableStateOf(false) }\n    var showThemeDialog by remember { mutableStateOf(false) }\n    val themes = listOf("Mint Fresh", "Midnight Dark", "Ocean Blue", "Sunset Warm", "Lavender Calm", "Rose Soft")'
)

# 2. Insert the Theme Dialog
theme_dialog = """
    if (showThemeDialog) {
        Dialog(onDismissRequest = { showThemeDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(max = 600.dp), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("App Theme", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    
                    themes.forEach { themeName ->
                        val isSelected = appTheme == themeName
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setAppTheme(themeName); showThemeDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(24.dp).clip(CircleShape).background(
                                            when (themeName) {
                                                "Mint Fresh" -> Color(0xFF30BA8C)
                                                "Midnight Dark" -> Color(0xFF191C1B)
                                                "Ocean Blue" -> Color(0xFF2196F3)
                                                "Sunset Warm" -> Color(0xFFFF9800)
                                                "Lavender Calm" -> Color(0xFF9C27B0)
                                                "Rose Soft" -> Color(0xFFE91E63)
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                        ).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.5f), CircleShape)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        themeName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showThemeDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Close") }
                }
            }
        }
    }
"""

content = content.replace(
    'Button(onClick = { showVaultDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Close") }\n                }\n            }\n        }\n    }',
    'Button(onClick = { showVaultDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Close") }\n                }\n            }\n        }\n    }\n' + theme_dialog
)

# 3. Add Theme Row inside the main Card
row_insertion = """
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showVaultDialog = true }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Manage Savings Vaults", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                
                HorizontalDivider()
                
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showThemeDialog = true }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("App Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(appTheme, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
"""

content = re.sub(r'                Row\(\s*modifier = Modifier\.fillMaxWidth\(\)\.clickable \{ showVaultDialog = true \}\.padding\(16\.dp\),\s*verticalAlignment = Alignment\.CenterVertically,\s*horizontalArrangement = Arrangement\.SpaceBetween\s*\) \{\s*Text\("Manage Savings Vaults", style = MaterialTheme\.typography\.titleMedium, fontWeight = FontWeight\.SemiBold\)\s*\}', row_insertion, content)

# 4. Remove the old App Theme Selection block completely
block_pattern = r'\s*// App Theme Selection\s*var themeExpanded by remember \{ mutableStateOf\(false\) \}\s*val themes = listOf\("Mint Fresh", "Midnight Dark", "Ocean Blue", "Sunset Warm", "Lavender Calm", "Rose Soft"\).*?Spacer\(modifier = Modifier\.height\(16\.dp\)\)'
content = re.sub(block_pattern, '', content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'w') as f:
    f.write(content)

