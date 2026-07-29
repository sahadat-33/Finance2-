#!/bin/bash
sed -i '/var isVaultExpanded by remember { mutableStateOf(false) }/a\
\
    val showAnalysisOnDashboard by viewModel.showAnalysisOnDashboard.collectAsState()\
' app/src/main/java/com/example/ui/DashboardScreen.kt

sed -i '/\/\/ No month selector row here since it is synchronized globally in the top app bar/a\
\
        if (showAnalysisOnDashboard) {\
            IncomeSpentAnalysisCard(viewModel = viewModel)\
        }\
' app/src/main/java/com/example/ui/DashboardScreen.kt

