package ir.mmxkarimi.notemax.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun NoteSkeletonLoading(
  modifier: Modifier = Modifier
) {
  val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
  val translateAnim by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1200f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1100, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "shimmer_translation"
  )

  val shimmerColors = listOf(
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
  )

  val brush = Brush.linearGradient(
    colors = shimmerColors,
    start = Offset(translateAnim - 400f, translateAnim - 400f),
    end = Offset(translateAnim, translateAnim)
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("notes_skeleton_loading"),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 3 skeleton cards with natural variations in line count and width
    SkeletonCard(brush = brush, titleFraction = 0.65f, contentLines = listOf(0.95f, 0.8f, 0.5f))
    SkeletonCard(brush = brush, titleFraction = 0.85f, contentLines = listOf(0.9f, 0.65f))
    SkeletonCard(brush = brush, titleFraction = 0.50f, contentLines = listOf(0.85f, 0.75f, 0.4f))
  }
}

@Composable
private fun SkeletonCard(
  brush: Brush,
  titleFraction: Float,
  contentLines: List<Float>
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp)),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    border = BorderStroke(
      width = 1.dp,
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Header: Date pill and action icon placeholder
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .width(68.dp)
            .height(11.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(brush)
        )
        Box(
          modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(brush)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Title placeholder
      Box(
        modifier = Modifier
          .fillMaxWidth(titleFraction)
          .height(18.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(brush)
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Content lines placeholder
      contentLines.forEach { fraction ->
        Box(
          modifier = Modifier
            .fillMaxWidth(fraction)
            .height(13.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(brush)
        )
        Spacer(modifier = Modifier.height(6.dp))
      }
    }
  }
}
