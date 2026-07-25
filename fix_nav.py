import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

if 'import com.example.ui.AboutScreen' not in content:
    content = content.replace('import com.example.ui.UpdateScreen', 'import com.example.ui.UpdateScreen\nimport com.example.ui.AboutScreen')

old_update_nav = """                        composable("update") {
                            UpdateScreen(onBack = { navController.popBackStack() })
                        }"""
new_update_nav = """                        composable("update") {
                            UpdateScreen(onBack = { navController.popBackStack() }, onNavigateToAbout = { navController.navigate("about") })
                        }
                        composable("about") {
                            AboutScreen(onBack = { navController.popBackStack() })
                        }"""

content = content.replace(old_update_nav, new_update_nav)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
