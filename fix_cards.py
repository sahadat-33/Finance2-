import re

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Pattern for the first Card
pattern1 = r'        // Settings rows\s*Card\(\s*modifier = Modifier\.fillMaxWidth\(\),\s*shape = RoundedCornerShape\(16\.dp\),\s*colors = CardDefaults\.cardColors\(containerColor = MaterialTheme\.colorScheme\.surface\),\s*border = if \(isDarkTheme\) null else BorderStroke\(1\.dp, MaterialTheme\.colorScheme\.outline\.copy\(alpha = 0\.4f\)\)\s*\) \{\s*Column \{.*?\}\s*\}'

new_card = """        // Settings rows
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
                
                HorizontalDivider()
                
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showThemeDialog = true }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("App Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(appTheme, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
                
                HorizontalDivider()

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
                
                HorizontalDivider()
                
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showOthersDialog = true }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Others", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }"""

content = re.sub(pattern1, new_card, content, flags=re.DOTALL)

# Pattern for the second Card
pattern2 = r'        Card\(\s*modifier = Modifier\.fillMaxWidth\(\),\s*shape = RoundedCornerShape\(16\.dp\),\s*colors = CardDefaults\.cardColors\(containerColor = MaterialTheme\.colorScheme\.surface\),\s*border = if \(isDarkTheme\) null else BorderStroke\(1\.dp, MaterialTheme\.colorScheme\.outline\.copy\(alpha = 0\.4f\)\)\s*\) \{\s*Column \{\s*Row\(\s*modifier = Modifier\.fillMaxWidth\(\)\.clickable \{ dataManagementExpanded = !dataManagementExpanded \}\.padding\(16\.dp\).*?\}\s*\}\s*\}'

content = re.sub(pattern2, "", content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'w') as f:
    f.write(content)
