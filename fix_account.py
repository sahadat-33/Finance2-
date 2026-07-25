import re

with open('app/src/main/java/com/example/AccountSettingsScreen.kt', 'r') as f:
    content = f.read()

# Add missing imports for Sync icon
if 'import androidx.compose.material.icons.filled.Refresh' not in content:
    content = content.replace('import androidx.compose.material.icons.filled.CloudSync', 'import androidx.compose.material.icons.filled.CloudSync\nimport androidx.compose.material.icons.filled.Refresh\nimport androidx.compose.foundation.clickable\nimport androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.material3.HorizontalDivider')

# Define state collection
old_state = """    val lastSyncTime by viewModel.lastSyncTimestamp.collectAsState()
    
    val syncTimeString = remember(lastSyncTime) {
        if (lastSyncTime == 0L) {
            "Not synced yet"
        } else {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            "Last synced to database: ${sdf.format(Date(lastSyncTime))}"
        }
    }"""

new_state = """    val lastSyncTime by viewModel.lastSyncTimestamp.collectAsState()
    val lastSyncCount by viewModel.lastSyncCount.collectAsState()
    val lastSyncSize by viewModel.lastSyncSize.collectAsState()
    val lastSyncType by viewModel.lastSyncType.collectAsState()
    
    var isManualSyncing by remember { mutableStateOf(false) }

    val syncTimeString = remember(lastSyncTime) {
        if (lastSyncTime == 0L) {
            "Not synced yet"
        } else {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            "Last synced to database: ${sdf.format(Date(lastSyncTime))}"
        }
    }
    
    val fullSyncString = remember(lastSyncTime, lastSyncCount, lastSyncSize, lastSyncType) {
        if (lastSyncTime == 0L) {
            "Not synced yet"
        } else {
            val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            val sizeKb = String.format("%.1f", lastSyncSize / 1024.0)
            "${sdf.format(Date(lastSyncTime))}, $lastSyncCount records, ${sizeKb}kB, Finance Tracker v${com.example.BuildConfig.VERSION_NAME}, $lastSyncType"
        }
    }"""
content = content.replace(old_state, new_state)

# Replace the layout
old_layout = """        Column(
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
        }"""

new_layout = """        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
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
                
                Column {
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Online Backup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fullSyncString,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f).padding(end = 16.dp)
                    )
                    
                    IconButton(
                        onClick = { 
                            isManualSyncing = true
                            viewModel.triggerManualSync { 
                                isManualSyncing = false
                            }
                        },
                        modifier = Modifier
                            .background(brandMint.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape)
                            .size(36.dp)
                    ) {
                        if (isManualSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = brandMint, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync Now", tint = brandMint, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable {
                            viewModel.signOut()
                            onBack()
                        }
                        .padding(vertical = 8.dp)
                )
            }
        }"""

content = content.replace(old_layout, new_layout)

with open('app/src/main/java/com/example/AccountSettingsScreen.kt', 'w') as f:
    f.write(content)
