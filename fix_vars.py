import re

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'r') as f:
    content = f.read()

# We want to move these lines:
vars_to_move = r'        var dataManagementExpanded by remember \{ mutableStateOf\(false\) \}\s*var exportScopeDialog by remember \{ mutableStateOf<String\?>\(null\) \} // null, "YEARLY", "MONTHLY"\s*val importLauncher = androidx\.activity\.compose\.rememberLauncherForActivityResult\(\s*androidx\.activity\.result\.contract\.ActivityResultContracts\.GetContent\(\)\s*\) \{ uri ->\s*uri\?\.let \{\s*// Dummy import action since JSON schema wasn\'t fully defined\.\s*android\.widget\.Toast\.makeText\(context, "Import successful!", android\.widget\.Toast\.LENGTH_SHORT\)\.show\(\)\s*\}\s*\}\s*val csvLauncher = androidx\.activity\.compose\.rememberLauncherForActivityResult\(\s*androidx\.activity\.result\.contract\.ActivityResultContracts\.CreateDocument\("text/csv"\)\s*\) \{ uri ->\s*uri\?\.let \{ destUri ->\s*coroutineScope\.launch\(Dispatchers\.IO\) \{\s*if \(exportScopeDialog == "YEARLY"\) exportDataToUri\(context, destUri, summaryRows\.monthlyData, selectedYear, "CSV"\)\s*else exportMonthlyDataToUri\(context, destUri, monthlyTransactions, selectedYear, "CSV"\)\s*exportScopeDialog = null\s*withContext\(Dispatchers\.Main\) \{ android\.widget\.Toast\.makeText\(context, "File saved successfully to storage!", android\.widget\.Toast\.LENGTH_SHORT\)\.show\(\) \}\s*\}\s*\}\s*\}\s*val pdfLauncher = androidx\.activity\.compose\.rememberLauncherForActivityResult\(\s*androidx\.activity\.result\.contract\.ActivityResultContracts\.CreateDocument\("application/pdf"\)\s*\) \{ uri ->\s*uri\?\.let \{ destUri ->\s*coroutineScope\.launch\(Dispatchers\.IO\) \{\s*if \(exportScopeDialog == "YEARLY"\) exportDataToUri\(context, destUri, summaryRows\.monthlyData, selectedYear, "PDF"\)\s*else exportMonthlyDataToUri\(context, destUri, monthlyTransactions, selectedYear, "PDF"\)\s*exportScopeDialog = null\s*withContext\(Dispatchers\.Main\) \{ android\.widget\.Toast\.makeText\(context, "File saved successfully to storage!", android\.widget\.Toast\.LENGTH_SHORT\)\.show\(\) \}\s*\}\s*\}\s*\}\s*val jsonLauncher = androidx\.activity\.compose\.rememberLauncherForActivityResult\(\s*androidx\.activity\.result\.contract\.ActivityResultContracts\.CreateDocument\("application/json"\)\s*\) \{ uri ->\s*uri\?\.let \{ destUri ->\s*coroutineScope\.launch\(Dispatchers\.IO\) \{\s*if \(exportScopeDialog == "YEARLY"\) exportDataToUri\(context, destUri, summaryRows\.monthlyData, selectedYear, "JSON"\)\s*else exportMonthlyDataToUri\(context, destUri, monthlyTransactions, selectedYear, "JSON"\)\s*exportScopeDialog = null\s*withContext\(Dispatchers\.Main\) \{ android\.widget\.Toast\.makeText\(context, "File saved successfully to storage!", android\.widget\.Toast\.LENGTH_SHORT\)\.show\(\) \}\s*\}\s*\}\s*\}'

match = re.search(vars_to_move, content)
if match:
    extracted = match.group(0)
    content = content.replace(extracted, "")
    
    # insert before // Settings rows
    content = content.replace("        // Settings rows", extracted + "\n\n        // Settings rows")

    with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'w') as f:
        f.write(content)
else:
    print("Could not find the variables block.")

