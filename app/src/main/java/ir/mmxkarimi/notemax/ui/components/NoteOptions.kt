package ir.mmxkarimi.notemax.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.ui.theme.Dimens
import ir.mmxkarimi.notemax.ui.theme.NoteColors
import ir.mmxkarimi.notemax.ui.theme.SheetShape
import ir.mmxkarimi.notemax.ui.theme.isAppDark

/** Row of colour swatches. Index 0 = no colour. */
@Composable
fun ColorPickerRow(
  selected: Long,
  onSelect: (Long) -> Unit,
  modifier: Modifier = Modifier
) {
  val dark = isAppDark()
  Row(
    modifier = modifier.horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // "None" swatch
    Swatch(
      color = MaterialTheme.colorScheme.surfaceContainer,
      selected = selected == 0L,
      onClick = { onSelect(0L) },
      contentDescription = stringResource(R.string.color_none)
    ) {
      Icon(
        Icons.Default.FormatColorReset,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
      )
    }
    for (i in 1..NoteColors.count) {
      val c = NoteColors.resolve(i.toLong(), dark) ?: continue
      Swatch(color = c, selected = selected == i.toLong(), onClick = { onSelect(i.toLong()) })
    }
  }
}

@Composable
private fun Swatch(
  color: androidx.compose.ui.graphics.Color,
  selected: Boolean,
  onClick: () -> Unit,
  contentDescription: String? = null,
  content: @Composable () -> Unit = {}
) {
  Box(
    modifier = Modifier
      .size(Dimens.touchTarget)
      .clip(CircleShape)
      .background(color)
      .border(
        BorderStroke(
          if (selected) 2.dp else 1.dp,
          if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        CircleShape
      )
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    if (selected && contentDescription == null) {
      Icon(
        Icons.Default.Check,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(20.dp)
      )
    } else {
      content()
    }
  }
}

/** Editable tag list with suggestions from existing tags. */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TagEditor(
  tags: List<String>,
  suggestions: List<String>,
  onTagsChange: (List<String>) -> Unit,
  modifier: Modifier = Modifier
) {
  var input by remember { mutableStateOf("") }
  fun commit() {
    val clean = input.trim().trimStart('#').lowercase().replace(",", "")
    if (clean.isNotEmpty() && clean !in tags) onTagsChange(tags + clean)
    input = ""
  }
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
    OutlinedTextField(
      value = input,
      onValueChange = { v ->
        if (v.endsWith(",") || v.endsWith(" ") && v.isNotBlank()) {
          input = v.dropLast(1); commit()
        } else input = v
      },
      singleLine = true,
      placeholder = { Text(stringResource(R.string.tags_placeholder)) },
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
      keyboardActions = KeyboardActions(onDone = { commit() }),
      shape = MaterialTheme.shapes.small,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("tag_input")
    )
    if (tags.isNotEmpty()) {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.xs),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs)
      ) {
        tags.forEach { tag ->
          InputChip(
            selected = true,
            onClick = { onTagsChange(tags - tag) },
            label = { Text("#$tag") },
            trailingIcon = {
              Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_remove_tag), modifier = Modifier.size(16.dp))
            }
          )
        }
      }
    }
    val unused = suggestions.filter { it !in tags }.take(12)
    if (unused.isNotEmpty()) {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.xs),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs)
      ) {
        unused.forEach { tag ->
          AssistChip(onClick = { onTagsChange(tags + tag) }, label = { Text("#$tag") })
        }
      }
    }
  }
}

/** Bottom sheet with note colour and tags (a short interruption, hence a sheet and not a screen). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteOptionsSheet(
  colorIndex: Long,
  tags: List<String>,
  tagSuggestions: List<String>,
  onColorChange: (Long) -> Unit,
  onTagsChange: (List<String>) -> Unit,
  onDismiss: () -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape = SheetShape,
    modifier = Modifier.testTag("note_options_sheet")
  ) {
    Column(
      modifier = Modifier
        .navigationBarsPadding()
        .padding(horizontal = Dimens.lg)
        .padding(bottom = Dimens.lg),
      verticalArrangement = Arrangement.spacedBy(Dimens.md)
    ) {
      Text(stringResource(R.string.options_color), style = MaterialTheme.typography.titleSmall)
      ColorPickerRow(selected = colorIndex, onSelect = onColorChange)
      Text(stringResource(R.string.options_tags), style = MaterialTheme.typography.titleSmall)
      TagEditor(tags = tags, suggestions = tagSuggestions, onTagsChange = onTagsChange)
    }
  }
}

/** Sheet used by Home multi-select to colour several notes at once. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSheet(
  selected: Long,
  onSelect: (Long) -> Unit,
  onDismiss: () -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    shape = SheetShape,
    modifier = Modifier.testTag("color_sheet")
  ) {
    Column(
      modifier = Modifier
        .navigationBarsPadding()
        .padding(horizontal = Dimens.lg)
        .padding(bottom = Dimens.lg),
      verticalArrangement = Arrangement.spacedBy(Dimens.md)
    ) {
      Text(stringResource(R.string.options_color), style = MaterialTheme.typography.titleSmall)
      ColorPickerRow(selected = selected, onSelect = onSelect)
    }
  }
}
