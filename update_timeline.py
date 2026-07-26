import re

file_path = "app/src/main/java/com/example/ui/TimelineScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# PART D: Add viewModel = viewModel to TimelineRowItem calls
content = content.replace(
    '''TimelineRowItem(
                            transaction = tx,
                            formatTaka = ::formatTaka,
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )''',
    '''TimelineRowItem(
                            transaction = tx,
                            formatTaka = ::formatTaka,
                            onDelete = { viewModel.deleteTransaction(tx) },
                            viewModel = viewModel
                        )'''
)

# PART A: Add viewModel to TimelineRowItem signature
content = content.replace(
    '''fun TimelineRowItem(
    transaction: Transaction,
    formatTaka: (Double) -> String,
    onDelete: () -> Unit
)''',
    '''@androidx.compose.foundation.ExperimentalFoundationApi
fun TimelineRowItem(
    transaction: Transaction,
    formatTaka: (Double) -> String,
    onDelete: () -> Unit,
    viewModel: FinanceViewModel
)'''
)

# PART B: Add showEditDialog state
content = content.replace(
    '''    var showDeleteConfirm by remember { mutableStateOf(false) }''',
    '''    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }'''
)

content = content.replace(
    '''    if (showDeleteConfirm) {''',
    '''    if (showEditDialog) {
        EditTransactionDialog(
            transaction = transaction,
            viewModel = viewModel,
            onDismiss = { showEditDialog = false }
        )
    }

    if (showDeleteConfirm) {'''
)

# PART C: Change clickable to combinedClickable
# Wait, look at TimelineRowItem card
card_pattern = r'''Card\(\s*modifier = Modifier\s*\.fillMaxWidth\(\)\s*\.testTag\("timeline_row_\$\{transaction\.id\}"\),'''
card_replacement = '''Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showEditDialog = true }
            )
            .testTag("timeline_row_${transaction.id}"),'''

content = re.sub(card_pattern, card_replacement, content)

# IMPORTS
if "import androidx.compose.foundation.combinedClickable" not in content:
    content = content.replace(
        "import androidx.compose.foundation.clickable",
        "import androidx.compose.foundation.clickable\nimport androidx.compose.foundation.combinedClickable\nimport androidx.compose.foundation.ExperimentalFoundationApi"
    )


with open(file_path, "w") as f:
    f.write(content)

