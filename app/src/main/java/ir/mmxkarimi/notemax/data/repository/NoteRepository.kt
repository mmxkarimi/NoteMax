package ir.mmxkarimi.notemax.data.repository

import ir.mmxkarimi.notemax.data.db.NoteDao
import ir.mmxkarimi.notemax.data.entity.NoteEntity
import ir.mmxkarimi.notemax.data.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepository(private val noteDao: NoteDao) {

  val allNotes: Flow<List<Note>> = noteDao.getAllNotes().map { entities ->
    entities.map { mapEntityToDomain(it) }
  }

  fun getNoteById(id: Long): Flow<Note?> {
    return noteDao.getNoteById(id).map { entity ->
      entity?.let { mapEntityToDomain(it) }
    }
  }

  suspend fun getNoteByIdDirect(id: Long): Note? {
    val entity = noteDao.getNoteByIdDirect(id) ?: return null
    return mapEntityToDomain(entity)
  }

  suspend fun saveNote(note: Note): Long {
    val now = System.currentTimeMillis()
    val finalContent = note.content

    val entity = NoteEntity(
      id = note.id,
      title = note.title,
      content = finalContent,
      isChecklist = note.isChecklist,
      colorHex = note.colorHex,
      isPinned = note.isPinned,
      isArchived = note.isArchived,
      isTrashed = note.isTrashed,
      createdAt = if (note.id == 0L) now else note.createdAt,
      updatedAt = now,
      orderIndex = note.orderIndex
    )

    val savedId = noteDao.insertNote(entity)
    return if (note.id == 0L) savedId else note.id
  }

  suspend fun reorderNotes(orderedIds: List<Long>) {
    orderedIds.forEachIndexed { index, id ->
      noteDao.updateOrderIndex(id, index)
    }
  }

  suspend fun togglePin(id: Long, isPinned: Boolean) {
    noteDao.setPinned(id, isPinned)
  }

  suspend fun toggleArchive(id: Long, isArchived: Boolean) {
    noteDao.setArchived(id, isArchived)
  }

  suspend fun moveToTrash(id: Long) {
    noteDao.setTrashed(id, true)
  }

  suspend fun restoreFromTrash(id: Long) {
    noteDao.setTrashed(id, false)
  }

  suspend fun deletePermanently(id: Long) {
    noteDao.deletePermanently(id)
  }

  suspend fun emptyTrash() {
    noteDao.emptyTrash()
  }

  suspend fun clearAllNotes() {
    noteDao.deleteAllNotes()
  }

  private fun mapEntityToDomain(entity: NoteEntity): Note {
    val parsedChecklist = if (entity.isChecklist) {
      Note.parseChecklist(entity.content)
    } else {
      emptyList()
    }

    return Note(
      id = entity.id,
      title = entity.title,
      content = entity.content,
      isChecklist = entity.isChecklist,
      checklistItems = parsedChecklist,
      colorHex = entity.colorHex,
      isPinned = entity.isPinned,
      isArchived = entity.isArchived,
      isTrashed = entity.isTrashed,
      createdAt = entity.createdAt,
      updatedAt = entity.updatedAt,
      orderIndex = entity.orderIndex
    )
  }

  suspend fun seedInitialNotesIfEmpty() {
    // Intentionally empty: pre-placed notes removed as requested
  }
}
