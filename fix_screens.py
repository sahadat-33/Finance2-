import os

screens = [
    'app/src/main/java/com/example/ui/DashboardScreen.kt',
    'app/src/main/java/com/example/ui/TimelineScreen.kt',
    'app/src/main/java/com/example/ui/YearlySummaryScreen.kt'
]

for screen in screens:
    with open(screen, 'r') as f:
        content = f.read()
    
    if 'viewModel.triggerFetchFromCloud()' not in content:
        # Find the first Composable function block
        import re
        match = re.search(r'(@Composable\s*fun\s+\w+\(.*?\)\s*\{)', content, re.DOTALL)
        if match:
            sig = match.group(1)
            new_sig = sig + '\n    androidx.compose.runtime.LaunchedEffect(Unit) {\n        viewModel.triggerFetchFromCloud()\n    }\n'
            content = content.replace(sig, new_sig)
            
            with open(screen, 'w') as f:
                f.write(content)

