package ir.mmxkarimi.notemax.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.mmxkarimi.notemax.data.model.ChecklistItem
import ir.mmxkarimi.notemax.data.model.Note
import ir.mmxkarimi.notemax.data.repository.NoteRepository
import ir.mmxkarimi.notemax.data.settings.AppSettings
import ir.mmxkarimi.notemax.data.settings.SettingsStore
import ir.mmxkarimi.notemax.data.settings.SortMode
import ir.mmxkarimi.notemax.widget.NoteWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class NotesUiState(
  val isLoading: Boolean = true,
  val activeNotes: List<Note> = emptyList(),
  val pinnedNotes: List<Note> = emptyList(),
  val unpinnedNotes: List<Note> = emptyList(),
  val archivedNotes: List<Note> = emptyList(),
  val trashedNotes: List<Note> = emptyList(),
  val allTags: List<String> = emptyList(),
  val totalActiveCount: Int = 0
)

class NotesViewModel(
  private val repository: NoteRepository,
  private val settingsStore: SettingsStore,
  private val context: Context
) : ViewModel() {

  val settings: StateFlow<AppSettings> = settingsStore.settings

  val uiState: StateFlow<NotesUiState> = combine(repository.allNotes, settingsStore.settings) { notes, settings ->
    val live = notes.filter { !it.isTrashed }
    val activeList = live.filter { !it.isArchived }
    val archivedList = live.filter { it.isArchived }.sortedByDescending { it.updatedAt }
    val trashedList = notes.filter { it.isTrashed }.sortedByDescending { it.updatedAt }

    val sorted = when (settings.sortMode) {
      SortMode.Manual -> activeList.sortedWith(
        compareByDescending<Note> { it.isPinned }.thenBy { it.orderIndex }.thenByDescending { it.updatedAt }
      )
      SortMode.Edited -> activeList.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
      SortMode.Created -> activeList.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.createdAt })
      SortMode.Title -> activeList.sortedWith(compareByDescending<Note> { it.isPinned }.thenBy { it.title.lowercase() })
    }

    NotesUiState(
      isLoading = false,
      activeNotes = sorted,
      pinnedNotes = sorted.filter { it.isPinned },
      unpinnedNotes = sorted.filter { !it.isPinned },
      archivedNotes = archivedList,
      trashedNotes = trashedList,
      allTags = (activeList + archivedList).flatMap { it.tags }.groupingBy { it }.eachCount()
        .entries.sortedByDescending { it.value }.map { it.key },
      totalActiveCount = activeList.size
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = NotesUiState(isLoading = true)
  )

  init {
    purgeExpiredTrash()
  }

  /** Auto-purges trashed notes older than the retention window chosen in Settings. */
  fun purgeExpiredTrash() {
    viewModelScope.launch {
      val days = settingsStore.settings.value.trashRetentionDays
      if (days > 0) {
        repository.purgeTrashOlderThan(System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L)
      }
    }
  }

  suspend fun getNote(id: Long): Note? = repository.getNoteByIdDirect(id)

  fun updateSettings(transform: (AppSettings) -> AppSettings) = settingsStore.update(transform)

  fun setArchived(note: Note, archived: Boolean) {
    viewModelScope.launch {
      repository.toggleArchive(note.id, archived)
      NoteWidgetProvider.updateAllWidgets(context)
    }
  }

  /** Bulk helpers used by the Home multi-select action bar. */
  fun bulkTrash(notes: List<Note>) = notes.forEach { removeNote(it) }
  fun bulkRestore(notes: List<Note>) = notes.forEach { restoreNote(it) }
  fun bulkArchive(notes: List<Note>, archived: Boolean) = notes.forEach { setArchived(it, archived) }

  fun bulkPin(notes: List<Note>, pinned: Boolean) {
    viewModelScope.launch {
      notes.forEach { repository.togglePin(it.id, pinned) }
      NoteWidgetProvider.updateAllWidgets(context)
    }
  }

  fun bulkColor(notes: List<Note>, colorIndex: Long) {
    viewModelScope.launch {
      notes.forEach { repository.saveNote(it.copy(colorHex = colorIndex, updatedAt = System.currentTimeMillis())) }
      NoteWidgetProvider.updateAllWidgets(context)
    }
  }

  fun togglePin(note: Note) {
    viewModelScope.launch {
      repository.togglePin(note.id, !note.isPinned)
      NoteWidgetProvider.updateAllWidgets(context)
    }
  }

  fun toggleArchive(note: Note) {
    viewModelScope.launch {
      repository.toggleArchive(note.id, !note.isArchived)
      NoteWidgetProvider.updateAllWidgets(context)
    }
  }

  fun removeNote(note: Note) {
    viewModelScope.launch {
      repository.moveToTrash(note.id)
      NoteWidgetProvider.updateAllWidgets(context)
    }
  }

  fun moveToTrash(note: Note) {
    removeNote(note)
  }

  fun restoreNote(note: Note) {
    viewModelScope.launch {
      repository.restoreFromTrash(note.id)
      NoteWidgetProvider.updateAllWidgets(context)
    }
  }

  fun deletePermanently(note: Note) {
    viewModelScope.launch {
      repository.deletePermanently(note.id)
      NoteWidgetProvider.updateAllWidgets(context)
    }
  }

  fun emptyTrash() {
    viewModelScope.launch {
      repository.emptyTrash()
      NoteWidgetProvider.updateAllWidgets(context)
    }
  }

  fun saveNote(note: Note) {
    viewModelScope.launch {
      repository.saveNote(note)
      NoteWidgetProvider.updateAllWidgets(context)
    }
  }

  fun saveNoteSync(note: Note): Long {
    return runBlocking(Dispatchers.IO) {
      val id = repository.saveNote(note)
      NoteWidgetProvider.updateAllWidgets(context)
      id
    }
  }

  fun reorderNotes(reorderedNotes: List<Note>) {
    viewModelScope.launch {
      repository.reorderNotes(reorderedNotes.map { it.id })
    }
  }

  fun toggleChecklistItem(note: Note, itemIndex: Int) {
    viewModelScope.launch {
      val lines = note.content.lines().toMutableList()
      var checklistCount = 0
      for (i in lines.indices) {
        val trimmed = lines[i].trim()
        val isBox = trimmed.startsWith("- [ ] ") || trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") ||
          trimmed.startsWith("* [ ] ") || trimmed.startsWith("* [x] ") || trimmed.startsWith("* [X] ")
        if (isBox) {
          if (checklistCount == itemIndex) {
            val indent = lines[i].substring(0, lines[i].indexOf(trimmed))
            val prefix = if (trimmed.startsWith("*")) "*" else "-"
            val text = trimmed.substring(6)
            val isChecked = trimmed.startsWith("- [x]") || trimmed.startsWith("- [X]") ||
              trimmed.startsWith("* [x]") || trimmed.startsWith("* [X]")
            val newCheck = if (isChecked) "[ ]" else "[x]"
            lines[i] = "$indent$prefix $newCheck $text"
            break
          }
          checklistCount++
        }
      }
      val newContent = lines.joinToString("\n")
      val updatedNote = note.copy(
        content = newContent,
        checklistItems = Note.parseChecklist(newContent),
        updatedAt = System.currentTimeMillis()
      )
      repository.saveNote(updatedNote)
      NoteWidgetProvider.updateAllWidgets(context)
    }
  }
}

class NotesViewModelFactory(
  private val repository: NoteRepository,
  private val settingsStore: SettingsStore,
  private val context: Context
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
      return NotesViewModel(repository, settingsStore, context.applicationContext) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
