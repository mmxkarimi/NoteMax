package ir.mmxkarimi.notemax.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.ui.theme.Dimens
import ir.mmxkarimi.notemax.ui.theme.Pill

/**
 * Bottom dock of the Home screen: a search pill that opens the Search screen and the primary
 * "new note" action. The accent (lime) is spent only on the FAB.
 */
@Composable
fun UnifiedFloatingMenuBar(
  onSearchClick: () -> Unit,
  onCreateNote: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = Dimens.md, vertical = Dimens.sm)
      .testTag("unified_floating_menu_bar"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
  ) {
    Surface(
      onClick = onSearchClick,
      shape = Pill,
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
      modifier = Modifier
        .weight(1f)
        .height(56.dp)
        .testTag("search_button")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = Dimens.md),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(Dimens.sm))
        Text(
          text = stringResource(R.string.search_notes),
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    FloatingActionButton(
      onClick = onCreateNote,
      shape = Pill,
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
      modifier = Modifier
        .size(56.dp)
        .testTag("floating_menu_primary_fab")
    ) {
      Icon(
        imageVector = Icons.Default.Add,
        contentDescription = stringResource(R.string.action_new_note),
        modifier = Modifier.size(26.dp)
      )
    }
  }
}
