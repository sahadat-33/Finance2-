import re

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'r') as f:
    content = f.read()

pattern = r'\s*HorizontalDivider\(\)\s*Row\(\s*modifier = Modifier\.fillMaxWidth\(\)\.padding\(16\.dp\),\s*verticalAlignment = Alignment\.CenterVertically,\s*horizontalArrangement = Arrangement\.SpaceBetween\s*\)\s*\{\s*Text\("Enable PIN Lock".*?\}\s*\)\s*\}\s*HorizontalDivider\(\)\s*Row\(\s*modifier = Modifier\.fillMaxWidth\(\)\.clickable \{ showCurrencyDialog = true \}\.padding\(16\.dp\),\s*verticalAlignment = Alignment\.CenterVertically,\s*horizontalArrangement = Arrangement\.SpaceBetween\s*\)\s*\{\s*Text\("Change Your Currency", style = MaterialTheme\.typography\.titleMedium, fontWeight = FontWeight\.SemiBold\)\s*\}'

new_rows = """
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showOthersDialog = true }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Others", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }"""

content = re.sub(pattern, new_rows, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/SettingsScreen.kt', 'w') as f:
    f.write(content)
