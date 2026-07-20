package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(viewModel: FinanceViewModel, onBack: () -> Unit) {
    var username by remember { mutableStateOf(viewModel.currentUserName ?: "") }
    var email by remember { mutableStateOf(viewModel.currentUserEmail ?: "") }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    
    var showMenu by remember { mutableStateOf(false) }
    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val lastSyncTime by viewModel.lastSyncTimestamp.collectAsState()
    
    val syncTimeString = remember(lastSyncTime) {
        if (lastSyncTime == 0L) {
            "Not synced yet"
        } else {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            "Last synced to database: ${sdf.format(Date(lastSyncTime))}"
        }
    }

    val brandMint = Color(0xFF30BA8C)
    val lightMintBackground = Color(0xFFE8F7F2)
    
    if (showChangeEmailDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newEmail by remember { mutableStateOf("") }
        var emailError by remember { mutableStateOf("") }
        var isReauthLoading by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showChangeEmailDialog = false },
            title = { Text("Change Email") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Please re-authenticate with your current password to change your email address.", style = MaterialTheme.typography.bodySmall)
                    if (emailError.isNotEmpty()) {
                        Text(emailError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("New Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isReauthLoading = true
                        emailError = ""
                        scope.launch {
                            val reauthSuccess = viewModel.reauthenticate(currentPassword)
                            if (reauthSuccess) {
                                val updateSuccess = viewModel.updateEmail(newEmail)
                                if (updateSuccess) {
                                    email = newEmail
                                    showChangeEmailDialog = false
                                } else {
                                    emailError = "Failed to update email. Make sure it is valid and not already in use."
                                }
                            } else {
                                emailError = "Incorrect password."
                            }
                            isReauthLoading = false
                        }
                    },
                    enabled = currentPassword.isNotEmpty() && newEmail.isNotEmpty() && !isReauthLoading
                ) {
                    if (isReauthLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    else Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeEmailDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showDeleteDialog) {
        var confirmationText by remember { mutableStateOf("") }
        var deleteError by remember { mutableStateOf("") }
        var isDeleting by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This will permanently delete your account and all data. This cannot be undone.", color = MaterialTheme.colorScheme.error)
                    Text("Type 'DELETE' to confirm:")
                    OutlinedTextField(
                        value = confirmationText,
                        onValueChange = { confirmationText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (deleteError.isNotEmpty()) {
                        Text(deleteError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        scope.launch {
                            try {
                                val uid = viewModel.currentUserId
                                if (uid != null) {
                                    val db = FirebaseFirestore.getInstance()
                                    // Normally you'd want a Cloud Function or recursive delete, 
                                    // but for this simple app, we can just clear the main document/collections if known.
                                    // However, standard user doc deletion:
                                    db.collection("users").document(uid).delete().await()
                                }
                                val deleted = viewModel.deleteAccount()
                                if (deleted) {
                                    viewModel.signOut()
                                    onBack()
                                } else {
                                    deleteError = "Failed to delete account. You may need to sign in again."
                                }
                            } catch (e: Exception) {
                                deleteError = e.message ?: "Unknown error"
                            }
                            isDeleting = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = confirmationText == "DELETE" && !isDeleting
                ) {
                    if (isDeleting) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onError)
                    else Text("Permanently Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Change Password") },
                                onClick = { 
                                    showMenu = false
                                    scope.launch {
                                        viewModel.sendPasswordReset(email)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Change Email") },
                                onClick = { 
                                    showMenu = false
                                    showChangeEmailDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Account", color = MaterialTheme.colorScheme.error) },
                                onClick = { 
                                    showMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = lightMintBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync, 
                        contentDescription = "Sync Status",
                        tint = brandMint
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = syncTimeString,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Profile Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandMint,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User Icon", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandMint,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Button(
                    onClick = {
                        isLoading = true
                        scope.launch {
                            viewModel.updateUsername(username)
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandMint),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        viewModel.signOut()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onError)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Version ${com.example.BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
