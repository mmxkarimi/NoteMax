package ir.mmxkarimi.notemax.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.mmxkarimi.notemax.ui.theme.Dimens

/** Composed empty state: icon medallion, what this place is, and (optionally) how to fill it. */
@Composable
fun EmptyState(
  icon: ImageVector,
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = Dimens.xl),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
      modifier = Modifier.size(72.dp)
    )
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = Dimens.md)
    )
    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = Dimens.xs)
    )
    if (actionLabel != null && onAction != null) {
      FilledTonalButton(
        onClick = onAction,
        modifier = Modifier.padding(top = Dimens.lg)
      ) {
        Text(actionLabel)
      }
    }
  }
}
