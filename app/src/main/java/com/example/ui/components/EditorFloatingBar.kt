package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

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
    shape = RoundedCornerShape(28.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
    tonalElevation = 6.dp,
    shadowElevation = 8.dp,
    modifier = modifier
      .padding(horizontal = 16.dp, vertical = 12.dp)
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
            painter = painterResource(R.drawable.ic_title),
            contentDescription = stringResource(R.string.tool_heading),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        // Checklist Box
        IconButton(onClick = onInsertChecklist, modifier = Modifier.size(40.dp)) {
          Icon(
            painter = painterResource(R.drawable.ic_checkbox_checked),
            contentDescription = stringResource(R.string.tool_checklist),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
        }

        // Bold
        IconButton(onClick = onInsertBold, modifier = Modifier.size(40.dp)) {
          Icon(
            painter = painterResource(R.drawable.ic_format_bold),
            contentDescription = stringResource(R.string.tool_bold),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        // Italic
        IconButton(onClick = onInsertItalic, modifier = Modifier.size(40.dp)) {
          Icon(
            painter = painterResource(R.drawable.ic_format_italic),
            contentDescription = stringResource(R.string.tool_italic),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        // Strikethrough
        IconButton(onClick = onInsertStrikethrough, modifier = Modifier.size(40.dp)) {
          Icon(
            painter = painterResource(R.drawable.ic_format_strikethrough),
            contentDescription = stringResource(R.string.tool_strikethrough),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        // Bullet List
        IconButton(onClick = onInsertBullet, modifier = Modifier.size(40.dp)) {
          Icon(
            painter = painterResource(R.drawable.ic_format_bullet),
            contentDescription = stringResource(R.string.tool_bullet),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        // Numbered List
        IconButton(onClick = onInsertNumbered, modifier = Modifier.size(40.dp)) {
          Icon(
            painter = painterResource(R.drawable.ic_format_numbered),
            contentDescription = stringResource(R.string.tool_numbered),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        // Code
        IconButton(onClick = onInsertCode, modifier = Modifier.size(40.dp)) {
          Icon(
            painter = painterResource(R.drawable.ic_code),
            contentDescription = stringResource(R.string.tool_code),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        // Quote
        IconButton(onClick = onInsertQuote, modifier = Modifier.size(40.dp)) {
          Icon(
            painter = painterResource(R.drawable.ic_quote),
            contentDescription = stringResource(R.string.tool_quote),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        // Table
        IconButton(onClick = onInsertTable, modifier = Modifier.size(40.dp)) {
          Icon(
            painter = painterResource(R.drawable.ic_table),
            contentDescription = stringResource(R.string.tool_table),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        // Horizontal Rule
        IconButton(onClick = onInsertHorizontalRule, modifier = Modifier.size(40.dp)) {
          Icon(
            painter = painterResource(R.drawable.ic_horizontal_rule),
            contentDescription = stringResource(R.string.tool_horizontal_rule),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        // Link
        IconButton(onClick = onInsertLink, modifier = Modifier.size(40.dp)) {
          Icon(
            painter = painterResource(R.drawable.ic_link),
            contentDescription = stringResource(R.string.tool_link),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        // Timestamp
        IconButton(onClick = onInsertTimestamp, modifier = Modifier.size(40.dp)) {
          Icon(
            painter = painterResource(R.drawable.ic_schedule),
            contentDescription = stringResource(R.string.tool_timestamp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
