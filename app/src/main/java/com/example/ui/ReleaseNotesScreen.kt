package com.example.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.data.UpdateCheckWorker
import com.example.data.UpdateChecker
import com.example.viewmodel.FinanceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseNotesScreen(
    viewModel: FinanceViewModel? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val updatePrefs = remember {
        context.getSharedPreferences(UpdateCheckWorker.PREFS_NAME, Context.MODE_PRIVATE)
    }

    var isLoading by remember { mutableStateOf(false) }
    var versionString by remember {
        mutableStateOf(
            updatePrefs.getString(UpdateCheckWorker.KEY_AVAILABLE_VERSION, "").orEmpty()
                .ifEmpty { BuildConfig.VERSION_NAME }
        )
    }
    var releaseNotesText by remember {
        mutableStateOf(
            updatePrefs.getString(UpdateCheckWorker.KEY_RELEASE_NOTES, "").orEmpty()
        )
    }

    LaunchedEffect(Unit) {
        if (releaseNotesText.isEmpty()) {
            isLoading = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val info = UpdateChecker.checkForUpdate(BuildConfig.VERSION_NAME)
                    withContext(Dispatchers.Main) {
                        if (info != null && info.releaseNotes.isNotEmpty()) {
                            versionString = info.version
                            releaseNotesText = info.releaseNotes
                        }
                        isLoading = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Release Notes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Version $versionString",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Latest published updates and changes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("release_notes_content")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        val displayNotes = if (releaseNotesText.isNotBlank()) {
                            releaseNotesText
                        } else {
                            "• On-the-spot mathematical calculations on transaction amount fields.\n" +
                            "• Real-time savings vault transfer and deposit synchronization.\n" +
                            "• Enhanced stability and background update checks.\n" +
                            "• UI and navigation polish for smooth transaction management."
                        }

                        Text(
                            text = displayNotes,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.35,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
