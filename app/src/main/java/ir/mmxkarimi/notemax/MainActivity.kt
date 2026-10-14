package ir.mmxkarimi.notemax

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.mmxkarimi.notemax.data.db.AppDatabase
import ir.mmxkarimi.notemax.data.model.Note
import ir.mmxkarimi.notemax.data.repository.NoteRepository
import ir.mmxkarimi.notemax.ui.screens.HomeScreen
import ir.mmxkarimi.notemax.ui.screens.NoteEditorScreen
import ir.mmxkarimi.notemax.ui.theme.MyApplicationTheme
import ir.mmxkarimi.notemax.ui.viewmodel.NotesViewModel
import ir.mmxkarimi.notemax.ui.viewmodel.NotesViewModelFactory

sealed interface Screen {
  data object Home : Screen
  data class Editor(val note: Note) : Screen
}

class MainActivity : ComponentActivity() {

  companion object {
    const val EXTRA_NOTE_ID = "ir.mmxkarimi.notemax.EXTRA_NOTE_ID"
  }

  private val database by lazy { AppDatabase.getInstance(applicationContext) }
  private val repository by lazy { NoteRepository(database.noteDao()) }
  private val viewModel: NotesViewModel by viewModels {
    NotesViewModelFactory(repository, applicationContext)
  }

  private var initialTargetNoteId by mutableStateOf<Long?>(null)

  var onSaveCurrentDraft: (() -> Unit)? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleNoteIntent(intent)

    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(
        android.graphics.Color.TRANSPARENT,
        android.graphics.Color.TRANSPARENT
      ),
      navigationBarStyle = SystemBarStyle.auto(
        android.graphics.Color.TRANSPARENT,
        android.graphics.Color.TRANSPARENT
      )
    )

    setContent {
      MyApplicationTheme {
        MainAppContent()
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleNoteIntent(intent)
  }

  private fun handleNoteIntent(intent: Intent?) {
    val noteId = intent?.getLongExtra(EXTRA_NOTE_ID, 0L) ?: 0L
    if (noteId > 0L) {
      initialTargetNoteId = noteId
    }
  }

  override fun onPause() {
    super.onPause()
    onSaveCurrentDraft?.invoke()
  }

  override fun onStop() {
    super.onStop()
    onSaveCurrentDraft?.invoke()
  }

  @Composable
  private fun MainAppContent() {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    LaunchedEffect(initialTargetNoteId, uiState.activeNotes) {
      val targetId = initialTargetNoteId ?: return@LaunchedEffect
      val note = uiState.activeNotes.firstOrNull { it.id == targetId }
      if (note != null) {
        currentScreen = Screen.Editor(note)
        initialTargetNoteId = null
      }
    }

    AnimatedContent(
      targetState = currentScreen,
      transitionSpec = {
        (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
          scaleIn(initialScale = 0.96f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
          .togetherWith(
            fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
              scaleOut(targetScale = 0.96f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
          )
      },
      label = "screen_navigation",
      modifier = Modifier.fillMaxSize()
    ) { targetScreen ->
      when (targetScreen) {
        is Screen.Home -> {
          HomeScreen(
            uiState = uiState,
            onNoteClick = { note ->
              currentScreen = Screen.Editor(note)
            },
            onNewNote = {
              val newNote = Note(
                isChecklist = false
              )
              currentScreen = Screen.Editor(newNote)
            },
            onTogglePin = { note -> viewModel.togglePin(note) },
            onRemove = { note -> viewModel.removeNote(note) },
            onRestore = { note -> viewModel.restoreNote(note) },
            onDeletePermanently = { note -> viewModel.deletePermanently(note) },
            onToggleChecklistItem = { note, itemIndex ->
              viewModel.toggleChecklistItem(note, itemIndex)
            },
            onReorderNotes = { reorderedList ->
              viewModel.reorderNotes(reorderedList)
            }
          )
        }

        is Screen.Editor -> {
          NoteEditorScreen(
            initialNote = targetScreen.note,
            onSaveAndClose = { updatedNote ->
              viewModel.saveNote(updatedNote)
              currentScreen = Screen.Home
            },
            onDeleteNote = { noteToDelete ->
              viewModel.removeNote(noteToDelete)
              currentScreen = Screen.Home
            },
            onNavigateBack = {
              currentScreen = Screen.Home
            },
            onSaveDraft = { draftNote ->
              viewModel.saveNoteSync(draftNote)
            }
          )
        }
      }
    }
  }
}
