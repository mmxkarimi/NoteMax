package ir.mmxkarimi.notemax.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.data.model.ChecklistItem
import ir.mmxkarimi.notemax.data.model.Note
import ir.mmxkarimi.notemax.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ViewMode {
  GRID,
  LIST
}

enum class SortOrder(val stringRes: Int) {
  DATE_NEWEST(R.string.sort_recently_updated),
  DATE_OLDEST(R.string.sort_oldest),
  TITLE_AZ(R.string.sort_title_az),
  PINNED_FIRST(R.string.sort_pinned_first)
}

data class NotesUiState(
  val activeNotes: List<Note> = emptyList(),
  val pinnedNotes: List<Note> = emptyList(),
  val unpinnedNotes: List<Note> = emptyList(),
  val archivedNotes: List<Note> = emptyList(),
  val trashedNotes: List<Note> = emptyList(),
  val viewMode: ViewMode = ViewMode.GRID,
  val sortOrder: SortOrder = SortOrder.PINNED_FIRST,
  val totalActiveCount: Int = 0
)

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

  private val _viewMode = MutableStateFlow(ViewMode.GRID)
  val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

  private val _sortOrder = MutableStateFlow(SortOrder.PINNED_FIRST)
  val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

  val uiState: StateFlow<NotesUiState> = combine(
    repository.allNotes,
    _viewMode,
    _sortOrder
  ) { notes, mode, sort ->
    val activeList = notes.filter { !it.isTrashed }
    val archivedList = emptyList<Note>()
    val trashedList = notes.filter { it.isTrashed }

    // Sort items
    val sorted = when (sort) {
      SortOrder.DATE_NEWEST -> activeList.sortedByDescending { it.updatedAt }
      SortOrder.DATE_OLDEST -> activeList.sortedBy { it.updatedAt }
      SortOrder.TITLE_AZ -> activeList.sortedBy { it.title.lowercase() }
      SortOrder.PINNED_FIRST -> activeList.sortedWith(
        compareByDescending<Note> { it.isPinned }.thenBy { it.orderIndex }.thenByDescending { it.updatedAt }
      )
    }

    val pinned = sorted.filter { it.isPinned }
    val unpinned = sorted.filter { !it.isPinned }

    NotesUiState(
      activeNotes = sorted,
      pinnedNotes = pinned,
      unpinnedNotes = unpinned,
      archivedNotes = archivedList,
      trashedNotes = trashedList,
      viewMode = mode,
      sortOrder = sort,
      totalActiveCount = activeList.size
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = NotesUiState()
  )

  fun toggleViewMode() {
    _viewMode.value = if (_viewMode.value == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
  }

  fun setSortOrder(order: SortOrder) {
    _sortOrder.value = order
  }

  fun togglePin(note: Note) {
    viewModelScope.launch {
      repository.togglePin(note.id, !note.isPinned)
    }
  }

  fun toggleArchive(note: Note) {
    viewModelScope.launch {
      repository.toggleArchive(note.id, !note.isArchived)
    }
  }

  fun removeNote(note: Note) {
    viewModelScope.launch {
      repository.moveToTrash(note.id)
    }
  }

  fun moveToTrash(note: Note) {
    removeNote(note)
  }

  fun restoreNote(note: Note) {
    viewModelScope.launch {
      repository.restoreFromTrash(note.id)
    }
  }

  fun deletePermanently(note: Note) {
    viewModelScope.launch {
      repository.deletePermanently(note.id)
    }
  }

  fun emptyTrash() {
    viewModelScope.launch {
      repository.emptyTrash()
    }
  }

  fun saveNote(note: Note) {
    viewModelScope.launch {
      repository.saveNote(note)
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
    }
  }
}

class NotesViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
      return NotesViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
