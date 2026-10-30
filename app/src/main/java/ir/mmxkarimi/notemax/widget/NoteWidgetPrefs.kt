package ir.mmxkarimi.notemax.widget

import android.content.Context

object NoteWidgetPrefs {
  private const val PREFS_NAME = "notemax_widget_prefs"
  private const val KEY_PREFIX = "widget_note_"

  fun saveNoteId(context: Context, appWidgetId: Int, noteId: Long) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putLong("$KEY_PREFIX$appWidgetId", noteId).apply()
  }

  fun loadNoteId(context: Context, appWidgetId: Int): Long {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getLong("$KEY_PREFIX$appWidgetId", 0L)
  }

  fun deleteWidget(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().remove("$KEY_PREFIX$appWidgetId").apply()
  }

  fun getWidgetIdsForNote(context: Context, noteId: Long): List<Int> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val widgetIds = mutableListOf<Int>()
    for ((key, value) in prefs.all) {
      if (key.startsWith(KEY_PREFIX) && value == noteId) {
        val idStr = key.removePrefix(KEY_PREFIX)
        idStr.toIntOrNull()?.let { widgetIds.add(it) }
      }
    }
    return widgetIds
  }
}
