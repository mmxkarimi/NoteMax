package ir.mmxkarimi.notemax.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.data.db.AppDatabase
import ir.mmxkarimi.notemax.data.model.Note
import ir.mmxkarimi.notemax.data.repository.NoteRepository
import ir.mmxkarimi.notemax.ui.components.MarkdownPreview
import ir.mmxkarimi.notemax.ui.theme.MyApplicationTheme

class NoteWidgetConfigureActivity : ComponentActivity() {

  private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
  private val repository by lazy { NoteRepository(AppDatabase.getInstance(applicationContext).noteDao()) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setResult(Activity.RESULT_CANCELED)

    val intent = intent
    val extras = intent.extras
    if (extras != null) {
      appWidgetId = extras.getInt(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
      )
    }

    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
      return
    }

    setContent {
      MyApplicationTheme {
        WidgetConfigureContent(
          onSelectNote = { selectedNote ->
            selectNoteAndFinish(selectedNote)
          },
          onClose = { finish() }
        )
      }
    }
  }

  private fun selectNoteAndFinish(note: Note) {
    NoteWidgetPrefs.saveNoteId(this, appWidgetId, note.id)

    // Trigger update for this widget
    val appWidgetManager = AppWidgetManager.getInstance(this)
    val provider = NoteWidgetProvider()
    provider.onUpdate(this, appWidgetManager, intArrayOf(appWidgetId))
    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view)

    val resultValue = Intent().apply {
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }
    setResult(Activity.RESULT_OK, resultValue)
    finish()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  private fun WidgetConfigureContent(
    onSelectNote: (Note) -> Unit,
    onClose: () -> Unit
  ) {
    val notes by repository.allNotes.collectAsStateWithLifecycle(initialValue = emptyList())
    val activeNotes = remember(notes) { notes.filter { !it.isTrashed } }
    var searchQuery by remember { mutableStateOf("") }

    val filteredNotes = remember(searchQuery, activeNotes) {
      val trimmed = searchQuery.trim()
      if (trimmed.isEmpty()) {
        activeNotes
      } else {
        activeNotes.filter { note ->
          note.title.contains(trimmed, ignoreCase = true) ||
            note.content.contains(trimmed, ignoreCase = true)
        }
      }
    }

    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Text(
              text = stringResource(R.string.widget_configure_title),
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          },
          navigationIcon = {
            IconButton(onClick = onClose) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back)
              )
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
          )
        )
      },
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .padding(horizontal = 16.dp)
      ) {
        // Search bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text(stringResource(R.string.search_notes_placeholder)) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = stringResource(R.string.search_notes),
              tint = MaterialTheme.colorScheme.primary
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = stringResource(R.string.search_clear)
                )
              }
            }
          },
          singleLine = true,
          shape = CircleShape,
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredNotes.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (searchQuery.isNotBlank()) stringResource(R.string.no_search_results) else stringResource(R.string.no_notes_yet),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(filteredNotes, key = { it.id }) { note ->
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 2.dp,
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp))
                  .clickable { onSelectNote(note) }
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = if (note.title.isNotBlank()) note.title else stringResource(R.string.untitled_note),
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.weight(1f)
                    )
                    Text(
                      text = note.formattedDateShort,
                      style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  if (note.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    MarkdownPreview(
                      markdownText = note.content,
                      maxLines = 2
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}
