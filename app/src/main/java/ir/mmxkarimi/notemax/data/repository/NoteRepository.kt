package ir.mmxkarimi.notemax.data.repository

import ir.mmxkarimi.notemax.crypto.CryptoManager
import ir.mmxkarimi.notemax.data.db.NoteDao
import ir.mmxkarimi.notemax.data.entity.NoteEntity
import ir.mmxkarimi.notemax.data.model.ChecklistItem
import ir.mmxkarimi.notemax.data.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class NoteRepository(private val noteDao: NoteDao) {

  // In-memory set of note IDs that have been unlocked via biometric verification in the current session
  private val unlockedNoteIds = MutableStateFlow<Set<Long>>(emptySet())
  
  // Master vault lock state (if true, all encrypted notes are accessible)
  private val isGlobalVaultUnlocked = MutableStateFlow(false)

  val allNotes: Flow<List<Note>> = combine(
    noteDao.getAllNotes(),
    unlockedNoteIds,
    isGlobalVaultUnlocked
  ) { entities, unlockedIds, globalVaultOpen ->
    entities.map { entity ->
      val isUnlocked = !entity.isEncrypted || globalVaultOpen || unlockedIds.contains(entity.id)
      mapEntityToDomain(entity, isUnlocked)
    }
  }

  fun getNoteById(id: Long): Flow<Note?> {
    return combine(
      noteDao.getNoteById(id),
      unlockedNoteIds,
      isGlobalVaultUnlocked
    ) { entity, unlockedIds, globalVaultOpen ->
      entity?.let {
        val isUnlocked = !it.isEncrypted || globalVaultOpen || unlockedIds.contains(it.id)
        mapEntityToDomain(it, isUnlocked)
      }
    }
  }

  suspend fun getNoteByIdDirect(id: Long): Note? {
    val entity = noteDao.getNoteByIdDirect(id) ?: return null
    val isUnlocked = !entity.isEncrypted || isGlobalVaultUnlocked.value || unlockedNoteIds.value.contains(entity.id)
    return mapEntityToDomain(entity, isUnlocked)
  }

  fun unlockNote(noteId: Long) {
    unlockedNoteIds.value = unlockedNoteIds.value + noteId
  }

  fun lockNote(noteId: Long) {
    unlockedNoteIds.value = unlockedNoteIds.value - noteId
  }

  fun unlockAllNotes() {
    isGlobalVaultUnlocked.value = true
  }

  fun lockAllNotes() {
    unlockedNoteIds.value = emptySet()
    isGlobalVaultUnlocked.value = false
  }

  fun isVaultUnlocked(): Boolean = isGlobalVaultUnlocked.value

  suspend fun saveNote(note: Note): Long {
    val now = System.currentTimeMillis()
    val finalContent = note.content

    // Perform AES-256 GCM encryption on title and content if note is marked encrypted
    val (encryptedTitle, encryptedContent) = if (note.isEncrypted) {
      Pair(
        CryptoManager.encrypt(note.title),
        CryptoManager.encrypt(finalContent)
      )
    } else {
      Pair(note.title, finalContent)
    }

    val entity = NoteEntity(
      id = note.id,
      titleEncrypted = encryptedTitle,
      contentEncrypted = encryptedContent,
      isChecklist = note.isChecklist,
      colorHex = note.colorHex,
      isEncrypted = note.isEncrypted,
      isPinned = note.isPinned,
      isArchived = note.isArchived,
      isTrashed = note.isTrashed,
      createdAt = if (note.id == 0L) now else note.createdAt,
      updatedAt = now,
      orderIndex = note.orderIndex
    )

    val savedId = noteDao.insertNote(entity)
    // Automatically consider freshly edited or created note as unlocked in session
    val targetId = if (note.id == 0L) savedId else note.id
    unlockNote(targetId)
    return targetId
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
    lockNote(id)
  }

  suspend fun emptyTrash() {
    noteDao.emptyTrash()
  }

  suspend fun clearAllNotes() {
    noteDao.deleteAllNotes()
    unlockedNoteIds.value = emptySet()
    isGlobalVaultUnlocked.value = false
  }

  private fun mapEntityToDomain(entity: NoteEntity, isUnlocked: Boolean): Note {
    val (decryptedTitle, decryptedContent) = if (entity.isEncrypted) {
      if (isUnlocked) {
        Pair(
          CryptoManager.decrypt(entity.titleEncrypted),
          CryptoManager.decrypt(entity.contentEncrypted)
        )
      } else {
        // Return masked representation to preserve strict security in memory and UI
        val maskedTitle = try {
          val decrypted = CryptoManager.decrypt(entity.titleEncrypted)
          if (decrypted.isNotBlank() && !decrypted.startsWith("[")) decrypted else "Encrypted Vault Note"
        } catch (_: Exception) {
          "Encrypted Vault Note"
        }
        Pair(maskedTitle, "")
      }
    } else {
      Pair(entity.titleEncrypted, entity.contentEncrypted)
    }

    val parsedChecklist = if (entity.isChecklist) {
      Note.parseChecklist(decryptedContent)
    } else {
      emptyList()
    }

    return Note(
      id = entity.id,
      title = decryptedTitle,
      content = decryptedContent,
      isChecklist = entity.isChecklist,
      checklistItems = parsedChecklist,
      colorHex = entity.colorHex,
      isEncrypted = entity.isEncrypted,
      isPinned = entity.isPinned,
      isArchived = entity.isArchived,
      isTrashed = entity.isTrashed,
      createdAt = entity.createdAt,
      updatedAt = entity.updatedAt,
      isUnlocked = isUnlocked,
      orderIndex = entity.orderIndex
    )
  }

  suspend fun seedInitialNotesIfEmpty() {
    // Intentionally empty: pre-placed notes removed as requested
  }
}
