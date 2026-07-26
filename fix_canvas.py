import re

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    content = f.read()

old_canvas = """                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .padding(4.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 12.dp.toPx()
                            // Leftover / Remaining backing circle (Lite Green)
                            drawCircle(
                                color = Color(0xFFBBECC4),
                                style = Stroke(stroke)
                            )
                            val rawSweep = (percentSpentVal * 360f).toFloat().coerceIn(0f, 360f)
                            val animatedSweep by animateFloatAsState(
                                targetValue = rawSweep,
                                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                                label = "donut_sweep"
                            )
                            // Spent foreground arc (Lite Blue)"""

new_canvas = """                    val rawSweep = (percentSpentVal * 360f).toFloat().coerceIn(0f, 360f)
                    val animatedSweep by animateFloatAsState(
                        targetValue = rawSweep,
                        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                        label = "donut_sweep"
                    )

                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .padding(4.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 12.dp.toPx()
                            // Leftover / Remaining backing circle (Lite Green)
                            drawCircle(
                                color = Color(0xFFBBECC4),
                                style = Stroke(stroke)
                            )
                            // Spent foreground arc (Lite Blue)"""

content = content.replace(old_canvas, new_canvas)

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.write(content)
