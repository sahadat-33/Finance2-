package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeAuthScreen(
    viewModel: FinanceViewModel,
    onLoginSuccess: () -> Unit,
    onBypass: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var isSignUpMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Finance Tracker",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Username") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (passwordVisible)
                                    androidx.compose.material.icons.Icons.Filled.Visibility
                                else androidx.compose.material.icons.Icons.Filled.VisibilityOff

                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )

                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 16.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        val context = androidx.compose.ui.platform.LocalContext.current
                        
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                        } else {
                            Button(
                                onClick = {
                                    if (isSignUpMode) {
                                        if (email.isBlank() || password.isBlank() || username.isBlank()) {
                                            errorMessage = "Username, Email, and Password are required."
                                            return@Button
                                        }
                                        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]+\$".toRegex())) {
                                            errorMessage = "Please enter a valid email address."
                                            return@Button
                                        }
                                        isLoading = true
                                        errorMessage = ""
                                        scope.launch {
                                            try {
                                                val success = viewModel.createAccount(email, password, username)
                                                isLoading = false
                                                if (success) {
                                                    onLoginSuccess()
                                                } else {
                                                    errorMessage = "Account creation failed."
                                                }
                                            } catch(e: Exception) {
                                                isLoading = false
                                                android.widget.Toast.makeText(context, "Authentication database sync failed. Please check internet connection.", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        if (email.isBlank() || password.isBlank()) {
                                            errorMessage = "Email and Password are required."
                                            return@Button
                                        }
                                        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]+\$".toRegex())) {
                                            errorMessage = "Please enter a valid email address."
                                            return@Button
                                        }
                                        isLoading = true
                                        errorMessage = ""
                                        scope.launch {
                                            try {
                                                val success = viewModel.login(email, password)
                                                isLoading = false
                                                if (success) {
                                                    onLoginSuccess()
                                                } else {
                                                    errorMessage = "Login failed. Check credentials."
                                                }
                                            } catch (e: Exception) {
                                                isLoading = false
                                                android.widget.Toast.makeText(context, "Authentication database sync failed. Please check internet connection.", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Text(if (isSignUpMode) "Create Account" else "Log In")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TextButton(onClick = { 
                            isSignUpMode = !isSignUpMode 
                            errorMessage = ""
                        }) {
                            Text(if (isSignUpMode) "Already have an account? Log In" else "Don't have an account? Sign Up")
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.enableOfflineGuest()
                            onBypass()
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Use Offline Now", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
