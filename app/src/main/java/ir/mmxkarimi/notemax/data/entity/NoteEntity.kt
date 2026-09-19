package ir.mmxkarimi.notemax.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val titleEncrypted: String,
  val contentEncrypted: String,
  val isChecklist: Boolean = false,
  val colorHex: Long = 0L,
  val isEncrypted: Boolean = false,
  val isPinned: Boolean = false,
  val isArchived: Boolean = false,
  val isTrashed: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val orderIndex: Int = 0
)
