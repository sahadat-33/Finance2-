package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(viewModel: FinanceViewModel, navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isChecking by remember { mutableStateOf(false) }

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
                Text(
                    text = "Verify Your Email Address",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "We have sent a verification email to your address. Please check your inbox and verify your email to unlock cloud backup features.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                if (isChecking) {
                    CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                } else {
                    Button(
                        onClick = {
                            isChecking = true
                            scope.launch {
                                val isVerified = viewModel.checkEmailVerification()
                                isChecking = false
                                if (!isVerified) {
                                    Toast.makeText(context, "Email is not verified yet. Please check your inbox.", Toast.LENGTH_SHORT).show()
                                } else {
                                    if (!viewModel.isOnboardingComplete.value) {
                                        navController.navigate("onboarding_balance") { popUpTo("verification") { inclusive = true } }
                                    } else {
                                        navController.navigate("main") { popUpTo("verification") { inclusive = true } }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text("I Have Verified My Email")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = { viewModel.signOut() }) {
                    Text("Sign Out")
                }
            }
        }
    }
}
