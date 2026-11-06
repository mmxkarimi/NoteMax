package ir.mmxkarimi.notemax.data.model

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
  val isPinned: Boolean = false,
  val isArchived: Boolean = false,
  val isTrashed: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val orderIndex: Int = 0,
  val tags: List<String> = emptyList()
) {
  val displayTitle: String
    get() = title

  val displayContent: String
    get() = content

  val wordCount: Int
    get() = if (content.isBlank()) 0
    else content.trim().split("\\s+".toRegex()).size

  val formattedDate: String
    get() {
      val sdf = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
      return sdf.format(Date(createdAt))
    }

  val formattedDateShort: String
    get() {
      val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
      return sdf.format(Date(createdAt))
    }

  val checklistProgress: Pair<Int, Int>?
    get() {
      if (isChecklist && checklistItems.isNotEmpty()) {
        val total = checklistItems.size
        val completed = checklistItems.count { it.isChecked }
        return Pair(completed, total)
      }
      val lines = content.lines()
      val total = lines.count { line ->
        val t = line.trim()
        t.startsWith("- [ ]") || t.startsWith("- [x]") || t.startsWith("- [X]") ||
          t.startsWith("* [ ]") || t.startsWith("* [x]") || t.startsWith("* [X]")
      }
      if (total == 0) return null
      val completed = lines.count { line ->
        val t = line.trim()
        t.startsWith("- [x]") || t.startsWith("- [X]") ||
          t.startsWith("* [x]") || t.startsWith("* [X]")
      }
      return Pair(completed, total)
    }

  companion object {
    fun parseTags(raw: String): List<String> =
      raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }.distinct()

    fun serializeTags(tags: List<String>): String =
      tags.map { it.trim().lowercase().replace(",", "") }.filter { it.isNotEmpty() }.distinct().joinToString(",")

    fun serializeChecklist(items: List<ChecklistItem>): String {
      return items.joinToString("\n") { item ->
        val check = if (item.isChecked) "[x]" else "[ ]"
        "- $check ${item.text}"
      }
    }

    fun parseChecklist(content: String): List<ChecklistItem> {
      if (content.isBlank()) return emptyList()
      val lines = content.lines()
      val hasChecklist = lines.any { line ->
        val t = line.trim()
        t.startsWith("- [ ]") || t.startsWith("- [x]") || t.startsWith("- [X]") ||
          t.startsWith("* [ ]") || t.startsWith("* [x]") || t.startsWith("* [X]")
      }
      if (!hasChecklist) return emptyList()

      return lines.mapNotNull { line ->
        val trimmed = line.trim()
        when {
          trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") ||
            trimmed.startsWith("* [x] ") || trimmed.startsWith("* [X] ") -> {
            ChecklistItem(
              text = trimmed.substring(6),
              isChecked = true
            )
          }
          trimmed.startsWith("- [ ] ") || trimmed.startsWith("* [ ] ") -> {
            ChecklistItem(
              text = trimmed.substring(6),
              isChecked = false
            )
          }
          else -> null
        }
      }
    }
  }
}
