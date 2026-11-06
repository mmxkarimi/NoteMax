package ir.mmxkarimi.notemax.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val title: String,
  val content: String,
  val isChecklist: Boolean = false,
  val colorHex: Long = 0L,
  val isPinned: Boolean = false,
  val isArchived: Boolean = false,
  val isTrashed: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val orderIndex: Int = 0,
  /** Comma-separated tag names (lowercase, no commas inside a tag). */
  @ColumnInfo(defaultValue = "")
  val tags: String = ""
)
