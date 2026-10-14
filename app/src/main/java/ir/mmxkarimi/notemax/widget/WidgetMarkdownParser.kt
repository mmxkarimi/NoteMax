package ir.mmxkarimi.notemax.widget

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.QuoteSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan

object WidgetMarkdownParser {

  fun parse(markdown: String): CharSequence {
    if (markdown.isBlank()) return ""

    val ssb = SpannableStringBuilder()
    val lines = markdown.lines()
    var inCodeBlock = false
    val codeBlockBuilder = StringBuilder()

    lines.forEachIndexed { index, rawLine ->
      val trimmed = rawLine.trim()

      if (trimmed.startsWith("```")) {
        if (inCodeBlock) {
          // Finish code block
          val start = ssb.length
          val codeText = codeBlockBuilder.toString().trimEnd()
          ssb.append(codeText).append("\n")
          val end = ssb.length - 1
          if (end > start) {
            ssb.setSpan(TypefaceSpan("monospace"), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSpan(BackgroundColorSpan(0x22888888), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          }
          codeBlockBuilder.clear()
          inCodeBlock = false
        } else {
          inCodeBlock = true
          codeBlockBuilder.clear()
        }
        return@forEachIndexed
      }

      if (inCodeBlock) {
        codeBlockBuilder.append(rawLine).append("\n")
        return@forEachIndexed
      }

      when {
        // Horizontal rule
        trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
          val start = ssb.length
          ssb.append("──────────────\n")
          val end = ssb.length - 1
          ssb.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Heading 1
        trimmed.startsWith("# ") -> {
          val text = trimmed.removePrefix("# ")
          val start = ssb.length
          appendInlineFormatted(ssb, text)
          val end = ssb.length
          ssb.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          ssb.setSpan(RelativeSizeSpan(1.35f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          ssb.append("\n\n")
        }

        // Heading 2
        trimmed.startsWith("## ") -> {
          val text = trimmed.removePrefix("## ")
          val start = ssb.length
          appendInlineFormatted(ssb, text)
          val end = ssb.length
          ssb.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          ssb.setSpan(RelativeSizeSpan(1.2f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          ssb.append("\n\n")
        }

        // Heading 3
        trimmed.startsWith("### ") -> {
          val text = trimmed.removePrefix("### ")
          val start = ssb.length
          appendInlineFormatted(ssb, text)
          val end = ssb.length
          ssb.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          ssb.setSpan(RelativeSizeSpan(1.1f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          ssb.append("\n\n")
        }

        // Checklist uncompleted
        trimmed.startsWith("- [ ] ") || trimmed.startsWith("* [ ] ") -> {
          val itemText = trimmed.substring(6)
          ssb.append("☐ ")
          appendInlineFormatted(ssb, itemText)
          ssb.append("\n")
        }

        // Checklist completed
        trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") ||
          trimmed.startsWith("* [x] ") || trimmed.startsWith("* [X] ") -> {
          val itemText = trimmed.substring(6)
          val start = ssb.length
          ssb.append("☑ ")
          val contentStart = ssb.length
          appendInlineFormatted(ssb, itemText)
          val end = ssb.length
          ssb.setSpan(StrikethroughSpan(), contentStart, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          ssb.append("\n")
        }

        // Bullet list
        trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
          val itemText = trimmed.substring(2)
          ssb.append("• ")
          appendInlineFormatted(ssb, itemText)
          ssb.append("\n")
        }

        // Numbered list
        trimmed.matches("^\\d+\\.\\s.*".toRegex()) -> {
          val dotIndex = trimmed.indexOf(". ")
          val num = trimmed.substring(0, dotIndex + 2)
          val itemText = trimmed.substring(dotIndex + 2)
          val start = ssb.length
          ssb.append(num)
          val numEnd = ssb.length
          ssb.setSpan(StyleSpan(Typeface.BOLD), start, numEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          appendInlineFormatted(ssb, itemText)
          ssb.append("\n")
        }

        // Quote block
        trimmed.startsWith("> ") -> {
          val quoteText = trimmed.removePrefix("> ")
          val start = ssb.length
          appendInlineFormatted(ssb, quoteText)
          val end = ssb.length
          ssb.setSpan(QuoteSpan(0xFF888888.toInt()), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          ssb.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          ssb.append("\n")
        }

        // Empty line
        trimmed.isEmpty() -> {
          ssb.append("\n")
        }

        // Default paragraph
        else -> {
          appendInlineFormatted(ssb, rawLine)
          ssb.append("\n")
        }
      }
    }

    return ssb.trimEnd()
  }

  private fun appendInlineFormatted(ssb: SpannableStringBuilder, text: String) {
    var i = 0
    val len = text.length

    while (i < len) {
      when {
        // Bold: **text**
        text.startsWith("**", i) -> {
          val closing = text.indexOf("**", i + 2)
          if (closing != -1) {
            val start = ssb.length
            ssb.append(text.substring(i + 2, closing))
            val end = ssb.length
            ssb.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            i = closing + 2
          } else {
            ssb.append(text[i])
            i++
          }
        }

        // Strikethrough: ~~text~~
        text.startsWith("~~", i) -> {
          val closing = text.indexOf("~~", i + 2)
          if (closing != -1) {
            val start = ssb.length
            ssb.append(text.substring(i + 2, closing))
            val end = ssb.length
            ssb.setSpan(StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            i = closing + 2
          } else {
            ssb.append(text[i])
            i++
          }
        }

        // Inline code: `code`
        text.startsWith("`", i) -> {
          val closing = text.indexOf("`", i + 1)
          if (closing != -1) {
            val start = ssb.length
            ssb.append(text.substring(i + 1, closing))
            val end = ssb.length
            ssb.setSpan(TypefaceSpan("monospace"), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSpan(BackgroundColorSpan(0x22888888), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            i = closing + 1
          } else {
            ssb.append(text[i])
            i++
          }
        }

        // Italic: *text*
        text[i] == '*' -> {
          val closing = text.indexOf('*', i + 1)
          if (closing != -1 && !text.startsWith("**", i)) {
            val start = ssb.length
            ssb.append(text.substring(i + 1, closing))
            val end = ssb.length
            ssb.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            i = closing + 1
          } else {
            ssb.append(text[i])
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
            val start = ssb.length
            ssb.append(linkTitle)
            val end = ssb.length
            ssb.setSpan(URLSpan(validUrl), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            i = closeParen + 1
          } else {
            ssb.append(text[i])
            i++
          }
        }

        else -> {
          ssb.append(text[i])
          i++
        }
      }
    }
  }
}
