package ir.mmxkarimi.notemax.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
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
  private val blocks = mutableListOf<CharSequence>()

  override fun onCreate() {
    loadData()
  }

  override fun onDataSetChanged() {
    loadData()
  }

  private fun loadData() {
    blocks.clear()
    noteId = NoteWidgetPrefs.loadNoteId(context, appWidgetId)
    if (noteId == 0L) return

    val db = AppDatabase.getInstance(context)
    val entity = runBlocking(Dispatchers.IO) {
      db.noteDao().getNoteByIdDirect(noteId)
    }

    if (entity != null && !entity.isTrashed && entity.content.isNotBlank()) {
      val parsed = WidgetMarkdownParser.parseBlocks(entity.content)
      blocks.addAll(parsed)
    }
  }

  override fun onDestroy() {
    blocks.clear()
  }

  override fun getCount(): Int = blocks.size

  override fun getViewAt(position: Int): RemoteViews {
    if (position !in blocks.indices) {
      return RemoteViews(context.packageName, R.layout.widget_note_item)
    }

    val rv = RemoteViews(context.packageName, R.layout.widget_note_item)
    rv.setTextViewText(R.id.widget_item_text, blocks[position])

    // Fill-in Intent to open note when item is tapped
    val fillInIntent = Intent().apply {
      putExtra(MainActivity.EXTRA_NOTE_ID, noteId)
    }
    rv.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent)

    return rv
  }

  override fun getLoadingView(): RemoteViews? = null

  override fun getViewTypeCount(): Int = 1

  override fun getItemId(position: Int): Long = position.toLong()

  override fun hasStableIds(): Boolean = false
}
