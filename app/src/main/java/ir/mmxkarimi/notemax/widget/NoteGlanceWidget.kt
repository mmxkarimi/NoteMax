package ir.mmxkarimi.notemax.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.RemoteViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.ColorFilter
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import ir.mmxkarimi.notemax.MainActivity
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.data.db.AppDatabase
import ir.mmxkarimi.notemax.ui.theme.NoteColors

private val SurfaceDefault = Color(0xFF1E211A)
private val TextPrimary = Color(0xFFE3E3DC)
private val TextSecondary = Color(0xFF909282)
private val Accent = Color(0xFFC6F135)

/**
 * Home-screen widget that shows one note (markdown rendered) on the note's own colour.
 *
 * The UI is written with Glance composables but rendered straight to RemoteViews with
 * [GlanceRemoteViews], so updates never depend on Glance's WorkManager sessions (which could
 * leave the launcher stuck on the loading spinner on some devices).
 */
@OptIn(ExperimentalGlanceRemoteViewsApi::class)
object NoteGlanceWidget {

  private const val MAX_BLOCKS = 40

  suspend fun render(context: Context, appWidgetId: Int, options: Bundle): RemoteViews = try {
    renderContent(context, appWidgetId, options)
  } catch (t: Throwable) {
    errorViews(context, t)
  }

  fun errorViews(context: Context, t: Throwable): RemoteViews =
    RemoteViews(context.packageName, R.layout.widget_error).apply {
      setTextViewText(R.id.widget_error_text, "${t.javaClass.simpleName}: ${t.message.orEmpty()}".take(240))
    }

  private fun sizeOf(context: Context, options: Bundle): DpSize {
    val portrait = context.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
    val w = options.getInt(if (portrait) AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH else AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
    val h = options.getInt(if (portrait) AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT else AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
    return DpSize((if (w > 0) w else 180).dp, (if (h > 0) h else 110).dp)
  }

  private suspend fun renderContent(context: Context, appWidgetId: Int, options: Bundle): RemoteViews {
    val noteId = NoteWidgetPrefs.loadNoteId(context, appWidgetId)
    val entity = withContext(Dispatchers.IO) {
      if (noteId != 0L) AppDatabase.getInstance(context).noteDao().getNoteByIdDirect(noteId) else null
    }
    val note = entity?.takeIf { !it.isTrashed }

    val title = note?.title?.takeIf { it.isNotBlank() } ?: context.getString(
      if (note == null) R.string.app_name else R.string.untitled_note
    )
    val blocks = note?.content?.let { WidgetMarkdownParser.parseBlocks(it) }.orEmpty().take(MAX_BLOCKS)
    val tint = NoteColors.resolve(note?.colorHex ?: 0L, darkTheme = true) ?: SurfaceDefault

    val action = if (note != null) {
      actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
          action = Intent.ACTION_VIEW
          data = android.net.Uri.parse("notemax://note/${note.id}")
          putExtra(MainActivity.EXTRA_NOTE_ID, note.id)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
      )
    } else {
      actionStartActivity(
        Intent(context, NoteWidgetConfigureActivity::class.java).apply {
          data = android.net.Uri.parse("notemax://widget/$appWidgetId")
          putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
      )
    }

    val emptyText = context.getString(
      if (note == null) R.string.widget_select_note else R.string.widget_empty_content
    )

    return GlanceRemoteViews().compose(context, sizeOf(context, options), appWidgetOptions = options) {
      Box(modifier = GlanceModifier.fillMaxSize().clickable(action)) {
        // Rounded background image, tinted with the note colour.
        Image(
          provider = ImageProvider(R.drawable.widget_background),
          contentDescription = null,
          contentScale = ContentScale.FillBounds,
          colorFilter = ColorFilter.tint(ColorProvider(tint)),
          modifier = GlanceModifier.fillMaxSize()
        )
        Column(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
          Text(
            text = title,
            maxLines = 1,
            style = TextStyle(color = ColorProvider(TextPrimary), fontSize = 15.sp, fontWeight = FontWeight.Bold)
          )
          Spacer(GlanceModifier.height(8.dp))
          if (blocks.isEmpty()) {
            Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text(text = emptyText, style = TextStyle(color = ColorProvider(TextSecondary), fontSize = 13.sp))
            }
          } else {
            // Plain column (no collection adapter): content beyond the widget height is clipped.
            Column(modifier = GlanceModifier.fillMaxSize()) {
              blocks.forEach { BlockRow(it) }
            }
          }
        }
      }
    }.remoteViews
  }
}

@androidx.compose.runtime.Composable
private fun BlockRow(block: WidgetBlock) {
  val rowModifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp)
  when (block.kind) {
    BlockKind.Heading1 -> Text(block.text, modifier = rowModifier, style = TextStyle(color = ColorProvider(TextPrimary), fontSize = 18.sp, fontWeight = FontWeight.Bold))
    BlockKind.Heading2 -> Text(block.text, modifier = rowModifier, style = TextStyle(color = ColorProvider(TextPrimary), fontSize = 16.sp, fontWeight = FontWeight.Bold))
    BlockKind.Heading3 -> Text(block.text, modifier = rowModifier, style = TextStyle(color = ColorProvider(TextPrimary), fontSize = 14.sp, fontWeight = FontWeight.Bold))
    BlockKind.Paragraph -> Text(block.text, modifier = rowModifier, style = TextStyle(color = ColorProvider(TextPrimary), fontSize = 13.sp))
    BlockKind.Bullet -> Text("•  ${block.text}", modifier = rowModifier, style = TextStyle(color = ColorProvider(TextPrimary), fontSize = 13.sp))
    BlockKind.Numbered -> Text(block.text, modifier = rowModifier, style = TextStyle(color = ColorProvider(TextPrimary), fontSize = 13.sp))
    BlockKind.Quote -> Text(block.text, modifier = rowModifier, style = TextStyle(color = ColorProvider(TextSecondary), fontSize = 13.sp, fontStyle = FontStyle.Italic))
    BlockKind.Code -> Text(block.text, modifier = rowModifier, style = TextStyle(color = ColorProvider(Accent), fontSize = 12.sp, fontFamily = FontFamily.Monospace))
    BlockKind.Divider -> Box(modifier = rowModifier.height(1.dp).background(ColorProvider(TextSecondary.copy(alpha = 0.4f)))) {}
    BlockKind.Unchecked, BlockKind.Checked -> {
      val checked = block.kind == BlockKind.Checked
      Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
        Image(
          provider = ImageProvider(if (checked) R.drawable.ic_widget_checkbox_checked else R.drawable.ic_widget_checkbox_unchecked),
          contentDescription = null,
          modifier = GlanceModifier.size(18.dp)
        )
        Spacer(GlanceModifier.width(8.dp))
        Text(
          text = block.text,
          style = TextStyle(
            color = ColorProvider(if (checked) TextSecondary else TextPrimary),
            fontSize = 13.sp,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
          )
        )
      }
    }
  }
}
