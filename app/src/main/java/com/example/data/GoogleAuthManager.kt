package com.example.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class GoogleAuthManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    private var auth: FirebaseAuth? = null

    init {
        try {
            // Check if Firebase is already initialized
            FirebaseApp.getInstance()
            auth = FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            // Init manually using mock strings to prevent crash, user MUST provide real google-services.json
            try {
                val apiKey = "AIzaSyA8JKuCTMzEQQ62-_8EB-EPmbRQjFJdYxU"
                val appId = "1:257098997013:android:d3dd4ca3987786b4dadd1c"
                val projectId = "finance-tracker-779de"
                
                if (apiKey.isNotBlank() && apiKey != "PLACEHOLDER") {
                    val options = FirebaseOptions.Builder()
                        .setApiKey(apiKey)
                        .setApplicationId(appId)
                        .setProjectId(projectId)
                        .build()
                    FirebaseApp.initializeApp(context, options)
                    auth = FirebaseAuth.getInstance()
                } else {
                    Log.e("Firebase", "API Key is missing! Provide real values in Settings.")
                }
            } catch (ex: Throwable) {
                Log.e("Firebase", "Failed to init Firebase manual: ${ex.message}")
            }
        }
    }

    val isUserSignedIn: Boolean
        get() = auth?.currentUser != null

    suspend fun signInWithGoogle(activityContext: Context): Boolean {
        if (auth == null) {
            Log.e("Auth", "FirebaseAuth not initialized. Cannot sign in.")
            return false
        }
        
        try {
            // Need a valid Web Client ID string
            val webClientId = "257098997013-d8g5pjjihn3kvj2vbheqgou8vfcadh4e.apps.googleusercontent.com"
            if (webClientId.isEmpty() || webClientId == "PLACEHOLDER") {
                Log.e("Auth", "Missing Web Client ID for Google Sign-In.")
                return false
            }

            // Using raw string for hashed nonce 
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            var context = activityContext
            while (context is android.content.ContextWrapper) {
                if (context is android.app.Activity) break
                context = context.baseContext
            }
            val activity = context as? android.app.Activity ?: activityContext

            val result = credentialManager.getCredential(
                context = activity,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                // Auth with Firebase
                val fbCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth?.signInWithCredential(fbCredential)?.await()
                return true
            }
        } catch (e: Exception) {
            Log.e("AUTH_ERROR", "Sync handshake blocked by Google Cloud. Try the Local Backup option below. Error: ${e.message}")
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(activityContext, "Sync handshake blocked by Google Cloud. Try the Local Backup option below.", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (ex: Exception) {
                Log.e("AUTH_ERROR", "Could not show toast", ex)
            }
        }
        return false
    }
}
