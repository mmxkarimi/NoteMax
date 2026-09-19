package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChecklistItem(
  val id: String = java.util.UUID.randomUUID().toString(),
  val text: String = "",
  val isChecked: Boolean = false
)

data class Note(
  val id: Long = 0L,
  val title: String = "",
  val content: String = "",
  val isChecklist: Boolean = false,
  val checklistItems: List<ChecklistItem> = emptyList(),
  val colorHex: Long = 0L,
  val isEncrypted: Boolean = true,
  val isPinned: Boolean = false,
  val isArchived: Boolean = false,
  val isTrashed: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val isUnlocked: Boolean = false // transient in-memory lock state
) {
  val displayTitle: String
    get() = if (isEncrypted && !isUnlocked) {
      if (title.isNotEmpty()) title else "Encrypted Vault Note"
    } else {
      if (title.isNotBlank()) title else "Untitled Note"
    }

  val displayContent: String
    get() = if (isEncrypted && !isUnlocked) {
      "🔒 Protected with 256-bit AES-GCM encryption. Tap to authenticate with Biometrics."
    } else {
      content
    }

  val wordCount: Int
    get() = if (content.isBlank() || (isEncrypted && !isUnlocked)) 0
    else content.trim().split("\\s+".toRegex()).size

  val formattedDate: String
    get() {
      val sdf = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
      return sdf.format(Date(updatedAt))
    }

  val formattedDateShort: String
    get() {
      val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
      return sdf.format(Date(updatedAt))
    }

  val checklistProgress: Pair<Int, Int>?
    get() {
      if (isEncrypted && !isUnlocked) return null
      if (isChecklist && checklistItems.isNotEmpty()) {
        val total = checklistItems.size
        val completed = checklistItems.count { it.isChecked }
        return Pair(completed, total)
      }
      val lines = content.lines()
      val total = lines.count { it.startsWith("- [ ]") || it.startsWith("- [x]") || it.startsWith("- [X]") }
      if (total == 0) return null
      val completed = lines.count { it.startsWith("- [x]") || it.startsWith("- [X]") }
      return Pair(completed, total)
    }

  companion object {
    fun serializeChecklist(items: List<ChecklistItem>): String {
      return items.joinToString("\n") { item ->
        val check = if (item.isChecked) "[x]" else "[ ]"
        "- $check ${item.text}"
      }
    }

    fun parseChecklist(content: String): List<ChecklistItem> {
      if (content.isBlank()) return emptyList()
      return content.lines().mapNotNull { line ->
        val trimmed = line.trim()
        when {
          trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") -> {
            ChecklistItem(
              text = trimmed.substring(6),
              isChecked = true
            )
          }
          trimmed.startsWith("- [ ] ") -> {
            ChecklistItem(
              text = trimmed.substring(6),
              isChecked = false
            )
          }
          trimmed.isNotEmpty() -> {
            ChecklistItem(
              text = trimmed,
              isChecked = false
            )
          }
          else -> null
        }
      }
    }
  }
}
