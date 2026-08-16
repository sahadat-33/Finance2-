package com.example.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.data.UpdateCheckWorker
import com.example.data.UpdateChecker
import com.example.data.UpdateInfo
import com.example.viewmodel.FinanceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    viewModel: FinanceViewModel? = null,
    onBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToReleaseNotes: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val updatePrefs = remember {
        context.getSharedPreferences(UpdateCheckWorker.PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Read initial update availability immediately from persistent storage (set by UpdateCheckWorker / ViewModel)
    val savedIsAvailable = updatePrefs.getBoolean(UpdateCheckWorker.KEY_IS_UPDATE_AVAILABLE, false)
    val savedVersion = updatePrefs.getString(UpdateCheckWorker.KEY_AVAILABLE_VERSION, "") ?: ""
    val savedUrl = updatePrefs.getString(UpdateCheckWorker.KEY_DOWNLOAD_URL, "") ?: ""
    val savedNotes = updatePrefs.getString(UpdateCheckWorker.KEY_RELEASE_NOTES, "") ?: ""
    val savedForce = updatePrefs.getBoolean(UpdateCheckWorker.KEY_FORCE_UPDATE, false)
    val savedCount = updatePrefs.getInt(UpdateCheckWorker.KEY_DOWNLOAD_COUNT, 0)

    var isChecking by remember { mutableStateOf(false) }
    var updateAvailable by remember {
        mutableStateOf(savedIsAvailable && savedVersion.isNotEmpty() && savedUrl.isNotEmpty())
    }
    var availableVersionName by remember { mutableStateOf(savedVersion) }
    var downloadUrl by remember { mutableStateOf<String?>(if (savedUrl.isNotEmpty()) savedUrl else null) }
    var releaseNotesText by remember { mutableStateOf(savedNotes) }
    var forceUpdateFlag by remember { mutableStateOf(savedForce) }
    var downloadCountVal by remember { mutableStateOf(savedCount) }
    var updateStatusMessage by remember {
        mutableStateOf(if (updateAvailable) "" else "Up to date")
    }
    var showDialog by remember { mutableStateOf(false) }

    fun checkForUpdates(openDialogIfAvailable: Boolean = true) {
        if (isChecking) return
        isChecking = true

        coroutineScope.launch(Dispatchers.IO) {
            val currentVersion = BuildConfig.VERSION_NAME
            try {
                val info = UpdateChecker.checkForUpdate(currentVersion)
                val now = System.currentTimeMillis()
                withContext(Dispatchers.Main) {
                    if (info != null) {
                        updatePrefs.edit()
                            .putBoolean(UpdateCheckWorker.KEY_IS_UPDATE_AVAILABLE, true)
                            .putString(UpdateCheckWorker.KEY_AVAILABLE_VERSION, info.version)
                            .putString(UpdateCheckWorker.KEY_DOWNLOAD_URL, info.downloadUrl)
                            .putString(UpdateCheckWorker.KEY_RELEASE_NOTES, info.releaseNotes)
                            .putBoolean(UpdateCheckWorker.KEY_FORCE_UPDATE, info.forceUpdate)
                            .putInt(UpdateCheckWorker.KEY_DOWNLOAD_COUNT, info.downloadCount)
                            .putLong(UpdateCheckWorker.KEY_LAST_CHECK_TIME, now)
                            .apply()

                        updateAvailable = true
                        availableVersionName = info.version
                        downloadUrl = info.downloadUrl
                        releaseNotesText = info.releaseNotes
                        forceUpdateFlag = info.forceUpdate
                        downloadCountVal = info.downloadCount
                        updateStatusMessage = ""
                        if (openDialogIfAvailable) {
                            showDialog = true
                        }
                    } else {
                        updatePrefs.edit()
                            .putBoolean(UpdateCheckWorker.KEY_IS_UPDATE_AVAILABLE, false)
                            .putString(UpdateCheckWorker.KEY_AVAILABLE_VERSION, "")
                            .putString(UpdateCheckWorker.KEY_DOWNLOAD_URL, "")
                            .putString(UpdateCheckWorker.KEY_RELEASE_NOTES, "")
                            .putBoolean(UpdateCheckWorker.KEY_FORCE_UPDATE, false)
                            .putInt(UpdateCheckWorker.KEY_DOWNLOAD_COUNT, 0)
                            .putLong(UpdateCheckWorker.KEY_LAST_CHECK_TIME, now)
                            .apply()

                        updateAvailable = false
                        downloadUrl = null
                        availableVersionName = ""
                        updateStatusMessage = "This is the newest version."
                    }
                    isChecking = false
                }
            } catch (e: Exception) {
                Log.e("UpdateCheck", "Exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    updateStatusMessage = "Couldn't check for updates. Please try again later."
                    isChecking = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Circular App Icon matching Snaptube style
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp,
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_image_1780221523424),
                        contentDescription = "Finance Tracker App Icon",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            
            Text(
                text = "Finance Tracker",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // "Check for updates" Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = !isChecking) {
                        if (updateAvailable && downloadUrl != null) {
                            showDialog = true
                        } else {
                            checkForUpdates(openDialogIfAvailable = true)
                        }
                    }
                    .testTag("check_for_updates_row"),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Check for updates",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isChecking) {
                            Spacer(modifier = Modifier.width(12.dp))
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (updateAvailable) {
                            // Red Pill "New" Badge
                            Surface(
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("new_update_badge")
                            ) {
                                Text(
                                    text = "New",
                                    color = androidx.compose.ui.graphics.Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        } else if (!isChecking && updateStatusMessage.isNotEmpty()) {
                            Text(
                                text = updateStatusMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Navigate to update",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // "Release notes" Row (temporarily hidden for later use)
            /*
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigateToReleaseNotes() }
                    .testTag("release_notes_row"),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Release notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Navigate to release notes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            */
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Credits",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateToAbout() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("credits_button")
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "©2026 Finance-tracker",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Snaptube-style Popup Dialog when update is available and tapped
        if (showDialog && updateAvailable && downloadUrl != null) {
            UpdateDialog(
                updateInfo = UpdateInfo(
                    version = availableVersionName.ifEmpty { "new version" },
                    downloadUrl = downloadUrl!!,
                    releaseNotes = releaseNotesText,
                    forceUpdate = forceUpdateFlag,
                    downloadCount = downloadCountVal
                ),
                onDismiss = {
                    showDialog = false
                },
                onUpdate = {
                    showDialog = false
                }
            )
        }
    }
}
