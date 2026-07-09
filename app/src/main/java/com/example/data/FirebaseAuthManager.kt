package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

import com.google.firebase.FirebaseOptions

class FirebaseAuthManager(context: Context) {
    init {
        try {
            FirebaseApp.getInstance()
        } catch (e: Exception) {
            Log.e("Firebase", "Failed to init Firebase: ${e.message}")
        }
    }

    val auth: FirebaseAuth? by lazy { 
        try {
            FirebaseAuth.getInstance() 
        } catch (e: Throwable) {
            null
        }
    }

    val isUserSignedIn: Boolean
        get() = auth?.currentUser != null

    suspend fun createAccount(email: String, pass: String, username: String): Boolean {
        return try {
            val res = auth?.createUserWithEmailAndPassword(email, pass)?.await()
            val user = res?.user
            if (user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                user.updateProfile(profileUpdates).await()
                try { user.sendEmailVerification().await() } catch(e: Exception) {}
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("Auth", "Failed to create account: ${e.message}")
            false
        }
    }

    suspend fun login(email: String, pass: String): Boolean {
        return try {
            auth?.signInWithEmailAndPassword(email, pass)?.await()
            true
        } catch (e: Exception) {
            Log.e("Auth", "Failed to login: ${e.message}")
            false
        }
    }

    suspend fun sendPasswordReset(email: String): Boolean {
        return try {
            auth?.sendPasswordResetEmail(email)?.await()
            true
        } catch (e: Exception) {
            Log.e("Auth", "Failed to send reset: ${e.message}")
            false
        }
    }

    suspend fun updateUsername(username: String): Boolean {
        return try {
            val user = auth?.currentUser ?: return false
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()
            user.updateProfile(profileUpdates).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun signOut() {
        auth?.signOut()
    }
}
