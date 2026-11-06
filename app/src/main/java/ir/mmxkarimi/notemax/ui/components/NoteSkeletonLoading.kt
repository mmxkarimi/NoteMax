package ir.mmxkarimi.notemax.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.mmxkarimi.notemax.ui.theme.CardShape
import ir.mmxkarimi.notemax.ui.theme.Dimens
import ir.mmxkarimi.notemax.ui.theme.Pill

/** Skeleton that mirrors the final two-column card layout so nothing jumps when data arrives. */
@Composable
fun NoteSkeletonLoading(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(Dimens.md)
) {
  val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
  val shift by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1200f,
    animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
    label = "shimmer_translation"
  )
  val base = MaterialTheme.colorScheme.surfaceContainerHigh
  val highlight = MaterialTheme.colorScheme.surfaceContainerHighest
  val brush = Brush.linearGradient(
    colors = listOf(base, highlight, base),
    start = Offset(shift - 400f, 0f),
    end = Offset(shift, 300f)
  )
  val heights = listOf(140.dp, 190.dp, 170.dp, 120.dp, 200.dp, 150.dp)

  LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    contentPadding = contentPadding,
    horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
    verticalArrangement = Arrangement.spacedBy(Dimens.sm),
    userScrollEnabled = false,
    modifier = modifier.testTag("notes_skeleton_loading")
  ) {
    items(heights) { h -> SkeletonCard(h, brush) }
  }
}

@Composable
private fun SkeletonCard(height: Dp, brush: Brush) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .height(height)
      .clip(CardShape)
      .background(MaterialTheme.colorScheme.surfaceContainer)
      .padding(Dimens.md),
    verticalArrangement = Arrangement.spacedBy(Dimens.xs)
  ) {
    SkeletonBar(0.7f, 16.dp, brush)
    SkeletonBar(1f, 10.dp, brush)
    SkeletonBar(0.9f, 10.dp, brush)
    SkeletonBar(0.5f, 10.dp, brush)
  }
}

@Composable
private fun SkeletonBar(fraction: Float, height: Dp, brush: Brush) {
  Box(
    modifier = Modifier
      .fillMaxWidth(fraction)
      .height(height)
      .clip(Pill)
      .background(brush)
  )
}
