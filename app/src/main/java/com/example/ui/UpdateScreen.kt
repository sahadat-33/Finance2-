package com.example.ui

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.example.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(onBack: () -> Unit, onNavigateToAbout: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var isChecking by remember { mutableStateOf(false) }
    var updateStatusMessage by remember { mutableStateOf("Up to date") }
    var updateAvailable by remember { mutableStateOf(false) }
    var downloadUrl by remember { mutableStateOf<String?>(null) }
    var checkAttempted by remember { mutableStateOf(false) }

    fun parseVersion(versionStr: String): List<Int> {
        return versionStr.replace(Regex("[^0-9.]"), "").split(".").mapNotNull { it.toIntOrNull() }
    }

    fun isNewerVersion(current: String, latest: String): Boolean {
        val currentParts = parseVersion(current)
        val latestParts = parseVersion(latest)
        val maxLength = maxOf(currentParts.size, latestParts.size)
        
        for (i in 0 until maxLength) {
            val curr = currentParts.getOrElse(i) { 0 }
            val lat = latestParts.getOrElse(i) { 0 }
            if (lat > curr) return true
            if (lat < curr) return false
        }
        return false
    }

    fun checkForUpdates() {
        if (isChecking) return
        isChecking = true
        checkAttempted = true
        
        val sharedPrefs = context.getSharedPreferences("UpdateCache", Context.MODE_PRIVATE)
        val lastCheckTime = sharedPrefs.getLong("last_check_time", 0L)
        val lastCheckVersion = sharedPrefs.getString("last_check_version", null)
        val lastCheckUrl = sharedPrefs.getString("last_check_url", null)
        val currentTime = System.currentTimeMillis()
        
        // 1 hour cache = 3600000 ms
        if (currentTime - lastCheckTime < 3600000L && lastCheckVersion != null) {
            val currentVersion = BuildConfig.VERSION_NAME
            val isNewer = isNewerVersion(currentVersion, lastCheckVersion)
            if (isNewer) {
                val cleanName = lastCheckVersion.ifEmpty { "new version" }
                updateStatusMessage = "A new version ($cleanName) is available"
                updateAvailable = true
                downloadUrl = lastCheckUrl
            } else {
                updateStatusMessage = "This is the newest version."
                updateAvailable = false
                downloadUrl = null
            }
            isChecking = false
            return
        }

        coroutineScope.launch(Dispatchers.IO) {
            val currentVersion = BuildConfig.VERSION_NAME
            val primaryResult = runCatching {
                kotlinx.coroutines.withTimeoutOrNull(8_000L) {
                    com.example.data.UpdateChecker.checkForUpdate(currentVersion)
                }
            }
            
            when {
                primaryResult.isSuccess && primaryResult.getOrNull() != null -> {
                    val info = primaryResult.getOrNull()!!
                    val releaseName = info.version
                    val assetUrl = info.downloadUrl
                    sharedPrefs.edit()
                        .putLong("last_check_time", currentTime)
                        .putString("last_check_version", releaseName)
                        .putString("last_check_url", assetUrl)
                        .apply()
                    withContext(Dispatchers.Main) {
                        val cleanName = releaseName.ifEmpty { "new version" }
                        updateStatusMessage = "A new version ($cleanName) is available"
                        updateAvailable = true
                        downloadUrl = assetUrl
                        isChecking = false
                    }
                }
                primaryResult.isSuccess -> {
                    sharedPrefs.edit()
                        .putLong("last_check_time", currentTime)
                        .putString("last_check_version", currentVersion)
                        .putString("last_check_url", null)
                        .apply()
                    withContext(Dispatchers.Main) {
                        updateStatusMessage = "This is the newest version."
                        updateAvailable = false
                        downloadUrl = null
                        isChecking = false
                    }
                }
                else -> {
                    try {
                        val url = URL("https://api.github.com/repos/sahadat-33/Finance2-/releases/tags/Apk")
                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                        connection.setRequestProperty("User-Agent", "FinanceTracker-App")
                        
                        val responseCode = connection.responseCode
                        if (responseCode == 200) {
                            val response = connection.inputStream.bufferedReader().use { it.readText() }
                            val jsonObject = JSONObject(response)
                            val releaseName = jsonObject.optString("name", "") // e.g., "v4.0.6"
                            
                            val isNewer = isNewerVersion(currentVersion, releaseName)
                            
                            val assets = jsonObject.optJSONArray("assets")
                            var assetUrl: String? = null
                            if (assets != null && assets.length() > 0) {
                                assetUrl = assets.getJSONObject(0).optString("browser_download_url")
                            }
                            
                            // Cache the successful result
                            sharedPrefs.edit()
                                .putLong("last_check_time", currentTime)
                                .putString("last_check_version", releaseName)
                                .putString("last_check_url", assetUrl)
                                .apply()

                            withContext(Dispatchers.Main) {
                                if (isNewer) {
                                    val cleanName = releaseName.ifEmpty { "new version" }
                                    updateStatusMessage = "A new version ($cleanName) is available"
                                    updateAvailable = true
                                    downloadUrl = assetUrl
                                } else {
                                    updateStatusMessage = "This is the newest version."
                                    updateAvailable = false
                                    downloadUrl = null
                                }
                            }
                        } else {
                            val errorResponse = try { connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "" } catch(e:Exception){""}
                            Log.e("UpdateCheck", "HTTP Error $responseCode: $errorResponse")
                            withContext(Dispatchers.Main) {
                                if (responseCode == 403 && errorResponse.contains("rate limit", ignoreCase = true)) {
                                    updateStatusMessage = "Too many checks — please try again in a bit."
                                } else {
                                    updateStatusMessage = "Couldn't check for updates. Please try again later."
                                }
                                updateAvailable = false
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("UpdateCheck", "Exception: ${e.message}", e)
                        withContext(Dispatchers.Main) {
                            updateStatusMessage = "Couldn't check for updates. Please try again later."
                            updateAvailable = false
                        }
                    } finally {
                        withContext(Dispatchers.Main) {
                            isChecking = false
                        }
                    }
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Image(
                painter = painterResource(id = R.drawable.icon_image_1780221523424),
                contentDescription = "App Icon",
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Finance Tracker",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(56.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isChecking) {
                        checkForUpdates()
                    }
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Check for updates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (isChecking) {
                        Spacer(modifier = Modifier.width(12.dp))
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isChecking) {
                        Text(
                            text = updateStatusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (updateAvailable && downloadUrl != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        try {
                            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                            request.setTitle("Finance Tracker Update")
                            request.setDescription("Downloading latest version...")
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Finance-Tracker_latest.apk")
                            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            downloadManager.enqueue(request)
                        } catch (e: Exception) {
                            // Ignored
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Download Update")
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Credits",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigateToAbout() }.padding(8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "©2026 Finance-tracker",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
