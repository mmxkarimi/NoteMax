package ir.mmxkarimi.notemax.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Detects whether the primary direction of [text] is Right-to-Left (e.g. Persian / Arabic / Hebrew).
 */
fun isRtlText(text: String): Boolean {
  for (char in text) {
    val dir = Character.getDirectionality(char)
    if (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
      dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
    ) {
      return true
    } else if (dir == Character.DIRECTIONALITY_LEFT_TO_RIGHT) {
      return false
    }
  }
  return false
}

@Composable
fun MarkdownRenderer(
  markdownText: String,
  onToggleTask: ((Int, Boolean) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  // If the text is blank, show exactly nothing as requested
  if (markdownText.isBlank()) {
    Box(modifier = modifier)
    return
  }

  val uriHandler = LocalUriHandler.current
  val lines = markdownText.lines()
  var inCodeBlock = false
  val codeBlockBuffer = StringBuilder()

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    lines.forEachIndexed { index, rawLine ->
      val trimmed = rawLine.trim()

      if (trimmed.startsWith("```")) {
        if (inCodeBlock) {
          // End of code block
          val code = codeBlockBuffer.toString().trimEnd()
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Text(
              text = code,
              fontFamily = FontFamily.Monospace,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(12.dp)
            )
          }
          codeBlockBuffer.clear()
          inCodeBlock = false
        } else {
          inCodeBlock = true
          codeBlockBuffer.clear()
        }
        return@forEachIndexed
      }

      if (inCodeBlock) {
        codeBlockBuffer.append(rawLine).append("\n")
        return@forEachIndexed
      }

      when {
        // Horizontal Rule
        trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
          HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 8.dp)
          )
        }

        // Heading 1
        trimmed.startsWith("# ") -> {
          val headingText = trimmed.removePrefix("# ")
          val isRtl = isRtlText(headingText)
          Text(
            text = renderInlineMarkdown(headingText, uriHandler),
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp,
              color = MaterialTheme.colorScheme.onSurface,
              textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp, bottom = 4.dp)
          )
        }

        // Heading 2
        trimmed.startsWith("## ") -> {
          val headingText = trimmed.removePrefix("## ")
          val isRtl = isRtlText(headingText)
          Text(
            text = renderInlineMarkdown(headingText, uriHandler),
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 19.sp,
              color = MaterialTheme.colorScheme.onSurface,
              textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 6.dp, bottom = 2.dp)
          )
        }

        // Heading 3
        trimmed.startsWith("### ") -> {
          val headingText = trimmed.removePrefix("### ")
          val isRtl = isRtlText(headingText)
          Text(
            text = renderInlineMarkdown(headingText, uriHandler),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onSurface,
              textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp)
          )
        }

        // Checklist Item (uncompleted)
        trimmed.startsWith("- [ ] ") || trimmed.startsWith("* [ ] ") -> {
          val itemText = trimmed.substring(6)
          val isRtl = isRtlText(itemText)
          CompositionLocalProvider(
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .clickable { onToggleTask?.invoke(index, true) }
                .padding(vertical = 2.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CheckBoxOutlineBlank,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = renderInlineMarkdown(itemText, uriHandler),
                style = TextStyle(
                  fontSize = 15.sp,
                  color = MaterialTheme.colorScheme.onSurface,
                  textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                ),
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Checklist Item (completed)
        trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") ||
          trimmed.startsWith("* [x] ") || trimmed.startsWith("* [X] ") -> {
          val itemText = trimmed.substring(6)
          val isRtl = isRtlText(itemText)
          CompositionLocalProvider(
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .clickable { onToggleTask?.invoke(index, false) }
                .padding(vertical = 2.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CheckBox,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = renderInlineMarkdown(itemText, uriHandler),
                style = TextStyle(
                  fontSize = 15.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                  textDecoration = TextDecoration.LineThrough,
                  textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                ),
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Bullet Item
        trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
          val itemText = trimmed.substring(2)
          val isRtl = isRtlText(itemText)
          CompositionLocalProvider(
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, top = 2.dp, bottom = 2.dp),
              verticalAlignment = Alignment.Top
            ) {
              Box(
                modifier = Modifier
                  .padding(top = 8.dp)
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = renderInlineMarkdown(itemText, uriHandler),
                style = TextStyle(
                  fontSize = 15.sp,
                  lineHeight = 22.sp,
                  color = MaterialTheme.colorScheme.onSurface,
                  textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                ),
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Numbered Item
        trimmed.matches("^\\d+\\.\\s.*".toRegex()) -> {
          val dotIndex = trimmed.indexOf(". ")
          val num = trimmed.substring(0, dotIndex + 1)
          val itemText = trimmed.substring(dotIndex + 2)
          val isRtl = isRtlText(itemText)
          CompositionLocalProvider(
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
              verticalAlignment = Alignment.Top
            ) {
              Text(
                text = num,
                style = TextStyle(
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.width(24.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = renderInlineMarkdown(itemText, uriHandler),
                style = TextStyle(
                  fontSize = 15.sp,
                  lineHeight = 22.sp,
                  color = MaterialTheme.colorScheme.onSurface,
                  textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                ),
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Quote Block
        trimmed.startsWith("> ") -> {
          val quoteText = trimmed.removePrefix("> ")
          val isRtl = isRtlText(quoteText)
          CompositionLocalProvider(
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            ) {
              Box(
                modifier = Modifier
                  .width(4.dp)
                  .height(24.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(MaterialTheme.colorScheme.primary)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = renderInlineMarkdown(quoteText, uriHandler),
                style = TextStyle(
                  fontSize = 15.sp,
                  fontStyle = FontStyle.Italic,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                ),
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Markdown Table Row
        trimmed.startsWith("|") && trimmed.endsWith("|") -> {
          if (!trimmed.contains("---")) {
            val columns = trimmed.split("|").filter { it.isNotBlank() }.map { it.trim() }
            val isRtl = columns.any { isRtlText(it) }
            CompositionLocalProvider(
              LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                columns.forEach { col ->
                  val colRtl = isRtlText(col)
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                  ) {
                    Text(
                      text = renderInlineMarkdown(col, uriHandler),
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Medium,
                      modifier = Modifier.padding(6.dp),
                      color = MaterialTheme.colorScheme.onSurface,
                      style = TextStyle(
                        textDirection = if (colRtl) TextDirection.Rtl else TextDirection.Ltr
                      )
                    )
                  }
                }
              }
            }
          }
        }

        // Standard Paragraph or empty line
        trimmed.isEmpty() -> {
          Spacer(modifier = Modifier.height(4.dp))
        }

        else -> {
          val isRtl = isRtlText(rawLine)
          Text(
            text = renderInlineMarkdown(rawLine, uriHandler),
            style = TextStyle(
              fontSize = 15.sp,
              lineHeight = 23.sp,
              color = MaterialTheme.colorScheme.onSurface,
              textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}

/**
 * Compact Markdown Preview suitable for note cards and search results.
 */
@Composable
fun MarkdownPreview(
  markdownText: String,
  modifier: Modifier = Modifier,
  maxLines: Int = 5
) {
  if (markdownText.isBlank()) return

  val uriHandler = LocalUriHandler.current
  val lines = markdownText.lines().filter { it.isNotBlank() }.take(maxLines)

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(3.dp)
  ) {
    lines.forEach { rawLine ->
      val trimmed = rawLine.trim()
      when {
        // Divider line
        trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
          HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            modifier = Modifier.padding(vertical = 2.dp)
          )
        }

        // Heading 1
        trimmed.startsWith("# ") -> {
          val headingText = trimmed.removePrefix("# ")
          val isRtl = isRtlText(headingText)
          Text(
            text = renderInlineMarkdown(headingText, uriHandler),
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface,
              textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Heading 2
        trimmed.startsWith("## ") -> {
          val headingText = trimmed.removePrefix("## ")
          val isRtl = isRtlText(headingText)
          Text(
            text = renderInlineMarkdown(headingText, uriHandler),
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 13.5.sp,
              color = MaterialTheme.colorScheme.onSurface,
              textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Heading 3
        trimmed.startsWith("### ") -> {
          val headingText = trimmed.removePrefix("### ")
          val isRtl = isRtlText(headingText)
          Text(
            text = renderInlineMarkdown(headingText, uriHandler),
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface,
              textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Checklist Item (uncompleted)
        trimmed.startsWith("- [ ] ") || trimmed.startsWith("* [ ] ") -> {
          val itemText = trimmed.substring(6)
          val isRtl = isRtlText(itemText)
          CompositionLocalProvider(
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Default.CheckBoxOutlineBlank,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = renderInlineMarkdown(itemText, uriHandler),
                style = TextStyle(
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurface,
                  textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Checklist Item (completed)
        trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") ||
          trimmed.startsWith("* [x] ") || trimmed.startsWith("* [X] ") -> {
          val itemText = trimmed.substring(6)
          val isRtl = isRtlText(itemText)
          CompositionLocalProvider(
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Default.CheckBox,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = renderInlineMarkdown(itemText, uriHandler),
                style = TextStyle(
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                  textDecoration = TextDecoration.LineThrough,
                  textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Bullet Item
        trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
          val itemText = trimmed.substring(2)
          val isRtl = isRtlText(itemText)
          CompositionLocalProvider(
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.Top
            ) {
              Box(
                modifier = Modifier
                  .padding(top = 6.dp)
                  .size(5.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = renderInlineMarkdown(itemText, uriHandler),
                style = TextStyle(
                  fontSize = 13.sp,
                  lineHeight = 18.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Numbered Item
        trimmed.matches("^\\d+\\.\\s.*".toRegex()) -> {
          val dotIndex = trimmed.indexOf(". ")
          val num = trimmed.substring(0, dotIndex + 1)
          val itemText = trimmed.substring(dotIndex + 2)
          val isRtl = isRtlText(itemText)
          CompositionLocalProvider(
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.Top
            ) {
              Text(
                text = num,
                style = TextStyle(
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.width(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = renderInlineMarkdown(itemText, uriHandler),
                style = TextStyle(
                  fontSize = 13.sp,
                  lineHeight = 18.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Quote Block
        trimmed.startsWith("> ") -> {
          val quoteText = trimmed.removePrefix("> ")
          val isRtl = isRtlText(quoteText)
          CompositionLocalProvider(
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .width(3.dp)
                  .height(16.dp)
                  .clip(RoundedCornerShape(1.5.dp))
                  .background(MaterialTheme.colorScheme.primary)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = renderInlineMarkdown(quoteText, uriHandler),
                style = TextStyle(
                  fontSize = 13.sp,
                  fontStyle = FontStyle.Italic,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Table row (supports homepage table preview)
        trimmed.startsWith("|") && trimmed.endsWith("|") -> {
          if (!trimmed.contains("---")) {
            val columns = trimmed.split("|").filter { it.isNotBlank() }.map { it.trim() }
            val isRtl = columns.any { isRtlText(it) }
            CompositionLocalProvider(
              LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                columns.forEach { col ->
                  val colRtl = isRtlText(col)
                  Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.weight(1f)
                  ) {
                    Text(
                      text = renderInlineMarkdown(col, uriHandler),
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Medium,
                      color = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      style = TextStyle(
                        textDirection = if (colRtl) TextDirection.Rtl else TextDirection.Ltr
                      )
                    )
                  }
                }
              }
            }
          }
        }

        // Code block line / fence
        trimmed.startsWith("```") -> {
          val codeText = trimmed.removePrefix("```").trim()
          if (codeText.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = codeText,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        // Standard Paragraph
        else -> {
          val isRtl = isRtlText(rawLine)
          Text(
            text = renderInlineMarkdown(rawLine, uriHandler),
            style = TextStyle(
              fontSize = 13.sp,
              lineHeight = 18.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
            ),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}

/**
 * Parses basic inline markdown: **bold**, *italic*, ~~strikethrough~~, `code`, and [text](link)
 */
fun renderInlineMarkdown(text: String, uriHandler: UriHandler? = null) = buildAnnotatedString {
  var i = 0
  val len = text.length

  while (i < len) {
    when {
      // Bold: **text**
      text.startsWith("**", i) -> {
        val closing = text.indexOf("**", i + 2)
        if (closing != -1) {
          withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(text.substring(i + 2, closing))
          }
          i = closing + 2
        } else {
          append(text[i])
          i++
        }
      }

      // Strikethrough: ~~text~~
      text.startsWith("~~", i) -> {
        val closing = text.indexOf("~~", i + 2)
        if (closing != -1) {
          withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
            append(text.substring(i + 2, closing))
          }
          i = closing + 2
        } else {
          append(text[i])
          i++
        }
      }

      // Inline code: `code`
      text.startsWith("`", i) -> {
        val closing = text.indexOf("`", i + 1)
        if (closing != -1) {
          withStyle(
            SpanStyle(
              fontFamily = FontFamily.Monospace,
              background = Color(0x1F888888),
              fontSize = 13.sp
            )
          ) {
            append(text.substring(i + 1, closing))
          }
          i = closing + 1
        } else {
          append(text[i])
          i++
        }
      }

      // Italic: *text* (single asterisk)
      text[i] == '*' -> {
        val closing = text.indexOf('*', i + 1)
        if (closing != -1 && !text.startsWith("**", i)) {
          withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
            append(text.substring(i + 1, closing))
          }
          i = closing + 1
        } else {
          append(text[i])
          i++
        }
      }

      // Link: [title](url)
      text[i] == '[' -> {
        val closeBracket = text.indexOf(']', i)
        val openParen = if (closeBracket != -1 && closeBracket + 1 < len && text[closeBracket + 1] == '(') closeBracket + 1 else -1
        val closeParen = if (openParen != -1) text.indexOf(')', openParen) else -1

        if (closeParen != -1) {
          val linkTitle = text.substring(i + 1, closeBracket)
          val rawUrl = text.substring(openParen + 1, closeParen).trim()
          val validUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://") && !rawUrl.startsWith("mailto:")) {
            "https://$rawUrl"
          } else {
            rawUrl
          }
          val link = LinkAnnotation.Url(
            url = validUrl,
            styles = TextLinkStyles(
              style = SpanStyle(
                color = Color(0xFF2196F3),
                textDecoration = TextDecoration.Underline
              )
            ),
            linkInteractionListener = uriHandler?.let { handler ->
              LinkInteractionListener {
                try {
                  handler.openUri(validUrl)
                } catch (_: Exception) {
                }
              }
            }
          )
          withLink(link) {
            append(linkTitle)
          }
          i = closeParen + 1
        } else {
          append(text[i])
          i++
        }
      }

      else -> {
        append(text[i])
        i++
      }
    }
  }
}
