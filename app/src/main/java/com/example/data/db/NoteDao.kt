package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
  @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
  fun getAllNotes(): Flow<List<NoteEntity>>

  @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
  fun getNoteById(id: Long): Flow<NoteEntity?>

  @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
  suspend fun getNoteByIdDirect(id: Long): NoteEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNote(note: NoteEntity): Long

  @Update
  suspend fun updateNote(note: NoteEntity)

  @Query("UPDATE notes SET isPinned = :isPinned, updatedAt = :timestamp WHERE id = :id")
  suspend fun setPinned(id: Long, isPinned: Boolean, timestamp: Long = System.currentTimeMillis())

  @Query("UPDATE notes SET isArchived = :isArchived, updatedAt = :timestamp WHERE id = :id")
  suspend fun setArchived(id: Long, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

  @Query("UPDATE notes SET isTrashed = :isTrashed, updatedAt = :timestamp WHERE id = :id")
  suspend fun setTrashed(id: Long, isTrashed: Boolean, timestamp: Long = System.currentTimeMillis())

  @Query("DELETE FROM notes WHERE id = :id")
  suspend fun deletePermanently(id: Long)

  @Query("DELETE FROM notes WHERE isTrashed = 1")
  suspend fun emptyTrash()

  @Query("DELETE FROM notes")
  suspend fun deleteAllNotes()

  @Query("SELECT COUNT(*) FROM notes WHERE isTrashed = 0 AND isArchived = 0")
  fun getActiveNotesCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM notes WHERE isEncrypted = 1 AND isTrashed = 0")
  fun getEncryptedNotesCount(): Flow<Int>
}
