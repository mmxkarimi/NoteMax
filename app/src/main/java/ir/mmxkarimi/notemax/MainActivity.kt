package ir.mmxkarimi.notemax

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.mmxkarimi.notemax.data.db.AppDatabase
import ir.mmxkarimi.notemax.data.model.Note
import ir.mmxkarimi.notemax.data.repository.NoteRepository
import ir.mmxkarimi.notemax.data.settings.SettingsStore
import ir.mmxkarimi.notemax.ui.screens.ArchiveScreen
import ir.mmxkarimi.notemax.ui.screens.HomeScreen
import ir.mmxkarimi.notemax.ui.screens.NoteEditorScreen
import ir.mmxkarimi.notemax.ui.screens.SearchScreen
import ir.mmxkarimi.notemax.ui.screens.SettingsScreen
import ir.mmxkarimi.notemax.ui.screens.TrashScreen
import ir.mmxkarimi.notemax.ui.theme.NoteMaxTheme
import ir.mmxkarimi.notemax.ui.viewmodel.NotesViewModel
import ir.mmxkarimi.notemax.ui.viewmodel.NotesViewModelFactory

/** Navigation destinations. Encoded as strings so the back stack survives rotation / process death. */
sealed interface Screen {
  data object Home : Screen
  data object Search : Screen
  data object Archive : Screen
  data object Trash : Screen
  data object Settings : Screen
  /** [noteId] == 0 means a new note, optionally pre-filled by an intent. */
  data class Editor(val noteId: Long, val title: String = "", val content: String = "") : Screen
}

private fun Screen.encode(): String = when (this) {
  Screen.Home -> "home"
  Screen.Search -> "search"
  Screen.Archive -> "archive"
  Screen.Trash -> "trash"
  Screen.Settings -> "settings"
  is Screen.Editor -> "editor|$noteId|${java.net.URLEncoder.encode(title, "UTF-8")}|${java.net.URLEncoder.encode(content, "UTF-8")}"
}

private fun decodeScreen(raw: String): Screen = when {
  raw == "search" -> Screen.Search
  raw == "archive" -> Screen.Archive
  raw == "trash" -> Screen.Trash
  raw == "settings" -> Screen.Settings
  raw.startsWith("editor|") -> {
    val p = raw.split("|")
    Screen.Editor(
      noteId = p.getOrNull(1)?.toLongOrNull() ?: 0L,
      title = java.net.URLDecoder.decode(p.getOrElse(2) { "" }, "UTF-8"),
      content = java.net.URLDecoder.decode(p.getOrElse(3) { "" }, "UTF-8")
    )
  }
  else -> Screen.Home
}

class MainActivity : ComponentActivity() {

  companion object {
    const val EXTRA_NOTE_ID = "ir.mmxkarimi.notemax.EXTRA_NOTE_ID"
    const val ACTION_NEW_NOTE = "ir.mmxkarimi.notemax.ACTION_NEW_NOTE"
  }

  private val database by lazy { AppDatabase.getInstance(applicationContext) }
  private val repository by lazy { NoteRepository(database.noteDao()) }
  private val settingsStore by lazy { SettingsStore(applicationContext) }
  private val viewModel: NotesViewModel by viewModels {
    NotesViewModelFactory(repository, settingsStore, applicationContext)
  }

  private var initialTargetNoteId by mutableStateOf<Long?>(null)
  private var shouldCreateNewNote by mutableStateOf(false)
  private var pendingInitialTitle by mutableStateOf("")
  private var pendingInitialContent by mutableStateOf("")

  var onSaveCurrentDraft: (() -> Unit)? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Only handle launch intents on a fresh start; after rotation the saved back stack is restored.
    if (savedInstanceState == null) handleNoteIntent(intent)

    // Single place for edge-to-edge; bar icon contrast follows the in-app theme (see NoteMaxTheme).
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
    )

    setContent {
      val settings by viewModel.settings.collectAsStateWithLifecycle()
      NoteMaxTheme(themeMode = settings.themeMode, dynamicColor = settings.dynamicColor) {
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
    if (intent == null) return

    val action = intent.action
    val isNewNoteAction = action == ACTION_NEW_NOTE ||
      action == "com.google.android.gms.actions.CREATE_NOTE" ||
      action == "android.intent.action.CREATE_NOTE" ||
      intent.getStringExtra("action") == "new_note"

    if (isNewNoteAction) {
      val title = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        ?: intent.getStringExtra(Intent.EXTRA_TITLE)
        ?: intent.getStringExtra("name")
        ?: ""
      val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        ?: intent.getStringExtra("text")
        ?: ""
      pendingInitialTitle = title
      pendingInitialContent = text
      shouldCreateNewNote = true
      return
    }

    val noteId = intent.getLongExtra(EXTRA_NOTE_ID, 0L)
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
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // Back stack: bottom = Home. Persisted as strings.
    val stack = rememberSaveable(
      saver = listSaver<SnapshotStateList<Screen>, String>(
        save = { it.map(Screen::encode) },
        restore = { saved -> saved.map(::decodeScreen).toMutableStateList() }
      )
    ) { mutableStateListOf<Screen>(Screen.Home) }

    fun push(screen: Screen) {
      if (stack.lastOrNull() != screen) stack.add(screen)
    }
    fun pop() {
      if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }
    /** Finishing an editor replaces it; back never lands on a stale editor. */
    fun closeEditor() = pop()

    LaunchedEffect(shouldCreateNewNote) {
      if (shouldCreateNewNote) {
        onSaveCurrentDraft?.invoke()
        while (stack.size > 1) stack.removeAt(stack.lastIndex)
        stack.add(Screen.Editor(0L, pendingInitialTitle, pendingInitialContent))
        shouldCreateNewNote = false
        pendingInitialTitle = ""
        pendingInitialContent = ""
      }
    }

    LaunchedEffect(initialTargetNoteId) {
      val targetId = initialTargetNoteId ?: return@LaunchedEffect
      onSaveCurrentDraft?.invoke()
      while (stack.size > 1) stack.removeAt(stack.lastIndex)
      stack.add(Screen.Editor(targetId))
      initialTargetNoteId = null
    }

    val current = stack.last()

    // Non-home screens pop on system/predictive back. The editor registers its own BackHandler (autosave).
    BackHandler(enabled = stack.size > 1 && current !is Screen.Editor) { pop() }

    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background,
      contentColor = MaterialTheme.colorScheme.onBackground
    ) {
    AnimatedContent(
      targetState = current,
      transitionSpec = {
        val goingDeeper = targetState.depth() > initialState.depth()
        val enter = fadeIn(tween(220, easing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f))) +
          scaleIn(initialScale = if (goingDeeper) 0.96f else 1.02f, animationSpec = tween(260, easing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)))
        val exit = fadeOut(tween(140))
        enter.togetherWith(exit)
      },
      label = "screen_navigation",
      modifier = Modifier.fillMaxSize()
    ) { screen ->
      when (screen) {
        Screen.Home -> HomeScreen(
          uiState = uiState,
          settings = settings,
          onNoteClick = { note -> push(Screen.Editor(note.id)) },
          onNewNote = { push(Screen.Editor(0L)) },
          onOpenSearch = { push(Screen.Search) },
          onOpenArchive = { push(Screen.Archive) },
          onOpenTrash = { push(Screen.Trash) },
          onOpenSettings = { push(Screen.Settings) },
          onToggleView = { viewModel.updateSettings { it.copy(gridView = !it.gridView) } },
          onToggleChecklistItem = { note, i -> viewModel.toggleChecklistItem(note, i) },
          onBulkTrash = viewModel::bulkTrash,
          onBulkRestore = viewModel::bulkRestore,
          onBulkArchive = viewModel::bulkArchive,
          onBulkPin = viewModel::bulkPin,
          onBulkColor = viewModel::bulkColor,
          onReorderNotes = viewModel::reorderNotes
        )

        Screen.Search -> SearchScreen(
          notes = uiState.activeNotes + uiState.archivedNotes,
          allTags = uiState.allTags,
          onBack = { pop() },
          onNoteClick = { note ->
            // Search is transient: opening a result replaces it so back returns to Home.
            stack.removeAt(stack.lastIndex)
            stack.add(Screen.Editor(note.id))
          }
        )

        Screen.Archive -> ArchiveScreen(
          notes = uiState.archivedNotes,
          onBack = { pop() },
          onNoteClick = { note -> push(Screen.Editor(note.id)) },
          onUnarchive = { viewModel.setArchived(it, false) },
          onToggleChecklistItem = { note, i -> viewModel.toggleChecklistItem(note, i) }
        )

        Screen.Trash -> TrashScreen(
          notes = uiState.trashedNotes,
          retentionDays = settings.trashRetentionDays,
          onBack = { pop() },
          onRestore = viewModel::restoreNote,
          onDeleteForever = viewModel::deletePermanently,
          onEmptyTrash = viewModel::emptyTrash
        )

        Screen.Settings -> SettingsScreen(
          settings = settings,
          onChange = { viewModel.updateSettings(it) },
          onBack = { pop() }
        )

        is Screen.Editor -> {
          // New notes open instantly; existing ones load by id once.
          val note by produceState<Note?>(
            initialValue = if (screen.noteId == 0L) Note(title = screen.title, content = screen.content) else null,
            key1 = screen.noteId
          ) {
            if (screen.noteId != 0L) {
              value = viewModel.getNote(screen.noteId)
              if (value == null) closeEditor()
            }
          }
          val loaded = note
          if (loaded != null) {
            NoteEditorScreen(
              initialNote = loaded,
              tagSuggestions = uiState.allTags,
              onSaveAndClose = { updated ->
                viewModel.saveNote(updated)
                closeEditor()
              },
              onDeleteNote = { toDelete ->
                viewModel.removeNote(toDelete)
                closeEditor()
              },
              onNavigateBack = { closeEditor() },
              onSaveDraft = { draft -> viewModel.saveNoteSync(draft) }
            )
          }
        }
      }
    }
    }
  }

  private fun Screen.depth(): Int = when (this) {
    Screen.Home -> 0
    is Screen.Editor -> 2
    else -> 1
  }
}
