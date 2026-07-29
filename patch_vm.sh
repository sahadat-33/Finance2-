#!/bin/bash
sed -i '/val lastSyncType: StateFlow<String> = _lastSyncType.asStateFlow()/a\
\
    private val _showAnalysisOnDashboard = MutableStateFlow(sharedPrefs.getBoolean("show_analysis_on_dashboard", false))\
    val showAnalysisOnDashboard: StateFlow<Boolean> = _showAnalysisOnDashboard.asStateFlow()\
\
    fun setShowAnalysisOnDashboard(enabled: Boolean) {\
        _showAnalysisOnDashboard.value = enabled\
        sharedPrefs.edit().putBoolean("show_analysis_on_dashboard", enabled).apply()\
    }' app/src/main/java/com/example/viewmodel/FinanceViewModel.kt
