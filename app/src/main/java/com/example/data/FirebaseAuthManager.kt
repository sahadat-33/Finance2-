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
        
    suspend fun reauthenticate(password: String): Boolean {
        val user = auth?.currentUser ?: return false
        val email = user.email ?: return false
        return try {
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateEmail(newEmail: String): Boolean {
        val user = auth?.currentUser ?: return false
        return try {
            user.updateEmail(newEmail).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteAccount(): Boolean {
        val user = auth?.currentUser ?: return false
        return try {
            user.delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
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

    val currentUserId: String?
        get() = auth?.currentUser?.uid

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


    val isEmailVerified: Boolean
        get() = auth?.currentUser?.isEmailVerified ?: false

    suspend fun checkEmailVerification(): Boolean {
        return try {
            auth?.currentUser?.reload()?.await()
            auth?.currentUser?.isEmailVerified ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    val currentUser: com.google.firebase.auth.FirebaseUser?
        get() = auth?.currentUser


    suspend fun reauthenticate(password: String): Boolean {
        val user = auth?.currentUser ?: return false
        val email = user.email ?: return false
        return try {
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateEmail(newEmail: String): Boolean {
        val user = auth?.currentUser ?: return false
        return try {
            user.updateEmail(newEmail).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteAccount(): Boolean {
        val user = auth?.currentUser ?: return false
        return try {
            user.delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
