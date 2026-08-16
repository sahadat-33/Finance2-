package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val releaseNotes: String = "",
    val forceUpdate: Boolean = false,
    val downloadCount: Int = 0
)

object UpdateChecker {
    private const val REPLIT_LATEST_URL =
        "https://fintrack-releases--cloudsahadat.replit.app/api/releases/latest"
    private const val GITHUB_LATEST_URL =
        "https://api.github.com/repos/sahadat-33/Finance2-/releases/tags/Apk"

    /**
     * Returns UpdateInfo if a newer version exists, null if already up to date.
     * Tries Replit primary first, falls back to GitHub releases.
     * Throws if both checks fail or encounter network errors.
     */
    suspend fun checkForUpdate(currentVersion: String): UpdateInfo? = withContext(Dispatchers.IO) {
        // 1. Try Replit primary source
        val replitResult = runCatching {
            withTimeoutOrNull(6000L) {
                val connection = URL(REPLIT_LATEST_URL).openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val json = connection.inputStream.bufferedReader().use { it.readText() }
                    val release = JSONObject(json)
                    val latest = release.optString("version", "")
                    val downloadUrl = release.optString("downloadUrl", "")
                    val rawNotes = release.optString("releaseNotes", "")
                    val releaseNotes = rawNotes.replace("\r\n", "\n").trim()
                    val forceUpdate = release.optBoolean("forceUpdate", false)
                    val downloadCount = release.optInt("downloadCount", 0)
                    if (latest.isNotEmpty() && downloadUrl.isNotEmpty()) {
                        UpdateInfo(
                            version = latest,
                            downloadUrl = downloadUrl,
                            releaseNotes = releaseNotes,
                            forceUpdate = forceUpdate,
                            downloadCount = downloadCount
                        )
                    } else null
                } else null
            }
        }.getOrNull()

        if (replitResult != null) {
            return@withContext if (isNewer(replitResult.version, currentVersion)) {
                replitResult
            } else null
        }

        // 2. Try GitHub fallback source
        val gitHubResult = runCatching {
            withTimeoutOrNull(8000L) {
                val connection = URL(GITHUB_LATEST_URL).openConnection() as HttpURLConnection
                connection.connectTimeout = 6000
                connection.readTimeout = 6000
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.setRequestProperty("User-Agent", "FinanceTracker-App")
                
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val releaseName = jsonObject.optString("name", "").ifEmpty {
                        jsonObject.optString("tag_name", "")
                    }
                    val assets = jsonObject.optJSONArray("assets")
                    var assetUrl: String? = null
                    if (assets != null && assets.length() > 0) {
                        assetUrl = assets.getJSONObject(0).optString("browser_download_url")
                    }
                    val rawNotes = jsonObject.optString("body", "")
                    val releaseNotes = rawNotes.replace("\r\n", "\n").trim()
                    if (releaseName.isNotEmpty() && !assetUrl.isNullOrEmpty()) {
                        UpdateInfo(
                            version = releaseName,
                            downloadUrl = assetUrl,
                            releaseNotes = releaseNotes,
                            forceUpdate = false,
                            downloadCount = 0
                        )
                    } else null
                } else {
                    Log.w("UpdateChecker", "GitHub API returned response code: $responseCode")
                    null
                }
            }
        }.getOrNull()

        if (gitHubResult != null) {
            return@withContext if (isNewer(gitHubResult.version, currentVersion)) {
                gitHubResult
            } else null
        }

        // If both sources failed to return valid release data, throw error to let caller decide
        throw Exception("Unable to reach update servers (Replit & GitHub).")
    }

    /**
     * Correct segment-by-segment numeric comparison.
     * Handles multi-digit segments properly (e.g. 4.10.0 > 4.9.0) and strips prefixes like "v".
     */
    fun isNewer(latest: String, current: String): Boolean {
        val l = latest.replace(Regex("[^0-9.]"), "").split(".").map { it.toIntOrNull() ?: 0 }
        val c = current.replace(Regex("[^0-9.]"), "").split(".").map { it.toIntOrNull() ?: 0 }
        val len = maxOf(l.size, c.size)
        for (i in 0 until len) {
            val lv = l.getOrElse(i) { 0 }
            val cv = c.getOrElse(i) { 0 }
            if (lv != cv) return lv > cv
        }
        return false
    }
}
