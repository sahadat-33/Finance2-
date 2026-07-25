import re

with open('app/src/main/java/com/example/ui/UpdateScreen.kt', 'r') as f:
    content = f.read()

# Add Log import
if 'import android.util.Log' not in content:
    content = content.replace('import android.net.Uri', 'import android.net.Uri\nimport android.util.Log')

# We need to change checkForUpdates function
old_func = """    fun checkForUpdates() {
        if (isChecking) return
        isChecking = true
        checkAttempted = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://api.github.com/repos/sahadat-33/Finance2-/releases/tags/Apk")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val releaseName = jsonObject.optString("name", "") // e.g., "v4.0.6"
                    
                    val currentVersion = BuildConfig.VERSION_NAME
                    val isNewer = isNewerVersion(currentVersion, releaseName)
                    
                    val assets = jsonObject.optJSONArray("assets")
                    var assetUrl: String? = null
                    if (assets != null && assets.length() > 0) {
                        assetUrl = assets.getJSONObject(0).optString("browser_download_url")
                    }

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
                    withContext(Dispatchers.Main) {
                        updateStatusMessage = "Couldn't check for updates. Please try again later."
                        updateAvailable = false
                    }
                }
            } catch (e: Exception) {
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
    }"""

new_func = """    fun checkForUpdates() {
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
                    
                    val currentVersion = BuildConfig.VERSION_NAME
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
    }"""

content = content.replace(old_func, new_func)

with open('app/src/main/java/com/example/ui/UpdateScreen.kt', 'w') as f:
    f.write(content)
