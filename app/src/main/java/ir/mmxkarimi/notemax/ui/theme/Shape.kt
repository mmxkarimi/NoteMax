package ir.mmxkarimi.notemax.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * The one shape scale. Rule:
 *  - chips, inputs, small tiles ........ 12dp  (small)
 *  - note cards, menus ................. 20dp  (medium / large)
 *  - sheets and dialogs ................ 28dp  (extraLarge)
 *  - actions (FAB, search pill, tags) .. fully rounded (Pill)
 */
val NoteMaxShapes = Shapes(
  extraSmall = RoundedCornerShape(8.dp),
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(20.dp),
  large = RoundedCornerShape(20.dp),
  extraLarge = RoundedCornerShape(28.dp)
)

val Pill = CircleShape
val CardShape = RoundedCornerShape(20.dp)
val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
