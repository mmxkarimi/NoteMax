package ir.mmxkarimi.notemax.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.data.model.Note
import ir.mmxkarimi.notemax.ui.components.EmptyState
import ir.mmxkarimi.notemax.ui.components.NoteCard
import ir.mmxkarimi.notemax.ui.theme.Dimens
import ir.mmxkarimi.notemax.ui.theme.Pill

/** Full-screen search: a real destination (back works, state survives rotation). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
  notes: List<Note>,
  allTags: List<String>,
  onBack: () -> Unit,
  onNoteClick: (Note) -> Unit,
  modifier: Modifier = Modifier
) {
  var query by rememberSaveable { mutableStateOf("") }
  var tagFilter by rememberSaveable { mutableStateOf<String?>(null) }
  val focusRequester = remember { FocusRequester() }
  val keyboard = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  val trimmed = query.trim()
  val results = remember(notes, trimmed, tagFilter) {
    notes.filter { n ->
      (tagFilter == null || tagFilter in n.tags) &&
        (trimmed.isEmpty() || n.title.contains(trimmed, ignoreCase = true) ||
          n.content.contains(trimmed, ignoreCase = true) ||
          n.tags.any { it.contains(trimmed.trimStart('#'), ignoreCase = true) })
    }
  }
  val showRecent = trimmed.isEmpty() && tagFilter == null
  val shown = if (showRecent) notes.sortedByDescending { it.updatedAt }.take(5) else results

  // Surface supplies the content colour, so the back arrow is light in dark mode (not default black).
  Surface(
    modifier = modifier
      .fillMaxSize()
      .testTag("search_screen"),
    color = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onBackground
  ) {
  Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = Dimens.xs, vertical = Dimens.xs),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = { keyboard?.hide(); onBack() }, modifier = Modifier.testTag("search_back_button")) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
      }
      TextField(
        value = query,
        onValueChange = { query = it },
        singleLine = true,
        placeholder = { Text(stringResource(R.string.search_notes_placeholder)) },
        trailingIcon = {
          if (query.isNotEmpty()) {
            IconButton(onClick = { query = "" }) {
              Icon(Icons.Default.Close, contentDescription = stringResource(R.string.search_clear))
            }
          }
        },
        shape = Pill,
        colors = TextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
          focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
          unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        modifier = Modifier
          .weight(1f)
          .padding(end = Dimens.sm)
          .focusRequester(focusRequester)
          .testTag("search_text_input")
      )
    }

    if (allTags.isNotEmpty()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = Dimens.md, vertical = Dimens.xs),
        horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
      ) {
        allTags.forEach { tag ->
          FilterChip(
            selected = tagFilter == tag,
            onClick = { tagFilter = if (tagFilter == tag) null else tag },
            label = { Text("#$tag") }
          )
        }
      }
    }

    if (shown.isEmpty()) {
      EmptyState(
        icon = Icons.Default.SearchOff,
        title = stringResource(R.string.no_search_results),
        description = stringResource(R.string.no_search_results_description),
        modifier = Modifier.testTag("empty_search_results")
      )
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .imePadding(),
        contentPadding = PaddingValues(start = Dimens.md, end = Dimens.md, top = Dimens.xs, bottom = Dimens.lg),
        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
      ) {
        item(key = "header") {
          Text(
            text = if (showRecent) stringResource(R.string.search_recent_notes, shown.size)
            else stringResource(R.string.search_results_count, shown.size),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = Dimens.xxs)
          )
        }
        items(shown, key = { it.id }) { note ->
          NoteCard(
            note = note,
            onClick = { keyboard?.hide(); onNoteClick(note) },
            maxPreviewLines = 2,
            modifier = Modifier.testTag("search_result_item_${note.id}")
          )
        }
        item(key = "nav_space") { androidx.compose.foundation.layout.Spacer(Modifier.navigationBarsPadding()) }
      }
    }
  }
  }
}

