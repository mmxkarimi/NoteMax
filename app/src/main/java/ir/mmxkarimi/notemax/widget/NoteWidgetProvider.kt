package ir.mmxkarimi.notemax.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import ir.mmxkarimi.notemax.MainActivity
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.data.db.AppDatabase
import ir.mmxkarimi.notemax.data.model.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NoteWidgetProvider : AppWidgetProvider() {

  private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    for (appWidgetId in appWidgetIds) {
      updateWidgetAsync(context, appWidgetManager, appWidgetId)
    }
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    for (appWidgetId in appWidgetIds) {
      NoteWidgetPrefs.deleteWidget(context, appWidgetId)
    }
  }

  private fun updateWidgetAsync(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
  ) {
    coroutineScope.launch {
      val noteId = NoteWidgetPrefs.loadNoteId(context, appWidgetId)
      val views = RemoteViews(context.packageName, R.layout.widget_note_view)

      if (noteId == 0L) {
        showEmptyWidget(context, views, appWidgetId, appWidgetManager)
        return@launch
      }

      val db = AppDatabase.getInstance(context)
      val noteEntity = db.noteDao().getNoteByIdDirect(noteId)

      if (noteEntity == null || noteEntity.isTrashed) {
        showEmptyWidget(context, views, appWidgetId, appWidgetManager)
        return@launch
      }

      val note = Note(
        id = noteEntity.id,
        title = noteEntity.title,
        content = noteEntity.content,
        createdAt = noteEntity.createdAt,
        updatedAt = noteEntity.updatedAt
      )

      val titleText = if (note.title.isNotBlank()) note.title else context.getString(R.string.untitled_note)
      views.setTextViewText(R.id.widget_title, titleText)
      views.setTextViewText(R.id.widget_date, note.formattedDateShort)

      val parsedMarkdown = WidgetMarkdownParser.parse(note.content)
      val contentText = if (parsedMarkdown.isNotEmpty()) {
        parsedMarkdown
      } else {
        context.getString(R.string.widget_empty_content)
      }
      views.setTextViewText(R.id.widget_content, contentText)

      views.setViewVisibility(R.id.widget_scroll_view, View.VISIBLE)
      views.setViewVisibility(R.id.widget_empty_container, View.GONE)

      // Click on note body opens MainActivity directly into Note Editor
      val openNoteIntent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(MainActivity.EXTRA_NOTE_ID, note.id)
      }
      val openPendingIntent = PendingIntent.getActivity(
        context,
        appWidgetId,
        openNoteIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      views.setOnClickPendingIntent(R.id.widget_scroll_view, openPendingIntent)
      views.setOnClickPendingIntent(R.id.widget_title, openPendingIntent)

      // Click on switch button reopens NoteWidgetConfigureActivity
      val switchIntent = Intent(context, NoteWidgetConfigureActivity::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
      val switchPendingIntent = PendingIntent.getActivity(
        context,
        100000 + appWidgetId,
        switchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      views.setOnClickPendingIntent(R.id.widget_btn_switch, switchPendingIntent)

      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }

  private fun showEmptyWidget(
    context: Context,
    views: RemoteViews,
    appWidgetId: Int,
    appWidgetManager: AppWidgetManager
  ) {
    views.setTextViewText(R.id.widget_title, context.getString(R.string.app_name))
    views.setTextViewText(R.id.widget_date, "")
    views.setViewVisibility(R.id.widget_scroll_view, View.GONE)
    views.setViewVisibility(R.id.widget_empty_container, View.VISIBLE)

    val configureIntent = Intent(context, NoteWidgetConfigureActivity::class.java).apply {
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val configurePendingIntent = PendingIntent.getActivity(
      context,
      appWidgetId,
      configureIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_root, configurePendingIntent)
    views.setOnClickPendingIntent(R.id.widget_empty_container, configurePendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
  }

  companion object {
    fun updateAllWidgets(context: Context) {
      val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
      val thisWidget = ComponentName(context, NoteWidgetProvider::class.java)
      val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget) ?: return
      if (appWidgetIds.isNotEmpty()) {
        val intent = Intent(context, NoteWidgetProvider::class.java).apply {
          action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
          putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(intent)
      }
    }
  }
}
