import re

with open('app/src/main/java/com/example/ui/UpdateScreen.kt', 'r') as f:
    content = f.read()

# Add Image import
if 'import androidx.compose.foundation.Image' not in content:
    content = content.replace('import androidx.compose.foundation.clickable', 'import androidx.compose.foundation.Image\nimport androidx.compose.foundation.clickable')
if 'import androidx.compose.ui.res.painterResource' not in content:
    content = content.replace('import androidx.compose.ui.unit.dp', 'import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.res.painterResource\nimport com.example.R')

# Update function signature
content = content.replace('fun UpdateScreen(onBack: () -> Unit) {', 'fun UpdateScreen(onBack: () -> Unit, onNavigateToAbout: () -> Unit) {')

# Adjust layout for title/image
old_top_layout = """            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Finance Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))"""

new_top_layout = """            Spacer(modifier = Modifier.height(16.dp))
            
            Image(
                painter = painterResource(id = R.drawable.icon_image_1780221523424),
                contentDescription = "App Icon",
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Finance Tracker",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(56.dp))"""

content = content.replace(old_top_layout, new_top_layout)

# Remove Policies and Guidelines, replace with Credits
old_bottom_layout = """            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Policies & Guidelines",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "|",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Credits",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "©2026 Finance-tracker",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))"""

new_bottom_layout = """            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Credits",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigateToAbout() }.padding(8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "©2026 Finance-tracker",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))"""

content = content.replace(old_bottom_layout, new_bottom_layout)

with open('app/src/main/java/com/example/ui/UpdateScreen.kt', 'w') as f:
    f.write(content)
