import re

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Fix 1: Animate the donut gauge arc on load
if "import androidx.compose.animation.core.animateFloatAsState" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.compose.animation.core.animateFloatAsState\nimport androidx.compose.animation.core.tween\nimport androidx.compose.animation.core.FastOutSlowInEasing")

old_sweep = """                            val sweepAngle = (percentSpentVal * 360f).toFloat().coerceIn(0f, 360f)
                            // Spent foreground arc (Lite Blue)
                            drawArc(
                                color = Color(0xFFA2C2FC),
                                startAngle = -90f,
                                sweepAngle = sweepAngle,"""

new_sweep = """                            val rawSweep = (percentSpentVal * 360f).toFloat().coerceIn(0f, 360f)
                            val animatedSweep by animateFloatAsState(
                                targetValue = rawSweep,
                                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                                label = "donut_sweep"
                            )
                            // Spent foreground arc (Lite Blue)
                            drawArc(
                                color = Color(0xFFA2C2FC),
                                startAngle = -90f,
                                sweepAngle = animatedSweep,"""

content = content.replace(old_sweep, new_sweep)

# Fix 2: Category expense chips (pill style)
old_category_row = r"""                            Row\(
                                modifier = Modifier\.fillMaxWidth\(\),
                                horizontalArrangement = Arrangement\.SpaceBetween,
                                verticalAlignment = Alignment\.CenterVertically
                            \) \{
                                Row\(
                                    verticalAlignment = Alignment\.CenterVertically,
                                    modifier = Modifier\.weight\(1f\)
                                \) \{
                                    Box\(
                                        modifier = Modifier
                                            \.size\(10\.dp\)
                                            \.clip\(CircleShape\)
                                            \.background\(getCategoryColor\(index\)\)
                                    \)
                                    Spacer\(modifier = Modifier\.width\(8\.dp\)\)
                                    Text\(
                                        text = exp\.categoryName,
                                        style = MaterialTheme\.typography\.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow\.Ellipsis
                                    \)
                                \}
                                Row\(
                                    verticalAlignment = Alignment\.CenterVertically,
                                    horizontalArrangement = Arrangement\.End
                                \) \{
                                    Text\(
                                        text = formatTaka\(exp\.amount\),
                                        style = MaterialTheme\.typography\.bodyMedium,
                                        fontWeight = FontWeight\.Bold
                                    \)
                                    Spacer\(modifier = Modifier\.width\(8\.dp\)\)
                                    Text\(
                                        text = "\$\{percent\.toInt\(\)\}%\",
                                        style = MaterialTheme\.typography\.bodySmall,
                                        color = MaterialTheme\.colorScheme\.onSurface\.copy\(alpha = 0\.5f\)
                                    \)
                                \}
                            \}"""

new_category_row = """                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(getCategoryColor(index).copy(alpha = 0.13f))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = exp.categoryName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = getCategoryColor(index),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = formatTaka(exp.amount),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${percent.toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                    )
                                }
                            }"""

content = re.sub(old_category_row, new_category_row, content, flags=re.MULTILINE)

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.write(content)
