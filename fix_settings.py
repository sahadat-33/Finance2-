import re

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Replace footer
content = content.replace(
    'Text("Designed & Developed by Sahadat Hossan"',
    'Text("Made with ❤️ by Sahadat"'
)

# Add appTheme state collection
content = content.replace(
    'val isDarkMode by viewModel.isDarkMode.collectAsState()',
    'val isDarkMode by viewModel.isDarkMode.collectAsState()\n    val appTheme by viewModel.appTheme.collectAsState()'
)

# Insert the Theme Selection Card right before Data Management
theme_card = """        // App Theme Selection
        var themeExpanded by remember { mutableStateOf(false) }
        val themes = listOf("Mint Fresh", "Midnight Dark", "Ocean Blue", "Sunset Warm", "Lavender Calm", "Rose Soft")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = if (isDarkTheme) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { themeExpanded = !themeExpanded }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("App Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Icon(if (themeExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = "Expand")
                }
                
                if (themeExpanded) {
                    HorizontalDivider()
                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        themes.forEach { themeName ->
                            val isSelected = appTheme == themeName
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setAppTheme(themeName) },
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
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        var dataManagementExpanded"""

content = content.replace("var dataManagementExpanded", theme_card)

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'w') as f:
    f.write(content)
