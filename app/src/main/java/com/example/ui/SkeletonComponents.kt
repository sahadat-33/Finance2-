package com.example.ui
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val colors = if (isDark)
        listOf(Color(0xFF1E2535), Color(0xFF2A3347), Color(0xFF1E2535))
    else
        listOf(Color(0xFFE4EAF2), Color(0xFFF2F6FA), Color(0xFFE4EAF2))
    return Brush.linearGradient(
        colors = colors,
        start = Offset(offset, 0f),
        end = Offset(offset + 300f, 0f)
    )
}
@Composable
fun SkeletonBox(modifier: Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(shimmerBrush()))
}
@Composable
fun DashboardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(20.dp)))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SkeletonBox(modifier = Modifier.weight(1f).height(80.dp))
            SkeletonBox(modifier = Modifier.weight(1f).height(80.dp))
        }
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(80.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(130.dp))
    }
}
