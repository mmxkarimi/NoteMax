package ir.mmxkarimi.notemax

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

  private val database by lazy { AppDatabase.getInstance(applicationContext) }
  private val repository by lazy { NoteRepository(database.noteDao()) }
  private val viewModel: NotesViewModel by viewModels {
    NotesViewModelFactory(repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
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

  @Composable
  private fun MainAppContent() {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

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
            onToggleViewMode = { viewModel.toggleViewMode() },
            onSortOrderSelected = { viewModel.setSortOrder(it) },
            onNoteClick = { note ->
              currentScreen = Screen.Editor(note)
            },
            onNewNote = {
              val newNote = Note(
                isChecklist = false,
                isEncrypted = false,
                isUnlocked = true
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
            }
          )
        }
      }
    }
  }
}
