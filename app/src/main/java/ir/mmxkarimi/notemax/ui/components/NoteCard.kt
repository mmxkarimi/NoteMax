package ir.mmxkarimi.notemax.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.data.model.Note

@Composable
fun NoteCard(
  note: Note,
  onClick: () -> Unit,
  onTogglePin: () -> Unit,
  onRemove: () -> Unit,
  onRestore: () -> Unit,
  onDeletePermanently: () -> Unit,
  onToggleChecklistItem: ((Int) -> Unit)? = null,
  onUnlockRequest: () -> Unit = {},
  isDragging: Boolean = false,
  dragHandleModifier: Modifier = Modifier,
  modifier: Modifier = Modifier
) {
  var menuExpanded by remember { mutableStateOf(false) }
  var showDeleteConfirmDialog by remember { mutableStateOf(false) }
  var isPermanentDelete by remember { mutableStateOf(false) }

  if (showDeleteConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirmDialog = false },
      title = {
        Text(
          text = stringResource(R.string.dialog_delete_title),
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(text = stringResource(R.string.dialog_delete_message))
      },
      confirmButton = {
        TextButton(
          onClick = {
            showDeleteConfirmDialog = false
            if (isPermanentDelete) {
              onDeletePermanently()
            } else {
              onRemove()
            }
          },
          modifier = Modifier.testTag("confirm_delete_button")
        ) {
          Text(
            text = stringResource(R.string.action_delete),
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
          )
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showDeleteConfirmDialog = false },
          modifier = Modifier.testTag("cancel_delete_button")
        ) {
          Text(text = stringResource(R.string.action_cancel))
        }
      },
      modifier = Modifier.testTag("confirm_delete_dialog")
    )
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
      .clip(RoundedCornerShape(20.dp))
      .clickable { onClick() }
      .testTag("note_card_${note.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isDragging) MaterialTheme.colorScheme.surfaceContainerHigh
      else MaterialTheme.colorScheme.surfaceContainer
    ),
    border = BorderStroke(
      width = if (isDragging) 2.dp else if (note.isPinned) 1.5.dp else 1.dp,
      color = if (isDragging) MaterialTheme.colorScheme.primary
      else if (note.isPinned) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
      else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = if (isDragging) 12.dp else if (note.isPinned) 4.dp else 1.dp
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        .padding(16.dp)
    ) {
      // Top Row: Pin and Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left: Date
        Text(
          text = note.formattedDateShort,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        // Actions: Drag Handle, Pin, and Overflow
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (!note.isTrashed) {
            // Drag indicator handle
            Icon(
              imageVector = Icons.Default.DragIndicator,
              contentDescription = stringResource(R.string.drag_to_reorder),
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDragging) 0.9f else 0.45f),
              modifier = Modifier
                .size(32.dp)
                .then(dragHandleModifier)
                .padding(4.dp)
                .testTag("drag_handle_${note.id}")
            )

            IconButton(
              onClick = onTogglePin,
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                contentDescription = if (note.isPinned) stringResource(R.string.action_unpin) else stringResource(R.string.action_pin),
                tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Box {
            IconButton(
              onClick = { menuExpanded = true },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.action_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }

            DropdownMenu(
              expanded = menuExpanded,
              onDismissRequest = { menuExpanded = false }
            ) {
              if (note.isTrashed) {
                DropdownMenuItem(
                  text = { Text(stringResource(R.string.action_restore)) },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.Restore,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp)
                    )
                  },
                  onClick = {
                    onRestore()
                    menuExpanded = false
                  }
                )
                DropdownMenuItem(
                  text = {
                    Text(
                      stringResource(R.string.action_delete_permanently),
                      color = MaterialTheme.colorScheme.error
                    )
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.DeleteForever,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(18.dp)
                    )
                  },
                  onClick = {
                    menuExpanded = false
                    isPermanentDelete = true
                    showDeleteConfirmDialog = true
                  }
                )
              } else {
                DropdownMenuItem(
                  text = {
                    Text(if (note.isPinned) stringResource(R.string.action_unpin) else stringResource(R.string.action_pin))
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp)
                    )
                  },
                  onClick = {
                    onTogglePin()
                    menuExpanded = false
                  }
                )
                DropdownMenuItem(
                  text = {
                    Text(
                      stringResource(R.string.action_remove),
                      color = MaterialTheme.colorScheme.error
                    )
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(18.dp)
                    )
                  },
                  onClick = {
                    menuExpanded = false
                    isPermanentDelete = false
                    showDeleteConfirmDialog = true
                  }
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Title
      if (note.title.isNotBlank()) {
        Text(
          text = note.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textDirection = TextDirection.ContentOrLtr
          ),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
      }

      // Checklist preview or markdown/text preview
      val parsedChecklist = Note.parseChecklist(note.content)
      if (parsedChecklist.isNotEmpty()) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          parsedChecklist.take(4).forEachIndexed { idx, item ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .clickable { onToggleChecklistItem?.invoke(idx) }
                .padding(vertical = 1.dp)
            ) {
              Icon(
                imageVector = if (item.isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                contentDescription = null,
                tint = if (item.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontSize = 13.sp,
                  textDirection = TextDirection.ContentOrLtr,
                  textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          if (parsedChecklist.size > 4) {
            Text(
              text = stringResource(R.string.more_items_count, parsedChecklist.size - 4),
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(start = 24.dp, top = 2.dp)
            )
          }
        }
      } else if (note.content.isNotBlank()) {
        Text(
          text = note.content,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textDirection = TextDirection.ContentOrLtr
          ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 5,
          overflow = TextOverflow.Ellipsis
        )
      }

      // Checklist progress indicator if markdown checklist exists
      note.checklistProgress?.let { (completed, total) ->
        if (total > 0) {
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            LinearProgressIndicator(
              progress = { completed.toFloat() / total.toFloat() },
              modifier = Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "$completed/$total",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}
