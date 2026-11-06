package ir.mmxkarimi.notemax.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.data.model.Note
import ir.mmxkarimi.notemax.ui.components.ConfirmDialog
import ir.mmxkarimi.notemax.ui.components.EmptyState
import ir.mmxkarimi.notemax.ui.components.NoteCard
import ir.mmxkarimi.notemax.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
  notes: List<Note>,
  retentionDays: Int,
  onBack: () -> Unit,
  onRestore: (Note) -> Unit,
  onDeleteForever: (Note) -> Unit,
  onEmptyTrash: () -> Unit,
  modifier: Modifier = Modifier
) {
  var pendingDelete by remember { mutableStateOf<Note?>(null) }
  var confirmEmpty by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("trash_screen"),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.nav_trash), style = MaterialTheme.typography.titleLarge) },
        navigationIcon = { BackButton(onBack) },
        actions = {
          if (notes.isNotEmpty()) {
            IconButton(onClick = { confirmEmpty = true }, modifier = Modifier.testTag("empty_trash_button")) {
              Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.action_empty_trash))
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
      )
    }
  ) { inner ->
    if (notes.isEmpty()) {
      EmptyState(
        icon = Icons.Default.Delete,
        title = stringResource(R.string.trash_empty_title),
        description = stringResource(R.string.trash_empty_description),
        modifier = Modifier.padding(inner)
      )
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize().padding(inner),
        contentPadding = PaddingValues(start = Dimens.md, end = Dimens.md, top = Dimens.xs, bottom = Dimens.lg),
        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
      ) {
        if (retentionDays > 0) {
          item(key = "retention") {
            Text(
              text = pluralStringResource(R.plurals.trash_retention_info, retentionDays, retentionDays),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(bottom = Dimens.xxs)
            )
          }
        }
        items(notes, key = { it.id }) { note ->
          // Trashed notes are read-only here: restore first, then edit.
          NoteCard(note = note, onClick = {}, maxPreviewLines = 3) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              TextButton(onClick = { onRestore(note) }, modifier = Modifier.testTag("restore_${note.id}")) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.padding(end = Dimens.xs))
                Text(stringResource(R.string.action_restore))
              }
              TextButton(onClick = { pendingDelete = note }, modifier = Modifier.testTag("delete_forever_${note.id}")) {
                Icon(
                  Icons.Default.DeleteForever, contentDescription = null,
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.padding(end = Dimens.xs)
                )
                Text(stringResource(R.string.action_delete_forever), color = MaterialTheme.colorScheme.error)
              }
            }
          }
        }
      }
    }
  }

  pendingDelete?.let { note ->
    ConfirmDialog(
      title = stringResource(R.string.dialog_delete_title),
      message = stringResource(R.string.dialog_delete_message),
      confirmLabel = stringResource(R.string.action_delete_forever),
      onConfirm = { onDeleteForever(note); pendingDelete = null },
      onDismiss = { pendingDelete = null }
    )
  }
  if (confirmEmpty) {
    ConfirmDialog(
      title = stringResource(R.string.dialog_empty_trash_title),
      message = stringResource(R.string.dialog_empty_trash_message),
      confirmLabel = stringResource(R.string.action_empty_trash),
      onConfirm = { onEmptyTrash(); confirmEmpty = false },
      onDismiss = { confirmEmpty = false }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
  notes: List<Note>,
  onBack: () -> Unit,
  onNoteClick: (Note) -> Unit,
  onUnarchive: (Note) -> Unit,
  onToggleChecklistItem: (Note, Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    modifier = modifier.fillMaxSize().testTag("archive_screen"),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.nav_archive), style = MaterialTheme.typography.titleLarge) },
        navigationIcon = { BackButton(onBack) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
      )
    }
  ) { inner ->
    if (notes.isEmpty()) {
      EmptyState(
        icon = Icons.Default.Archive,
        title = stringResource(R.string.archive_empty_title),
        description = stringResource(R.string.archive_empty_description),
        modifier = Modifier.padding(inner)
      )
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize().padding(inner),
        contentPadding = PaddingValues(start = Dimens.md, end = Dimens.md, top = Dimens.xs, bottom = Dimens.lg),
        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
      ) {
        items(notes, key = { it.id }) { note ->
          NoteCard(
            note = note,
            onClick = { onNoteClick(note) },
            onToggleChecklistItem = { onToggleChecklistItem(note, it) },
            maxPreviewLines = 3
          ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
              TextButton(onClick = { onUnarchive(note) }, modifier = Modifier.testTag("unarchive_${note.id}")) {
                Icon(Icons.Default.Unarchive, contentDescription = null, modifier = Modifier.padding(end = Dimens.xs))
                Text(stringResource(R.string.action_unarchive))
              }
            }
          }
        }
        item(key = "nav") { androidx.compose.foundation.layout.Spacer(Modifier.navigationBarsPadding()) }
      }
    }
  }
}

@Composable
internal fun BackButton(onBack: () -> Unit) {
  IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
  }
}
