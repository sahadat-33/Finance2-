package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class UpdateInfo(val version: String, val downloadUrl: String)

object UpdateChecker {
    private const val LATEST_URL =
        "https://fintrack-releases--cloudsahadat.replit.app/api/releases/latest"

    /**
     * Returns UpdateInfo if a newer version exists, null if already up to date.
     * Throws if the network call fails or the response is malformed.
     */
    suspend fun checkForUpdate(currentVersion: String): UpdateInfo? {
        return withContext(Dispatchers.IO) {
            val json = URL(LATEST_URL).readText()
            val release = JSONObject(json)
            val latest = release.getString("version")
            if (isNewer(latest, currentVersion))
                UpdateInfo(latest, release.getString("downloadUrl"))
            else null
        }
    }

    /**
     * Correct segment-by-segment numeric comparison.
     * Handles multi-digit segments properly (e.g. 4.10.0 > 4.9.0).
     * Do NOT use .any { x > y } — that is broken for multi-digit segments.
     */
    private fun isNewer(latest: String, current: String): Boolean {
        val l = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val c = current.split(".").map { it.toIntOrNull() ?: 0 }
        val len = maxOf(l.size, c.size)
        for (i in 0 until len) {
            val lv = l.getOrElse(i) { 0 }
            val cv = c.getOrElse(i) { 0 }
            if (lv != cv) return lv > cv
        }
        return false
    }
}
