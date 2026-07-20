with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add import
if "import com.example.ui.UpdateScreen" not in content:
    content = content.replace("import com.example.ui.OthersScreen", "import com.example.ui.OthersScreen\nimport com.example.ui.UpdateScreen")

# Add composable
others_block = """                        composable("others") {
                            OthersScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }"""
new_others_block = """                        composable("others") {
                            OthersScreen(viewModel = viewModel, onBack = { navController.popBackStack() }, onNavigateToUpdate = { navController.navigate("update") })
                        }
                        composable("update") {
                            UpdateScreen(onBack = { navController.popBackStack() })
                        }"""
content = content.replace(others_block, new_others_block)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
