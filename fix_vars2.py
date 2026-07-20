import re

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'r') as f:
    content = f.read()

vars_to_move = r'\s*var showNewYearDialog by remember \{ mutableStateOf\(false\) \}\s*if \(showNewYearDialog\) \{\s*AlertDialog\(\s*onDismissRequest = \{ showNewYearDialog = false \},\s*title = \{ Text\("Start New Year"\) \},\s*text = \{ Text\("This will archive current year\'s matrix to local JSON history, clear active logs, and carry over final cash balance \(\$\{formatAmt\(summaryRows\.totalRow\.cash\)\}\) to Jan 1st of the new year as \'Last Month Carryover\'\. Your custom categories and savings vault totals will remain intact\. Proceed\?"\) \},\s*confirmButton = \{\s*TextButton\(onClick = \{\s*viewModel\.startNewYear\(summaryRows\.totalRow\.cash, context\)\s*showNewYearDialog = false\s*\}\) \{\s*Text\("Confirm"\)\s*\}\s*\},\s*dismissButton = \{\s*TextButton\(onClick = \{ showNewYearDialog = false \}\) \{\s*Text\("Cancel"\)\s*\}\s*\}\s*\)\s*\}\s*var tier1DialogExpanded by remember \{ mutableStateOf\(false\) \}\s*if \(tier1DialogExpanded\) \{\s*AlertDialog\(\s*onDismissRequest = \{ tier1DialogExpanded = false \},\s*title = \{ Text\("Select Scope of Export Data"\) \},\s*text = \{\s*Column \{\s*Button\(onClick = \{ exportScopeDialog = "YEARLY"; tier1DialogExpanded = false \}, modifier = Modifier\.fillMaxWidth\(\)\) \{ Text\("Yearly Summary Data"\) \}\s*Spacer\(Modifier\.height\(8\.dp\)\)\s*Button\(onClick = \{ exportScopeDialog = "MONTHLY"; tier1DialogExpanded = false \}, modifier = Modifier\.fillMaxWidth\(\)\) \{ Text\("Monthly Detailed Breakdown"\) \}\s*\}\s*\},\s*confirmButton = \{\},\s*dismissButton = \{ TextButton\(onClick = \{ tier1DialogExpanded = false \}\) \{ Text\("Cancel"\) \} \}\s*\)\s*\}\s*if \(exportScopeDialog != null && !tier1DialogExpanded\) \{\s*AlertDialog\(\s*onDismissRequest = \{ exportScopeDialog = null \},\s*title = \{ Text\("Select File Destination Format"\) \},\s*text = \{\s*Column \{\s*Button\(onClick = \{ csvLauncher\.launch\("finance_export\.csv"\) \}, modifier = Modifier\.fillMaxWidth\(\)\) \{ Text\("CSV Spreadsheet \.csv"\) \}\s*Spacer\(Modifier\.height\(8\.dp\)\)\s*Button\(onClick = \{ pdfLauncher\.launch\("finance_export\.pdf"\) \}, modifier = Modifier\.fillMaxWidth\(\)\) \{ Text\("Print-Ready Document \.pdf"\) \}\s*Spacer\(Modifier\.height\(8\.dp\)\)\s*Button\(onClick = \{ jsonLauncher\.launch\("finance_export\.json"\) \}, modifier = Modifier\.fillMaxWidth\(\)\) \{ Text\("JSON Payload \.json"\) \}\s*\}\s*\},\s*confirmButton = \{\},\s*dismissButton = \{ TextButton\(onClick = \{ exportScopeDialog = null \}\) \{ Text\("Cancel"\) \} \}\s*\)\s*\}'

match = re.search(vars_to_move, content)
if match:
    extracted = match.group(0)
    content = content.replace(extracted, "")
    
    # insert before // Settings rows
    content = content.replace("        // Settings rows", extracted.lstrip() + "\n\n        // Settings rows")

    with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'w') as f:
        f.write(content)
else:
    print("Could not find the variables block.")

