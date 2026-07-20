import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add import
if "import com.example.ui.OthersScreen" not in content:
    content = content.replace("import com.example.ui.SettingsScreen", "import com.example.ui.SettingsScreen\nimport com.example.ui.OthersScreen")

# Update settings composable
settings_block = """                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateToProfile = { navController.navigate("profile") },
                                onNavigateToAuth = { rootNavController.navigate("welcome_auth") }
                            )
                        }"""
new_settings_block = """                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateToProfile = { navController.navigate("profile") },
                                onNavigateToAuth = { rootNavController.navigate("welcome_auth") },
                                onNavigateToOthers = { navController.navigate("others") }
                            )
                        }
                        composable("others") {
                            OthersScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }"""
content = content.replace(settings_block, new_settings_block)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

