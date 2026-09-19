package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
  var content by remember { mutableStateOf(initialNote?.content ?: "") }
  var isPinned by remember { mutableStateOf(initialNote?.isPinned ?: false) }
  var isPreviewMode by remember { mutableStateOf(false) }

  fun saveCurrentNote() {
    val noteToSave = (initialNote ?: Note()).copy(
      title = title.trim(),
      content = content.trim(),
      isChecklist = content.lines().any { it.trimStart().startsWith("- [ ]") || it.trimStart().startsWith("- [x]") },
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

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .imePadding()
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
          // Preview / Edit Mode Toggle Button (Icon only)
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
                painter = painterResource(R.drawable.ic_visibility),
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
              painter = painterResource(if (isPinned) R.drawable.ic_pin else R.drawable.ic_pin_outlined),
              contentDescription = stringResource(if (isPinned) R.string.action_unpin else R.string.action_pin),
              tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Share Button
          IconButton(
            onClick = {
              val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(Intent.EXTRA_TEXT, "$title\n\n$content")
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

          // Delete Button (if editing existing note)
          if (initialNote != null && initialNote.id != 0L) {
            IconButton(
              onClick = { onDeleteNote(initialNote) },
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
          .padding(bottom = 96.dp) // Room for the bottom floating bar
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
          MarkdownRenderer(
            markdownText = content.ifBlank { stringResource(R.string.note_preview_empty) },
            onToggleTask = { lineIndex, isChecked ->
              val lines = content.lines().toMutableList()
              if (lineIndex in lines.indices) {
                val targetLine = lines[lineIndex]
                val updatedLine = if (isChecked) {
                  targetLine.replaceFirst("- [ ]", "- [x]").replaceFirst("* [ ]", "* [x]")
                } else {
                  targetLine.replaceFirst("- [x]", "- [ ]").replaceFirst("* [x]", "* [ ]")
                }
                lines[lineIndex] = updatedLine
                content = lines.joinToString("\n")
              }
            },
            modifier = Modifier.testTag("markdown_rendered_preview")
          )
        } else {
          BasicTextField(
            value = content,
            onValueChange = { content = it },
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
              if (content.isEmpty()) {
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

      // Bottom Floating Bar for Editor Features & Markdown
      EditorFloatingBar(
        onInsertHeading = {
          content = if (content.endsWith("\n") || content.isEmpty()) "$content## " else "$content\n## "
        },
        onInsertBold = {
          content = "$content**bold**"
        },
        onInsertItalic = {
          content = "$content*italic*"
        },
        onInsertStrikethrough = {
          content = "$content~~strikethrough~~"
        },
        onInsertChecklist = {
          content = if (content.endsWith("\n") || content.isEmpty()) "$content- [ ] " else "$content\n- [ ] "
        },
        onInsertBullet = {
          content = if (content.endsWith("\n") || content.isEmpty()) "$content- " else "$content\n- "
        },
        onInsertNumbered = {
          content = if (content.endsWith("\n") || content.isEmpty()) "${content}1. " else "$content\n1. "
        },
        onInsertCode = {
          content = if (content.endsWith("\n") || content.isEmpty()) "$content```\n// code\n```\n" else "$content\n```\n// code\n```\n"
        },
        onInsertQuote = {
          content = if (content.endsWith("\n") || content.isEmpty()) "$content> " else "$content\n> "
        },
        onInsertTable = {
          val tableTemplate = "\n| Item | Status |\n| --- | --- |\n| Task 1 | In Progress |\n| Task 2 | Done |\n"
          content = "$content$tableTemplate"
        },
        onInsertHorizontalRule = {
          content = if (content.endsWith("\n") || content.isEmpty()) "$content---\n" else "$content\n---\n"
        },
        onInsertLink = {
          content = "$content[link title](https://example.com)"
        },
        onInsertTimestamp = {
          val now = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date())
          content = "$content\n[$now]\n"
        },
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
      )
    }
  }
}
