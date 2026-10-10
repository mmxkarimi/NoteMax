package ir.mmxkarimi.notemax.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.ui.viewmodel.ViewMode

@Composable
fun UnifiedFloatingMenuBar(
  viewMode: ViewMode,
  onToggleViewMode: () -> Unit,
  onSearchClick: () -> Unit,
  onCreateNote: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
    contentAlignment = Alignment.BottomCenter
  ) {
    // Streamlined Floating Pill Bar (ViewMode + Search + Add)
    Surface(
      shape = RoundedCornerShape(32.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 8.dp,
      shadowElevation = 14.dp,
      border = BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      ),
      modifier = Modifier
        .wrapContentWidth()
        .height(60.dp)
        .shadow(
          elevation = 16.dp,
          shape = RoundedCornerShape(32.dp),
          spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        )
        .testTag("unified_floating_menu_bar")
    ) {
      Row(
        modifier = Modifier
          .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // View Mode Toggle (Grid <-> List)
        IconButton(
          onClick = onToggleViewMode,
          modifier = Modifier
            .size(42.dp)
            .testTag("toggle_view_mode_button")
        ) {
          Icon(
            imageVector = if (viewMode == ViewMode.GRID) Icons.Default.ViewAgenda else Icons.Default.GridView,
            contentDescription = stringResource(if (viewMode == ViewMode.GRID) R.string.view_mode_list else R.string.view_mode_grid),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(21.dp)
          )
        }

        // Search Button
        IconButton(
          onClick = onSearchClick,
          modifier = Modifier
            .size(42.dp)
            .testTag("search_button")
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(R.string.search_notes),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(21.dp)
          )
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Primary Quick Action (+) directly opens Note Editor
        FloatingActionButton(
          onClick = onCreateNote,
          shape = CircleShape,
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp, pressedElevation = 6.dp),
          modifier = Modifier
            .size(46.dp)
            .testTag("floating_menu_primary_fab")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.action_add),
            modifier = Modifier.size(24.dp)
          )
        }
      }
    }
  }
}
