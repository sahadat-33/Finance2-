import re

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Replace PIN and Currency in SettingsScreen with "Others"
old_rows = """                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable PIN Lock", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isPinEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                pinDialogMode = "SET"
                                showPinDialog = true
                            } else {
                                pinDialogMode = "DISABLE"
                                showPinDialog = true
                            }
                        }
                    )
                }
                
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showCurrencyDialog = true }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Change Your Currency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }"""

new_rows = """                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showOthersDialog = true }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Others", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }"""

content = content.replace(old_rows, new_rows)

# Insert the dialog code right before "var showCategoryDialog"
dialogs_insertion = """    var showOthersDialog by remember { mutableStateOf(false) }
    var updateDialogMessage by remember { mutableStateOf<String?>(null) }
    var updateDialogUrl by remember { mutableStateOf<String?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    if (showOthersDialog) {
        Dialog(onDismissRequest = { showOthersDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Others", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable PIN Lock", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = isPinEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    pinDialogMode = "SET"
                                    showPinDialog = true
                                } else {
                                    pinDialogMode = "DISABLE"
                                    showPinDialog = true
                                }
                            }
                        )
                    }
                    
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showCurrencyDialog = true }.padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Change Your Currency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (!isCheckingUpdate) {
                                isCheckingUpdate = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val url = java.net.URL("https://api.github.com/repos/sahadat-33/Finance2-/releases/latest")
                                        val connection = url.openConnection() as java.net.HttpURLConnection
                                        connection.requestMethod = "GET"
                                        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                                        
                                        if (connection.responseCode == 200) {
                                            val response = connection.inputStream.bufferedReader().use { it.readText() }
                                            val jsonObject = org.json.JSONObject(response)
                                            val tagName = jsonObject.getString("tag_name")
                                            val assets = jsonObject.getJSONArray("assets")
                                            var apkUrl = ""
                                            for (i in 0 until assets.length()) {
                                                val asset = assets.getJSONObject(i)
                                                if (asset.getString("name").endsWith(".apk")) {
                                                    apkUrl = asset.getString("browser_download_url")
                                                    break
                                                }
                                            }
                                            
                                            withContext(Dispatchers.Main) {
                                                if (tagName != "v" + com.example.BuildConfig.VERSION_NAME && tagName != com.example.BuildConfig.VERSION_NAME) {
                                                    updateDialogMessage = "A new version ($tagName) is available"
                                                    updateDialogUrl = apkUrl
                                                } else {
                                                    updateDialogMessage = "You're on the latest version."
                                                    updateDialogUrl = null
                                                }
                                                showUpdateDialog = true
                                                isCheckingUpdate = false
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                updateDialogMessage = "Couldn't check for updates. Please try again later."
                                                updateDialogUrl = null
                                                showUpdateDialog = true
                                                isCheckingUpdate = false
                                            }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            updateDialogMessage = "Couldn't check for updates. Please try again later."
                                            updateDialogUrl = null
                                            showUpdateDialog = true
                                            isCheckingUpdate = false
                                        }
                                    }
                                }
                            }
                        }.padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isCheckingUpdate) "Checking for Updates..." else "Check for Updates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showOthersDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Close") }
                }
            }
        }
    }
    
    if (showUpdateDialog) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("Update Status") },
            text = { Text(updateDialogMessage ?: "") },
            confirmButton = {
                if (updateDialogUrl != null && updateDialogUrl!!.isNotEmpty()) {
                    Button(onClick = {
                        intent.data = android.net.Uri.parse(updateDialogUrl)
                        context.startActivity(intent)
                        showUpdateDialog = false
                    }) {
                        Text("Download Update")
                    }
                } else {
                    Button(onClick = { showUpdateDialog = false }) {
                        Text("OK")
                    }
                }
            },
            dismissButton = {
                if (updateDialogUrl != null && updateDialogUrl!!.isNotEmpty()) {
                    TextButton(onClick = { showUpdateDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    var showCategoryDialog by remember { mutableStateOf(false) }"""

content = content.replace("    var showCategoryDialog by remember { mutableStateOf(false) }", dialogs_insertion)

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'w') as f:
    f.write(content)
