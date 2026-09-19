package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Note
import com.example.ui.components.EditorFloatingBar
import com.example.ui.components.MarkdownRenderer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
  initialNote: Note?,
  onSaveAndClose: (Note) -> Unit,
  onDeleteNote: (Note) -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val contentFocusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  var title by remember { mutableStateOf(initialNote?.title ?: "") }
  var contentTextFieldValue by remember {
    mutableStateOf(TextFieldValue(text = initialNote?.content ?: ""))
  }
  var isPinned by remember { mutableStateOf(initialNote?.isPinned ?: false) }
  var isPreviewMode by remember { mutableStateOf(false) }
  var showDeleteConfirmDialog by remember { mutableStateOf(false) }

  fun saveCurrentNote() {
    val trimmedTitle = title.trim()
    val trimmedContent = contentTextFieldValue.text.trim()

    // If both title and content are blank:
    if (trimmedTitle.isBlank() && trimmedContent.isBlank()) {
      if (initialNote != null && initialNote.id != 0L) {
        // If an existing note was edited to be completely empty, delete/trash it
        onDeleteNote(initialNote)
      } else {
        // For a new note with no content, simply discard and navigate back without saving
        onNavigateBack()
      }
      return
    }

    val noteToSave = (initialNote ?: Note()).copy(
      title = trimmedTitle,
      content = trimmedContent,
      isChecklist = false,
      colorHex = 0L,
      isEncrypted = false,
      isPinned = isPinned,
      updatedAt = System.currentTimeMillis()
    )
    onSaveAndClose(noteToSave)
  }

  // Handle system back navigation
  BackHandler {
    saveCurrentNote()
  }

  // Delete Confirmation Dialog
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
            if (initialNote != null) {
              onDeleteNote(initialNote)
            } else {
              onNavigateBack()
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

  // Helper regex for identifying list items, blockquotes, and headings
  val blockMarkerRegex = Regex("""^(\s*)(-\s*\[[ xX]\]\s*|\*\s*\[[ xX]\]\s*|[-*]\s+|\d+\.\s+|>\s+|#{1,6}\s+)""")

  // Helper functions for formatting selected text or inserting at cursor
  fun applyInlineFormat(prefix: String, suffix: String, defaultPlaceholder: String = "text") {
    val text = contentTextFieldValue.text
    val selection = contentTextFieldValue.selection
    if (selection.min != selection.max) {
      val start = selection.min
      val end = selection.max
      val selected = text.substring(start, end)

      // 1. If the selected text itself starts with prefix and ends with suffix -> strip/untoggle
      if (selected.startsWith(prefix) && selected.endsWith(suffix) && selected.length >= prefix.length + suffix.length) {
        val unstyled = selected.substring(prefix.length, selected.length - suffix.length)
        val newText = text.replaceRange(start, end, unstyled)
        contentTextFieldValue = TextFieldValue(
          text = newText,
          selection = TextRange(start, start + unstyled.length)
        )
        return
      }

      // 2. If the characters immediately outside the selection are prefix and suffix -> strip/untoggle
      if (start >= prefix.length && end + suffix.length <= text.length) {
        val before = text.substring(start - prefix.length, start)
        val after = text.substring(end, end + suffix.length)
        if (before == prefix && after == suffix) {
          val newText = text.substring(0, start - prefix.length) + selected + text.substring(end + suffix.length)
          contentTextFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(start - prefix.length, start - prefix.length + selected.length)
          )
          return
        }
      }

      // 3. Otherwise wrap with prefix and suffix
      val replacement = "$prefix$selected$suffix"
      val newText = text.replaceRange(start, end, replacement)
      contentTextFieldValue = TextFieldValue(
        text = newText,
        selection = TextRange(start + prefix.length, start + prefix.length + selected.length)
      )
    } else {
      val cursorPos = selection.start

      // 1. Check if cursor is directly between prefix and suffix (e.g. **|**)
      if (cursorPos >= prefix.length && cursorPos + suffix.length <= text.length) {
        val before = text.substring(cursorPos - prefix.length, cursorPos)
        val after = text.substring(cursorPos, cursorPos + suffix.length)
        if (before == prefix && after == suffix) {
          val newText = text.substring(0, cursorPos - prefix.length) + text.substring(cursorPos + suffix.length)
          contentTextFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(cursorPos - prefix.length)
          )
          return
        }
      }

      // 2. Check if cursor is inside an existing formatted span without newlines
      val lastPrefix = text.lastIndexOf(prefix, (cursorPos - 1).coerceAtLeast(0))
      val nextSuffix = text.indexOf(suffix, cursorPos)
      if (lastPrefix != -1 && nextSuffix != -1 && nextSuffix > lastPrefix) {
        val inner = text.substring(lastPrefix + prefix.length, nextSuffix)
        if (!inner.contains('\n')) {
          val newText = text.substring(0, lastPrefix) + inner + text.substring(nextSuffix + suffix.length)
          val newCursor = (cursorPos - prefix.length).coerceIn(lastPrefix, lastPrefix + inner.length)
          contentTextFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursor)
          )
          return
        }
      }

      // 3. Otherwise insert placeholder text
      val insertion = "$prefix$defaultPlaceholder$suffix"
      val newText = text.substring(0, cursorPos) + insertion + text.substring(cursorPos)
      contentTextFieldValue = TextFieldValue(
        text = newText,
        selection = TextRange(cursorPos + prefix.length, cursorPos + prefix.length + defaultPlaceholder.length)
      )
    }
  }

  fun applyBlockFormat(prefix: String) {
    val text = contentTextFieldValue.text
    val selection = contentTextFieldValue.selection
    if (selection.min != selection.max) {
      val start = selection.min
      val end = selection.max

      // Expand to full line boundaries
      val lastNewlineBefore = text.lastIndexOf('\n', (start - 1).coerceAtLeast(0))
      val lineStart = if (lastNewlineBefore == -1 || start == 0) 0 else lastNewlineBefore + 1
      val nextNewlineAfter = text.indexOf('\n', end)
      val lineEnd = if (nextNewlineAfter == -1) text.length else nextNewlineAfter

      val fullLinesText = text.substring(lineStart, lineEnd)
      val lines = fullLinesText.lines()
      val nonBlankLines = lines.filter { it.isNotBlank() }

      // Check if all non-blank lines already have this exact style -> if so, remove/untoggle
      val allHaveSamePrefix = nonBlankLines.isNotEmpty() && nonBlankLines.all { line ->
        line.trimStart().startsWith(prefix.trim())
      }

      val formattedLines = if (allHaveSamePrefix) {
        lines.map { line ->
          val indent = line.takeWhile { it.isWhitespace() }
          val trimmed = line.substring(indent.length)
          if (trimmed.startsWith(prefix.trim())) {
            indent + trimmed.removePrefix(prefix.trim()).trimStart()
          } else {
            line
          }
        }
      } else {
        lines.map { line ->
          if (line.isBlank()) {
            line
          } else {
            val match = blockMarkerRegex.find(line)
            if (match != null) {
              val indent = match.groupValues[1]
              val rest = line.substring(match.range.last + 1)
              "$indent$prefix$rest"
            } else {
              val indent = line.takeWhile { it.isWhitespace() }
              val rest = line.substring(indent.length)
              "$indent$prefix$rest"
            }
          }
        }
      }

      val replacement = formattedLines.joinToString("\n")
      val newText = text.substring(0, lineStart) + replacement + text.substring(lineEnd)
      contentTextFieldValue = TextFieldValue(
        text = newText,
        selection = TextRange(lineStart, lineStart + replacement.length)
      )
    } else {
      val cursorPos = selection.start
      val lastNewline = text.lastIndexOf('\n', (cursorPos - 1).coerceAtLeast(0))
      val lineStart = if (lastNewline == -1 || cursorPos == 0) 0 else lastNewline + 1
      val nextNewline = text.indexOf('\n', cursorPos)
      val lineEnd = if (nextNewline == -1) text.length else nextNewline

      val currentLine = text.substring(lineStart, lineEnd)
      val indent = currentLine.takeWhile { it.isWhitespace() }
      val trimmed = currentLine.substring(indent.length)

      val newLine = if (trimmed.startsWith(prefix.trim())) {
        // Toggle off if it already has this format
        indent + trimmed.removePrefix(prefix.trim()).trimStart()
      } else {
        val match = blockMarkerRegex.find(currentLine)
        if (match != null) {
          // Replace existing block marker (e.g. numbered list to bullet or checklist)
          val lineIndent = match.groupValues[1]
          val rest = currentLine.substring(match.range.last + 1)
          "$lineIndent$prefix$rest"
        } else {
          "$indent$prefix$trimmed"
        }
      }

      val newText = text.substring(0, lineStart) + newLine + text.substring(lineEnd)
      val delta = newLine.length - currentLine.length
      val newCursor = (cursorPos + delta).coerceIn(lineStart, lineStart + newLine.length)
      contentTextFieldValue = TextFieldValue(
        text = newText,
        selection = TextRange(newCursor)
      )
    }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .testTag("note_editor_screen"),
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    containerColor = MaterialTheme.colorScheme.background,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (title.isNotBlank()) title else stringResource(R.string.action_note),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            ),
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface
          )
        },
        navigationIcon = {
          IconButton(
            onClick = { saveCurrentNote() },
            modifier = Modifier.testTag("editor_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.action_back),
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        },
        actions = {
          // Preview / Edit Mode Toggle Button
          IconButton(
            onClick = { isPreviewMode = !isPreviewMode },
            modifier = Modifier.testTag("editor_toggle_preview_button")
          ) {
            if (isPreviewMode) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.tool_edit),
                tint = MaterialTheme.colorScheme.primary
              )
            } else {
              Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = stringResource(R.string.tool_preview),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Pin Button
          IconButton(
            onClick = { isPinned = !isPinned },
            modifier = Modifier.testTag("editor_pin_button")
          ) {
            Icon(
              imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
              contentDescription = stringResource(if (isPinned) R.string.action_unpin else R.string.action_pin),
              tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Share Button
          IconButton(
            onClick = {
              val currentContent = contentTextFieldValue.text
              val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(Intent.EXTRA_TEXT, "$title\n\n$currentContent")
                type = "text/plain"
              }
              context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.action_share)))
            },
            modifier = Modifier.testTag("editor_share_button")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = stringResource(R.string.action_share),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Delete Button (shows confirm dialog)
          if (initialNote != null && initialNote.id != 0L) {
            IconButton(
              onClick = { showDeleteConfirmDialog = true },
              modifier = Modifier.testTag("editor_delete_button")
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.action_delete),
                tint = MaterialTheme.colorScheme.error
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = innerPadding.calculateTopPadding())
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .imePadding()
          .verticalScroll(rememberScrollState())
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) {
            if (!isPreviewMode) {
              contentFocusRequester.requestFocus()
              keyboardController?.show()
            }
          }
          .padding(horizontal = 18.dp)
          .padding(bottom = 96.dp)
      ) {
        // Title Input
        BasicTextField(
          value = title,
          onValueChange = { title = it },
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("note_title_input"),
          textStyle = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textDirection = TextDirection.ContentOrLtr
          ),
          cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
          decorationBox = { innerTextField ->
            if (title.isEmpty()) {
              Text(
                text = stringResource(R.string.note_title_placeholder),
                style = TextStyle(
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                  textDirection = TextDirection.ContentOrLtr
                )
              )
            }
            innerTextField()
          }
        )

        HorizontalDivider(
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
          modifier = Modifier.padding(bottom = 12.dp)
        )

        // Main Editor or Markdown Preview
        if (isPreviewMode) {
          // If empty, show exactly nothing as requested
          val currentContent = contentTextFieldValue.text
          if (currentContent.isNotBlank()) {
            MarkdownRenderer(
              markdownText = currentContent,
              onToggleTask = { lineIndex, isChecked ->
                val lines = currentContent.lines().toMutableList()
                if (lineIndex in lines.indices) {
                  val targetLine = lines[lineIndex]
                  val updatedLine = if (isChecked) {
                    targetLine.replaceFirst("- [ ]", "- [x]").replaceFirst("* [ ]", "* [x]")
                  } else {
                    targetLine.replaceFirst("- [x]", "- [ ]").replaceFirst("* [x]", "* [ ]")
                  }
                  lines[lineIndex] = updatedLine
                  val newText = lines.joinToString("\n")
                  contentTextFieldValue = TextFieldValue(text = newText)
                }
              },
              modifier = Modifier.testTag("markdown_rendered_preview")
            )
          } else {
            // Render exactly nothing for empty content preview
            Box(modifier = Modifier.fillMaxWidth().testTag("markdown_rendered_preview"))
          }
        } else {
          BasicTextField(
            value = contentTextFieldValue,
            onValueChange = { contentTextFieldValue = it },
            modifier = Modifier
              .fillMaxWidth()
              .focusRequester(contentFocusRequester)
              .testTag("note_content_input"),
            textStyle = TextStyle(
              fontSize = 15.sp,
              lineHeight = 24.sp,
              color = MaterialTheme.colorScheme.onSurface,
              textDirection = TextDirection.ContentOrLtr
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
              if (contentTextFieldValue.text.isEmpty()) {
                Text(
                  text = stringResource(R.string.note_body_placeholder),
                  style = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    textDirection = TextDirection.ContentOrLtr
                  )
                )
              }
              innerTextField()
            }
          )

          // Tapping on empty space below the text opens keyboard
          Spacer(
            modifier = Modifier
              .fillMaxWidth()
              .defaultMinSize(minHeight = 400.dp)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) {
                contentFocusRequester.requestFocus()
                keyboardController?.show()
              }
          )
        }
      }

      // Bottom Floating Bar moves with keyboard via imePadding
      if (!isPreviewMode) {
        EditorFloatingBar(
          onInsertHeading = { applyBlockFormat("## ") },
          onInsertBold = { applyInlineFormat("**", "**", "bold") },
          onInsertItalic = { applyInlineFormat("*", "*", "italic") },
          onInsertStrikethrough = { applyInlineFormat("~~", "~~", "strikethrough") },
          onInsertChecklist = { applyBlockFormat("- [ ] ") },
          onInsertBullet = { applyBlockFormat("- ") },
          onInsertNumbered = { applyBlockFormat("1. ") },
          onInsertCode = {
            val text = contentTextFieldValue.text
            val selection = contentTextFieldValue.selection
            if (selection.min != selection.max) {
              val start = selection.min
              val end = selection.max
              val selected = text.substring(start, end)
              if (selected.startsWith("```\n") && selected.endsWith("\n```")) {
                val unstyled = selected.removePrefix("```\n").removeSuffix("\n```")
                val newText = text.replaceRange(start, end, unstyled)
                contentTextFieldValue = TextFieldValue(text = newText, selection = TextRange(start, start + unstyled.length))
              } else if (selected.startsWith("`") && selected.endsWith("`") && selected.length >= 2) {
                val unstyled = selected.removePrefix("`").removeSuffix("`")
                val newText = text.replaceRange(start, end, unstyled)
                contentTextFieldValue = TextFieldValue(text = newText, selection = TextRange(start, start + unstyled.length))
              } else if (start >= 1 && end + 1 <= text.length && text[start - 1] == '`' && text[end] == '`') {
                val newText = text.substring(0, start - 1) + selected + text.substring(end + 1)
                contentTextFieldValue = TextFieldValue(text = newText, selection = TextRange(start - 1, start - 1 + selected.length))
              } else if (selected.contains("\n")) {
                val replacement = "```\n$selected\n```"
                val newText = text.replaceRange(start, end, replacement)
                contentTextFieldValue = TextFieldValue(text = newText, selection = TextRange(start + 4, start + 4 + selected.length))
              } else {
                val replacement = "`$selected`"
                val newText = text.replaceRange(start, end, replacement)
                contentTextFieldValue = TextFieldValue(text = newText, selection = TextRange(start + 1, start + 1 + selected.length))
              }
            } else {
              applyInlineFormat("`", "`", "code")
            }
          },
          onInsertQuote = { applyBlockFormat("> ") },
          onInsertTable = {
            val tableTemplate = "\n| Item | Status |\n| --- | --- |\n| Task 1 | In Progress |\n| Task 2 | Done |\n"
            val text = contentTextFieldValue.text
            val cursorPos = contentTextFieldValue.selection.start
            val newText = text.substring(0, cursorPos) + tableTemplate + text.substring(cursorPos)
            contentTextFieldValue = TextFieldValue(text = newText, selection = TextRange(cursorPos + tableTemplate.length))
          },
          onInsertHorizontalRule = {
            val rule = "\n---\n"
            val text = contentTextFieldValue.text
            val cursorPos = contentTextFieldValue.selection.start
            val newText = text.substring(0, cursorPos) + rule + text.substring(cursorPos)
            contentTextFieldValue = TextFieldValue(text = newText, selection = TextRange(cursorPos + rule.length))
          },
          onInsertLink = {
            val text = contentTextFieldValue.text
            val selection = contentTextFieldValue.selection
            if (selection.min != selection.max) {
              val start = selection.min
              val end = selection.max
              val selected = text.substring(start, end)
              val linkRegex = Regex("""^\[(.*)\]\(.*?\)$""")
              val match = linkRegex.find(selected)
              if (match != null) {
                val plain = match.groupValues[1]
                val newText = text.replaceRange(start, end, plain)
                contentTextFieldValue = TextFieldValue(text = newText, selection = TextRange(start, start + plain.length))
              } else {
                val replacement = "[$selected](https://)"
                val newText = text.replaceRange(start, end, replacement)
                contentTextFieldValue = TextFieldValue(text = newText, selection = TextRange(start + selected.length + 3, start + replacement.length - 1))
              }
            } else {
              val cursorPos = selection.start
              val insertion = "[link](https://)"
              val newText = text.substring(0, cursorPos) + insertion + text.substring(cursorPos)
              contentTextFieldValue = TextFieldValue(text = newText, selection = TextRange(cursorPos + 1, cursorPos + 5))
            }
          },
          onInsertTimestamp = {
            val now = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date())
            val stamp = " [$now] "
            val text = contentTextFieldValue.text
            val cursorPos = contentTextFieldValue.selection.start
            val newText = text.substring(0, cursorPos) + stamp + text.substring(cursorPos)
            contentTextFieldValue = TextFieldValue(text = newText, selection = TextRange(cursorPos + stamp.length))
          },
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .imePadding()
            .navigationBarsPadding()
        )
      }
    }
  }
}
