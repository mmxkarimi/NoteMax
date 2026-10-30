package ir.mmxkarimi.notemax.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import ir.mmxkarimi.notemax.MainActivity
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class NoteWidgetService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
    return NoteWidgetFactory(applicationContext, intent)
  }
}

class NoteWidgetFactory(
  private val context: Context,
  private val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

  private val appWidgetId = intent.getIntExtra(
    AppWidgetManager.EXTRA_APPWIDGET_ID,
    AppWidgetManager.INVALID_APPWIDGET_ID
  )
  private var noteId: Long = 0L
  private val items = mutableListOf<WidgetItem>()

  override fun onCreate() {
    loadData()
  }

  override fun onDataSetChanged() {
    loadData()
  }

  private fun loadData() {
    items.clear()
    noteId = NoteWidgetPrefs.loadNoteId(context, appWidgetId)
    if (noteId == 0L) return

    val db = AppDatabase.getInstance(context)
    val entity = runBlocking(Dispatchers.IO) {
      db.noteDao().getNoteByIdDirect(noteId)
    }

    if (entity != null && !entity.isTrashed && entity.content.isNotBlank()) {
      val parsed = WidgetMarkdownParser.parseBlocks(entity.content)
      items.addAll(parsed)
    }
  }

  override fun onDestroy() {
    items.clear()
  }

  override fun getCount(): Int = items.size

  override fun getViewAt(position: Int): RemoteViews {
    if (position !in items.indices) {
      return RemoteViews(context.packageName, R.layout.widget_note_item)
    }

    val item = items[position]
    val rv = RemoteViews(context.packageName, R.layout.widget_note_item)
    rv.setTextViewText(R.id.widget_item_text, item.text)

    when (item.checkState) {
      CheckState.UNCHECKED -> {
        rv.setViewVisibility(R.id.widget_item_checkbox, View.VISIBLE)
        rv.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_widget_checkbox_unchecked)
      }
      CheckState.CHECKED -> {
        rv.setViewVisibility(R.id.widget_item_checkbox, View.VISIBLE)
        rv.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_widget_checkbox_checked)
      }
      CheckState.NONE -> {
        rv.setViewVisibility(R.id.widget_item_checkbox, View.GONE)
      }
    }

    // Fill-in Intent to open note when item or checkbox is tapped
    val fillInIntent = Intent().apply {
      putExtra(MainActivity.EXTRA_NOTE_ID, noteId)
    }
    rv.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent)
    rv.setOnClickFillInIntent(R.id.widget_item_checkbox, fillInIntent)

    return rv
  }

  override fun getLoadingView(): RemoteViews? = null

  override fun getViewTypeCount(): Int = 1

  override fun getItemId(position: Int): Long = position.toLong()

  override fun hasStableIds(): Boolean = false
}
