import re

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if "Canvas(modifier = Modifier.fillMaxSize()) {" in line:
        # Before this line, insert the animation state
        new_lines.append('                    val rawSweep = (percentSpentVal * 360f).toFloat().coerceIn(0f, 360f)\n')
        new_lines.append('                    val animatedSweep by animateFloatAsState(\n')
        new_lines.append('                        targetValue = rawSweep,\n')
        new_lines.append('                        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),\n')
        new_lines.append('                        label = "donut_sweep"\n')
        new_lines.append('                    )\n')
        new_lines.append(line)
    elif "val rawSweep = (percentSpentVal * 360f).toFloat().coerceIn(0f, 360f)" in line and "Canvas(" not in "".join(lines[max(0, i-10):i]):
        # Skip this line if it's inside Canvas
        pass
    elif "val animatedSweep by animateFloatAsState(" in line and "Canvas(" not in "".join(lines[max(0, i-10):i]):
        pass
    elif "targetValue = rawSweep," in line and "Canvas(" not in "".join(lines[max(0, i-10):i]):
        pass
    elif "animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)," in line and "Canvas(" not in "".join(lines[max(0, i-10):i]):
        pass
    elif 'label = "donut_sweep"' in line and "Canvas(" not in "".join(lines[max(0, i-10):i]):
        pass
    elif line.strip() == ")" and "label = \"donut_sweep\"" in lines[i-1]:
        pass
    else:
        new_lines.append(line)

# Let's do it much simpler:
content = "".join(lines)
old_canvas = "                        Canvas(modifier = Modifier.fillMaxSize()) {\n                            val stroke = 12.dp.toPx()\n                            // Leftover / Remaining backing circle (Lite Green)\n                            drawCircle(\n                                color = Color(0xFFBBECC4),\n                                style = Stroke(stroke)\n                            )\n                            val rawSweep = (percentSpentVal * 360f).toFloat().coerceIn(0f, 360f)\n                            val animatedSweep by animateFloatAsState(\n                                targetValue = rawSweep,\n                                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),\n                                label = \"donut_sweep\"\n                            )"

new_canvas = "                    val rawSweep = (percentSpentVal * 360f).toFloat().coerceIn(0f, 360f)\n                    val animatedSweep by animateFloatAsState(\n                        targetValue = rawSweep,\n                        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),\n                        label = \"donut_sweep\"\n                    )\n                        Canvas(modifier = Modifier.fillMaxSize()) {\n                            val stroke = 12.dp.toPx()\n                            // Leftover / Remaining backing circle (Lite Green)\n                            drawCircle(\n                                color = Color(0xFFBBECC4),\n                                style = Stroke(stroke)\n                            )"
if old_canvas in content:
    content = content.replace(old_canvas, new_canvas)
else:
    print("Failed to match exact string.")

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.write(content)
