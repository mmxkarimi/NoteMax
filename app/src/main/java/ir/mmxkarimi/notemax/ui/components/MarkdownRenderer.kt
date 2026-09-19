package ir.mmxkarimi.notemax.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
          Text(
            text = renderInlineMarkdown(trimmed.removePrefix("# ")),
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp,
              color = MaterialTheme.colorScheme.onSurface,
              textDirection = TextDirection.ContentOrLtr
            ),
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
          )
        }

        // Heading 2
        trimmed.startsWith("## ") -> {
          Text(
            text = renderInlineMarkdown(trimmed.removePrefix("## ")),
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 19.sp,
              color = MaterialTheme.colorScheme.onSurface,
              textDirection = TextDirection.ContentOrLtr
            ),
            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
          )
        }

        // Heading 3
        trimmed.startsWith("### ") -> {
          Text(
            text = renderInlineMarkdown(trimmed.removePrefix("### ")),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onSurface,
              textDirection = TextDirection.ContentOrLtr
            ),
            modifier = Modifier.padding(top = 4.dp)
          )
        }

        // Checklist Item (uncompleted)
        trimmed.startsWith("- [ ] ") || trimmed.startsWith("* [ ] ") -> {
          val itemText = trimmed.substring(6)
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
              text = renderInlineMarkdown(itemText),
              style = TextStyle(
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textDirection = TextDirection.ContentOrLtr
              )
            )
          }
        }

        // Checklist Item (completed)
        trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") ||
          trimmed.startsWith("* [x] ") || trimmed.startsWith("* [X] ") -> {
          val itemText = trimmed.substring(6)
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
              text = renderInlineMarkdown(itemText),
              style = TextStyle(
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textDecoration = TextDecoration.LineThrough,
                textDirection = TextDirection.ContentOrLtr
              )
            )
          }
        }

        // Bullet Item
        trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
          val itemText = trimmed.substring(2)
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
              text = renderInlineMarkdown(itemText),
              style = TextStyle(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textDirection = TextDirection.ContentOrLtr
              )
            )
          }
        }

        // Numbered Item
        trimmed.matches("^\\d+\\.\\s.*".toRegex()) -> {
          val dotIndex = trimmed.indexOf(". ")
          val num = trimmed.substring(0, dotIndex + 1)
          val itemText = trimmed.substring(dotIndex + 2)
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
              text = renderInlineMarkdown(itemText),
              style = TextStyle(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textDirection = TextDirection.ContentOrLtr
              )
            )
          }
        }

        // Quote Block
        trimmed.startsWith("> ") -> {
          val quoteText = trimmed.removePrefix("> ")
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
              text = renderInlineMarkdown(quoteText),
              style = TextStyle(
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDirection = TextDirection.ContentOrLtr
              )
            )
          }
        }

        // Markdown Table Row
        trimmed.startsWith("|") && trimmed.endsWith("|") -> {
          if (!trimmed.contains("---")) {
            val columns = trimmed.split("|").filter { it.isNotBlank() }.map { it.trim() }
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              columns.forEach { col ->
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                  modifier = Modifier.weight(1f)
                ) {
                  Text(
                    text = renderInlineMarkdown(col),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(6.dp),
                    color = MaterialTheme.colorScheme.onSurface
                  )
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
          Text(
            text = renderInlineMarkdown(rawLine),
            style = TextStyle(
              fontSize = 15.sp,
              lineHeight = 23.sp,
              color = MaterialTheme.colorScheme.onSurface,
              textDirection = TextDirection.ContentOrLtr
            )
          )
        }
      }
    }
  }
}

/**
 * Parses basic inline markdown: **bold**, *italic*, ~~strikethrough~~, `code`, and [text](link)
 */
private fun renderInlineMarkdown(text: String) = buildAnnotatedString {
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
          withStyle(
            SpanStyle(
              color = Color(0xFF2196F3),
              textDecoration = TextDecoration.Underline
            )
          ) {
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
