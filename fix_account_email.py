import re

with open('app/src/main/java/com/example/AccountSettingsScreen.kt', 'r') as f:
    content = f.read()

old_email_logic = """                    onClick = {
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
                    },"""

new_email_logic = """                    onClick = {
                        isReauthLoading = true
                        emailError = ""
                        scope.launch {
                            val reauthSuccess = viewModel.reauthenticate(currentPassword)
                            if (reauthSuccess) {
                                val result = viewModel.updateEmail(newEmail)
                                if (result == "SUCCESS") {
                                    emailError = "A verification link has been sent to $newEmail. Please check your inbox, click the verification link, then sign out and sign back in using your new email and your current password."
                                    // Optionally we could close the dialog after some delay, but user needs to read this.
                                    // Let's just leave it open with the message, or change state.
                                    // The instruction says: "Show the user a clear message..."
                                } else if (result == "EMAIL_IN_USE") {
                                    emailError = "This email is already in use by another account."
                                } else if (result == "INVALID_EMAIL") {
                                    emailError = "Invalid email format."
                                } else {
                                    emailError = "Failed: $result"
                                }
                            } else {
                                emailError = "Incorrect password (re-auth failure)."
                            }
                            isReauthLoading = false
                        }
                    },"""

content = content.replace(old_email_logic, new_email_logic)

with open('app/src/main/java/com/example/AccountSettingsScreen.kt', 'w') as f:
    f.write(content)
