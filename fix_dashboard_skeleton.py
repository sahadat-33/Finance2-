import re

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Add states
old_states = """    val formattedMonth = monthNameFormatter.format(selectedCalendar.time)

    // Format utility for amounts
    fun formatTaka(amount: Double): String {"""

new_states = """    val formattedMonth = monthNameFormatter.format(selectedCalendar.time)

    var skeletonVisible by remember { mutableStateOf(true) }
    LaunchedEffect(stats.transactions) {
        if (stats.transactions.isNotEmpty()) {
            skeletonVisible = false
        } else {
            kotlinx.coroutines.delay(500)
            skeletonVisible = false
        }
    }

    // Format utility for amounts
    fun formatTaka(amount: Double): String {"""

content = content.replace(old_states, new_states)

# Wrap Column
old_column = """    var isVaultExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {"""

new_column = """    var isVaultExpanded by remember { mutableStateOf(false) }

    if (skeletonVisible) {
        DashboardSkeleton()
    } else {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {"""

content = content.replace(old_column, new_column)
# And append a closing brace at the very end
content = content + "\n}"

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.write(content)
