package ir.mmxkarimi.notemax.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Title
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

@Composable
fun EditorFloatingBar(
  onInsertHeading: () -> Unit,
  onInsertBold: () -> Unit,
  onInsertItalic: () -> Unit,
  onInsertStrikethrough: () -> Unit,
  onInsertChecklist: () -> Unit,
  onInsertBullet: () -> Unit,
  onInsertNumbered: () -> Unit,
  onInsertCode: () -> Unit,
  onInsertQuote: () -> Unit,
  onInsertTable: () -> Unit,
  onInsertHorizontalRule: () -> Unit,
  onInsertLink: () -> Unit,
  onInsertTimestamp: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(32.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 8.dp,
    shadowElevation = 14.dp,
    border = BorderStroke(
      1.dp,
      MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    ),
    modifier = modifier
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .shadow(
        elevation = 16.dp,
        shape = RoundedCornerShape(32.dp),
        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
      )
      .testTag("editor_floating_bar")
  ) {
    // Scrollable Markdown Formatting Actions
    Row(
      modifier = Modifier
        .padding(horizontal = 8.dp, vertical = 4.dp)
        .horizontalScroll(rememberScrollState()),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      // Heading
      IconButton(onClick = onInsertHeading, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = Icons.Default.Title,
          contentDescription = stringResource(R.string.tool_heading),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      // Checklist Box
      IconButton(onClick = onInsertChecklist, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = Icons.Default.CheckBox,
          contentDescription = stringResource(R.string.tool_checklist),
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
      }

      // Bold
      IconButton(onClick = onInsertBold, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = Icons.Default.FormatBold,
          contentDescription = stringResource(R.string.tool_bold),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      // Italic
      IconButton(onClick = onInsertItalic, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = Icons.Default.FormatItalic,
          contentDescription = stringResource(R.string.tool_italic),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      // Strikethrough
      IconButton(onClick = onInsertStrikethrough, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = Icons.Default.FormatStrikethrough,
          contentDescription = stringResource(R.string.tool_strikethrough),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      // Bullet List
      IconButton(onClick = onInsertBullet, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
          contentDescription = stringResource(R.string.tool_bullet),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      // Numbered List
      IconButton(onClick = onInsertNumbered, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = Icons.Default.FormatListNumbered,
          contentDescription = stringResource(R.string.tool_numbered),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      // Code
      IconButton(onClick = onInsertCode, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = Icons.Default.Code,
          contentDescription = stringResource(R.string.tool_code),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      // Quote
      IconButton(onClick = onInsertQuote, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = Icons.Default.FormatQuote,
          contentDescription = stringResource(R.string.tool_quote),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      // Table
      IconButton(onClick = onInsertTable, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = Icons.Default.TableChart,
          contentDescription = stringResource(R.string.tool_table),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      // Horizontal Rule
      IconButton(onClick = onInsertHorizontalRule, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = Icons.Default.HorizontalRule,
          contentDescription = stringResource(R.string.tool_horizontal_rule),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      // Link
      IconButton(onClick = onInsertLink, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = Icons.Default.Link,
          contentDescription = stringResource(R.string.tool_link),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      // Timestamp
      IconButton(onClick = onInsertTimestamp, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = Icons.Default.Schedule,
          contentDescription = stringResource(R.string.tool_timestamp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
