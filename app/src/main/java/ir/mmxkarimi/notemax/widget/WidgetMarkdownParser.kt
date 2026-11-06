package ir.mmxkarimi.notemax.widget

enum class BlockKind { Heading1, Heading2, Heading3, Paragraph, Bullet, Numbered, Quote, Code, Divider, Unchecked, Checked }

/** A renderable line/block of a note for the Glance widget (inline markdown markers already stripped). */
data class WidgetBlock(val kind: BlockKind, val text: String = "")

/** Converts markdown into simple blocks. Glance text has no spans, so inline markers are stripped. */
object WidgetMarkdownParser {

  private val linkRegex = Regex("""\[([^\]]*)]\([^)]*\)""")
  private val numberedRegex = Regex("""^(\d+)\.\s+(.*)$""")

  fun stripInline(raw: String): String {
    var t = linkRegex.replace(raw) { it.groupValues[1] }
    t = t.replace("**", "").replace("~~", "").replace("`", "")
    t = t.replace(Regex("""(?<!\*)\*(?!\s)([^*]+)\*(?!\*)""")) { it.groupValues[1] }
    return t.trim()
  }

  fun parseBlocks(markdown: String): List<WidgetBlock> {
    if (markdown.isBlank()) return emptyList()
    val out = mutableListOf<WidgetBlock>()
    val paragraph = StringBuilder()
    val code = StringBuilder()
    var inCode = false

    fun flushParagraph() {
      if (paragraph.isNotBlank()) out.add(WidgetBlock(BlockKind.Paragraph, stripInline(paragraph.toString())))
      paragraph.clear()
    }

    for (rawLine in markdown.lines()) {
      val line = rawLine.trim()

      if (line.startsWith("```")) {
        if (inCode) {
          if (code.isNotBlank()) out.add(WidgetBlock(BlockKind.Code, code.toString().trimEnd()))
          code.clear()
          inCode = false
        } else {
          flushParagraph()
          inCode = true
        }
        continue
      }
      if (inCode) { code.append(rawLine).append('\n'); continue }

      when {
        line.isEmpty() -> flushParagraph()
        line == "---" || line == "***" || line == "___" -> { flushParagraph(); out.add(WidgetBlock(BlockKind.Divider)) }
        line.startsWith("- [ ]") || line.startsWith("* [ ]") -> {
          flushParagraph(); out.add(WidgetBlock(BlockKind.Unchecked, stripInline(line.drop(5))))
        }
        line.startsWith("- [x]") || line.startsWith("- [X]") || line.startsWith("* [x]") || line.startsWith("* [X]") -> {
          flushParagraph(); out.add(WidgetBlock(BlockKind.Checked, stripInline(line.drop(5))))
        }
        line.startsWith("### ") || line.startsWith("#### ") || line.startsWith("##### ") || line.startsWith("###### ") -> {
          flushParagraph(); out.add(WidgetBlock(BlockKind.Heading3, stripInline(line.trimStart('#'))))
        }
        line.startsWith("## ") -> { flushParagraph(); out.add(WidgetBlock(BlockKind.Heading2, stripInline(line.drop(3)))) }
        line.startsWith("# ") -> { flushParagraph(); out.add(WidgetBlock(BlockKind.Heading1, stripInline(line.drop(2)))) }
        line.startsWith("- ") || line.startsWith("* ") -> {
          flushParagraph(); out.add(WidgetBlock(BlockKind.Bullet, stripInline(line.drop(2))))
        }
        numberedRegex.matches(line) -> {
          flushParagraph()
          val m = numberedRegex.find(line)!!
          out.add(WidgetBlock(BlockKind.Numbered, m.groupValues[1] + ". " + stripInline(m.groupValues[2])))
        }
        line.startsWith(">") -> { flushParagraph(); out.add(WidgetBlock(BlockKind.Quote, stripInline(line.trimStart('>')))) }
        line.startsWith("|") -> { flushParagraph(); out.add(WidgetBlock(BlockKind.Paragraph, line.trim('|').split('|').joinToString("  ·  ") { it.trim() }.takeIf { !line.contains("---") } ?: "")) }
        else -> { if (paragraph.isNotEmpty()) paragraph.append(' '); paragraph.append(line) }
      }
    }
    if (inCode && code.isNotBlank()) out.add(WidgetBlock(BlockKind.Code, code.toString().trimEnd()))
    flushParagraph()
    return out.filter { it.kind == BlockKind.Divider || it.text.isNotBlank() }
  }
}
