import re

with open('app/src/main/java/com/example/ui/EditTransactionDialog.kt', 'r') as f:
    content = f.read()

pattern = re.compile(r'(keyboardOptions = KeyboardOptions\(\s*keyboardType = )KeyboardType\.Text(, \s*imeAction = androidx\.compose\.ui\.text\.input\.ImeAction\.Done\s*\))', re.DOTALL)
content = re.sub(pattern, r'\1KeyboardType.Number\2', content)

trailing_icon_code2 = """
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { amountStr += "+" },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.width(36.dp)
                            ) {
                                Text("+", style = MaterialTheme.typography.titleMedium)
                            }
                            TextButton(
                                onClick = { amountStr += "-" },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.width(36.dp)
                            ) {
                                Text("-", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    },"""

# Let's just put the trailingIcon before `keyboardOptions`
target = "keyboardOptions = KeyboardOptions("
content = content.replace(target, trailing_icon_code2.strip() + "\n                    " + target)

with open('app/src/main/java/com/example/ui/EditTransactionDialog.kt', 'w') as f:
    f.write(content)

