import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_login_success = """                            onLoginSuccess = { 
                                if (!viewModel.isEmailVerifiedFlow.value) {
                                    rootNavController.navigate("verification") { popUpTo("welcome_auth") { inclusive = true } }
                                } else if (!viewModel.isOnboardingComplete.value) {
                                    rootNavController.navigate("main") { popUpTo("welcome_auth") { inclusive = true } }
                                } else {
                                    rootNavController.navigate("main") { popUpTo("welcome_auth") { inclusive = true } }
                                }
                            },
                            onBypass = { 
                                if (!viewModel.isOnboardingComplete.value) {
                                    rootNavController.navigate("main") { popUpTo("welcome_auth") { inclusive = true } }
                                } else {
                                    rootNavController.navigate("main") { popUpTo("welcome_auth") { inclusive = true } }
                                }
                            }"""

new_login_success = """                            onLoginSuccess = { 
                                if (!viewModel.isEmailVerifiedFlow.value) {
                                    rootNavController.navigate("verification") { popUpTo("welcome_auth") { inclusive = true } }
                                } else if (!viewModel.isOnboardingComplete.value) {
                                    viewModel.completeOnboarding() // Skip onboarding step
                                    rootNavController.navigate("main") { popUpTo("welcome_auth") { inclusive = true } }
                                } else {
                                    rootNavController.navigate("main") { popUpTo("welcome_auth") { inclusive = true } }
                                }
                            },
                            onBypass = { 
                                if (!viewModel.isOnboardingComplete.value) {
                                    viewModel.completeOnboarding() // Skip onboarding step
                                    rootNavController.navigate("main") { popUpTo("welcome_auth") { inclusive = true } }
                                } else {
                                    rootNavController.navigate("main") { popUpTo("welcome_auth") { inclusive = true } }
                                }
                            }"""

content = content.replace(old_login_success, new_login_success)

# Also comment out onboarding_balance composable instead of leaving it active just in case
content = content.replace('composable("onboarding_balance")', '// composable("onboarding_balance") intentionally disabled\n                    /* composable("onboarding_balance")')
content = content.replace('                        )\n                    }\n                    composable("main")', '                        )\n                    } */\n                    composable("main")')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/EmailVerificationScreen.kt', 'r') as f:
    ev_content = f.read()

old_ev = """                                    if (!viewModel.isOnboardingComplete.value) {
                                        navController.navigate("main") { popUpTo("verification") { inclusive = true } }
                                    } else {
                                        navController.navigate("main") { popUpTo("verification") { inclusive = true } }
                                    }"""

new_ev = """                                    if (!viewModel.isOnboardingComplete.value) {
                                        viewModel.completeOnboarding() // Skip onboarding step
                                        navController.navigate("main") { popUpTo("verification") { inclusive = true } }
                                    } else {
                                        navController.navigate("main") { popUpTo("verification") { inclusive = true } }
                                    }"""

ev_content = ev_content.replace(old_ev, new_ev)

with open('app/src/main/java/com/example/ui/EmailVerificationScreen.kt', 'w') as f:
    f.write(ev_content)
