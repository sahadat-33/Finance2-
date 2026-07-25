import re

with open('app/src/main/java/com/example/ui/theme/Theme.kt', 'r') as f:
    content = f.read()

content = content.replace('themeName: String = "Mint Fresh"', 'themeName: String = "Ocean Blue"')
content = content.replace('"Midnight Dark" -> MidnightDarkScheme\n', '')
content = content.replace('else -> if (darkTheme) MintDark else MintLight // Default to Mint Fresh', 'else -> if (darkTheme) BlueDark else BlueLight')

with open('app/src/main/java/com/example/ui/theme/Theme.kt', 'w') as f:
    f.write(content)
