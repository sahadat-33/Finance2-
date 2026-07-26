import re

file_path = "app/src/main/java/com/example/ui/TimelineScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    "@androidx.compose.foundation.OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)\nfun TimelineRowItem",
    "@kotlin.OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)\nfun TimelineRowItem"
)

with open(file_path, "w") as f:
    f.write(content)
