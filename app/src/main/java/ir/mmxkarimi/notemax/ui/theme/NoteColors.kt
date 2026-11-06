package ir.mmxkarimi.notemax.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Per-note card tints. Notes persist the palette *index* in `colorHex` (0 = default surface),
 * so the same note renders with the right tint in light and dark themes.
 * All tints are low-chroma so the lime accent stays the only loud colour in the app.
 */
object NoteColors {
  private val light = listOf(
    Color(0xFFF6DCD8), // rose
    Color(0xFFF7E4C8), // amber
    Color(0xFFEAEBC0), // sand
    Color(0xFFD6EBCB), // sage
    Color(0xFFCDE9E2), // mint
    Color(0xFFD3E4F5), // sky
    Color(0xFFE2DCF3), // lavender
    Color(0xFFE6E2DA)  // stone
  )
  private val dark = listOf(
    Color(0xFF4A2C2A),
    Color(0xFF4A3A22),
    Color(0xFF40421F),
    Color(0xFF2F4429),
    Color(0xFF254139),
    Color(0xFF2B3D50),
    Color(0xFF3B3452),
    Color(0xFF3A3932)
  )

  val count: Int get() = light.size

  /** Returns the tint for [index] (1-based; 0 or out of range means "no colour"). */
  fun resolve(index: Long, darkTheme: Boolean): Color? {
    val i = index.toInt() - 1
    if (i !in light.indices) return null
    return if (darkTheme) dark[i] else light[i]
  }
}
