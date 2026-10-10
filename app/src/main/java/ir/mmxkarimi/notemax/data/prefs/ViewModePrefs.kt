package ir.mmxkarimi.notemax.data.prefs

import android.content.Context
import ir.mmxkarimi.notemax.ui.viewmodel.ViewMode

/** Persists the notes view mode (grid / list) across app restarts. */
class ViewModePrefs(context: Context) {

  private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  fun loadViewMode(): ViewMode {
    val raw = prefs.getString(KEY_VIEW_MODE, null) ?: return ViewMode.GRID
    return try {
      ViewMode.valueOf(raw)
    } catch (_: IllegalArgumentException) {
      ViewMode.GRID
    }
  }

  fun saveViewMode(mode: ViewMode) {
    prefs.edit().putString(KEY_VIEW_MODE, mode.name).apply()
  }

  companion object {
    private const val PREFS_NAME = "notemax_prefs"
    private const val KEY_VIEW_MODE = "view_mode"

    @Volatile
    private var instance: ViewModePrefs? = null

    fun getInstance(context: Context): ViewModePrefs {
      return instance ?: synchronized(this) {
        instance ?: ViewModePrefs(context.applicationContext).also { instance = it }
      }
    }
  }
}
