package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Note
import com.example.ui.theme.VaultEncryptedBorderDark
import com.example.ui.theme.VaultEncryptedBorderLight
import com.example.ui.theme.VaultEncryptedColorDark
import com.example.ui.theme.VaultEncryptedColorLight
import com.example.ui.theme.VaultEncryptedContainerDark
import com.example.ui.theme.VaultEncryptedContainerLight

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
  modifier: Modifier = Modifier
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .clickable { onClick() }
      .testTag("note_card_${note.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    border = BorderStroke(
      width = if (note.isPinned) 1.5.dp else 1.dp,
      color = if (note.isPinned) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
      else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (note.isPinned) 4.dp else 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Top Row: Type indicator and Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.weight(1f, fill = false)
        ) {
          if (note.isChecklist) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  painter = painterResource(R.drawable.ic_checklist),
                  contentDescription = stringResource(R.string.action_checklist),
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = stringResource(R.string.action_checklist),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                  ),
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }
          }

          if (note.isEncrypted) {
            val isDark = isSystemInDarkTheme()
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isDark) VaultEncryptedContainerDark else VaultEncryptedContainerLight,
              border = BorderStroke(
                1.dp,
                if (isDark) VaultEncryptedBorderDark else VaultEncryptedBorderLight
              ),
              modifier = Modifier.testTag("note_encrypted_badge_${note.id}")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  painter = painterResource(R.drawable.ic_lock),
                  contentDescription = stringResource(R.string.badge_encrypted),
                  tint = if (isDark) VaultEncryptedColorDark else VaultEncryptedColorLight,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = stringResource(R.string.badge_aes_256),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                  ),
                  color = if (isDark) VaultEncryptedColorDark else VaultEncryptedColorLight
                )
              }
            }
          }
        }

        // Actions: Pin and Overflow
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (!note.isTrashed) {
            IconButton(
              onClick = onTogglePin,
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                painter = painterResource(if (note.isPinned) R.drawable.ic_pin else R.drawable.ic_pin_outlined),
                contentDescription = if (note.isPinned) stringResource(R.string.action_unpin) else stringResource(R.string.action_pin),
                tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(17.dp)
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
                modifier = Modifier.size(17.dp)
              )
            }

            DropdownMenu(
              expanded = menuExpanded,
              onDismissRequest = { menuExpanded = false }
            ) {
              if (note.isTrashed) {
                DropdownMenuItem(
                  text = { Text(stringResource(R.string.action_restore)) },
                  leadingIcon = { Icon(painter = painterResource(R.drawable.ic_restore), contentDescription = null, modifier = Modifier.size(18.dp)) },
                  onClick = {
                    onRestore()
                    menuExpanded = false
                  }
                )
                DropdownMenuItem(
                  text = { Text(stringResource(R.string.action_delete_permanently), color = MaterialTheme.colorScheme.error) },
                  leadingIcon = { Icon(painter = painterResource(R.drawable.ic_delete_forever), contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                  onClick = {
                    onDeletePermanently()
                    menuExpanded = false
                  }
                )
              } else {
                DropdownMenuItem(
                  text = { Text(if (note.isPinned) stringResource(R.string.action_unpin) else stringResource(R.string.action_pin)) },
                  leadingIcon = { Icon(painter = painterResource(if (note.isPinned) R.drawable.ic_pin else R.drawable.ic_pin_outlined), contentDescription = null, modifier = Modifier.size(18.dp)) },
                  onClick = {
                    onTogglePin()
                    menuExpanded = false
                  }
                )
                DropdownMenuItem(
                  text = { Text(stringResource(R.string.action_remove), color = MaterialTheme.colorScheme.error) },
                  leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                  onClick = {
                    onRemove()
                    menuExpanded = false
                  }
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Title supporting BiDi text layout natively
      Text(
        text = note.displayTitle,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          textDirection = TextDirection.ContentOrLtr
        ),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Checklist Mode preview or standard Text preview
      if (note.isChecklist && note.checklistItems.isNotEmpty()) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          note.checklistItems.take(4).forEachIndexed { idx, item ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                  onToggleChecklistItem?.invoke(idx)
                }
                .padding(vertical = 2.dp)
            ) {
              Icon(
                painter = painterResource(if (item.isChecked) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_blank),
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

          if (note.checklistItems.size > 4) {
            Text(
              text = stringResource(R.string.more_items_count, note.checklistItems.size - 4),
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

      // Checklist progress indicator
      note.checklistProgress?.let { (completed, total) ->
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          LinearProgressIndicator(
            progress = { if (total > 0) completed.toFloat() / total.toFloat() else 0f },
            modifier = Modifier
              .weight(1f)
              .height(6.dp)
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

      Spacer(modifier = Modifier.height(10.dp))

      // Footer: Date and Word count
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = note.formattedDateShort,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        val infoText = if (note.isChecklist) {
          val total = note.checklistItems.size
          val done = note.checklistItems.count { it.isChecked }
          stringResource(R.string.checklist_done, done, total)
        } else {
          ""
        }
        if (infoText.isNotEmpty()) {
          Text(
            text = infoText,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
          )
        }
      }
    }
  }
}
