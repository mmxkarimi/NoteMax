package ir.mmxkarimi.notemax.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.data.model.Note
import ir.mmxkarimi.notemax.ui.theme.CardShape
import ir.mmxkarimi.notemax.ui.theme.Dimens
import ir.mmxkarimi.notemax.ui.theme.NoteColors
import ir.mmxkarimi.notemax.ui.theme.Pill
import ir.mmxkarimi.notemax.ui.theme.isAppDark

/**
 * A note in the Home / Archive / Trash / Search lists.
 *
 * Tap opens, long-press starts multi-select. Press feedback is a quick spring scale (0.97) so the
 * card feels physical without ever blocking the tap.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
  note: Note,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  onLongClick: (() -> Unit)? = null,
  onToggleChecklistItem: ((Int) -> Unit)? = null,
  selected: Boolean = false,
  selectionMode: Boolean = false,
  maxPreviewLines: Int = 5,
  footer: (@Composable ColumnScope.() -> Unit)? = null
) {
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (pressed) 0.97f else 1f,
    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
    label = "card_press"
  )

  val tint = NoteColors.resolve(note.colorHex, isAppDark())
  val container = tint ?: MaterialTheme.colorScheme.surfaceContainer
  val border = when {
    selected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    else -> null
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .graphicsLayer { scaleX = scale; scaleY = scale }
      .clip(CardShape)
      .background(container)
      .let { if (border != null) it.then(Modifier.cardBorder(border)) else it }
      .combinedClickable(
        interactionSource = interaction,
        indication = LocalIndication.current,
        onClick = onClick,
        onLongClick = onLongClick
      )
      .testTag("note_card_${note.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(Dimens.md),
      verticalArrangement = Arrangement.spacedBy(Dimens.xs)
    ) {
      if (note.title.isNotBlank()) {
        val isTitleRtl = isRtlText(note.title)
        Text(
          text = note.title,
          style = MaterialTheme.typography.titleMedium.copy(
            textDirection = if (isTitleRtl) TextDirection.Rtl else TextDirection.Ltr
          ),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .fillMaxWidth()
            .padding(end = if (selectionMode || note.isPinned) Dimens.lg else 0.dp)
        )
      }

      val parsedChecklist = remember(note.content) { Note.parseChecklist(note.content) }
      if (parsedChecklist.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.xxs)) {
          parsedChecklist.take(4).forEachIndexed { idx, item ->
            val isRtl = isRtlText(item.text)
            CompositionLocalProvider(
              LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable(enabled = onToggleChecklistItem != null && !selectionMode) {
                    onToggleChecklistItem?.invoke(idx)
                  }
              ) {
                Icon(
                  imageVector = if (item.isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                  contentDescription = null,
                  tint = if (item.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(Dimens.xs))
                Text(
                  text = renderInlineMarkdown(item.text),
                  style = MaterialTheme.typography.bodyMedium.copy(
                    textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                  ),
                  color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                  else MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
          if (parsedChecklist.size > 4) {
            Text(
              text = stringResource(R.string.more_items_count, parsedChecklist.size - 4),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else if (note.content.isNotBlank()) {
        MarkdownPreview(markdownText = note.content, maxLines = maxPreviewLines)
      }

      note.checklistProgress?.let { (completed, total) ->
        if (total > 0) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
              progress = { completed.toFloat() / total.toFloat() },
              modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(Pill),
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
              gapSize = 0.dp,
              drawStopIndicator = {}
            )
            Spacer(Modifier.width(Dimens.xs))
            Text(
              text = "$completed/$total",
              style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      if (note.tags.isNotEmpty()) {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.xxs), verticalAlignment = Alignment.CenterVertically) {
          note.tags.take(2).forEach { TagPill(it) }
          if (note.tags.size > 2) {
            Text(
              text = "+${note.tags.size - 2}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Text(
        text = note.formattedDateShort,
        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      footer?.invoke(this)
    }

    // Top-end status: selection check, or pin marker.
    Box(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(Dimens.sm)
    ) {
      when {
        selectionMode -> {
          Box(
            modifier = Modifier
              .size(22.dp)
              .clip(CircleShape)
              .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
              .cardBorder(
                BorderStroke(
                  1.5.dp,
                  if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            if (selected) {
              Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
        note.isPinned -> Icon(
          imageVector = Icons.Default.PushPin,
          contentDescription = stringResource(R.string.action_unpin),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

@Composable
fun TagPill(tag: String, modifier: Modifier = Modifier) {
  Text(
    text = "#$tag",
    style = MaterialTheme.typography.labelSmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = modifier
      .clip(Pill)
      .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
      .padding(horizontal = Dimens.xs, vertical = 2.dp)
  )
}

private fun Modifier.cardBorder(
  border: BorderStroke,
  shape: androidx.compose.ui.graphics.Shape = CardShape
): Modifier = this.then(Modifier.border(border, shape))
