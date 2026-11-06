package ir.mmxkarimi.notemax.data.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { System, Light, Dark }

enum class SortMode { Manual, Edited, Created, Title }

data class AppSettings(
  val themeMode: ThemeMode = ThemeMode.System,
  val dynamicColor: Boolean = false,
  val gridView: Boolean = true,
  val sortMode: SortMode = SortMode.Manual,
  val trashRetentionDays: Int = 30
)

/** Tiny SharedPreferences-backed settings holder exposed as a [StateFlow]. */
class SettingsStore(context: Context) {
  private val prefs = context.applicationContext.getSharedPreferences("notemax_settings", Context.MODE_PRIVATE)

  private val _settings = MutableStateFlow(read())
  val settings: StateFlow<AppSettings> = _settings.asStateFlow()

  private fun read(): AppSettings = AppSettings(
    themeMode = runCatching { ThemeMode.valueOf(prefs.getString("theme", null) ?: "System") }.getOrDefault(ThemeMode.System),
    dynamicColor = prefs.getBoolean("dynamic", false),
    gridView = prefs.getBoolean("grid", true),
    sortMode = runCatching { SortMode.valueOf(prefs.getString("sort", null) ?: "Manual") }.getOrDefault(SortMode.Manual),
    trashRetentionDays = prefs.getInt("trash_days", 30)
  )

  fun update(transform: (AppSettings) -> AppSettings) {
    val next = transform(_settings.value)
    prefs.edit()
      .putString("theme", next.themeMode.name)
      .putBoolean("dynamic", next.dynamicColor)
      .putBoolean("grid", next.gridView)
      .putString("sort", next.sortMode.name)
      .putInt("trash_days", next.trashRetentionDays)
      .apply()
    _settings.value = next
  }
}
