package ir.mmxkarimi.notemax.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Widget provider: renders each widget directly in the broadcast (no WorkManager session). */
class NoteGlanceWidgetReceiver : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    NoteWidgetProvider.renderAsync(context, appWidgetIds, goAsync())
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle
  ) {
    NoteWidgetProvider.renderAsync(context, intArrayOf(appWidgetId), goAsync())
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    appWidgetIds.forEach { NoteWidgetPrefs.deleteWidget(context, it) }
  }
}

/** Entry point used by the app to refresh every placed widget after a note changes. */
object NoteWidgetProvider {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  fun updateAllWidgets(context: Context) {
    val app = context.applicationContext
    val ids = AppWidgetManager.getInstance(app)
      .getAppWidgetIds(ComponentName(app, NoteGlanceWidgetReceiver::class.java))
    if (ids.isNotEmpty()) renderAsync(app, ids, null)
  }

  /** Renders [ids] now; suspend-friendly variant used by the configure screen. */
  suspend fun render(context: Context, ids: IntArray) {
    val app = context.applicationContext
    val manager = AppWidgetManager.getInstance(app)
    ids.forEach { id ->
      val views = NoteGlanceWidget.render(app, id, manager.getAppWidgetOptions(id))
      manager.updateAppWidget(id, views)
    }
  }

  internal fun renderAsync(
    context: Context,
    ids: IntArray,
    pending: android.content.BroadcastReceiver.PendingResult?
  ) {
    val app = context.applicationContext
    scope.launch {
      try {
        // Broadcasts must finish within ~10s; rendering takes milliseconds.
        withTimeoutOrNull(8_000) { render(app, ids) }
      } finally {
        pending?.finish()
      }
    }
  }
}
