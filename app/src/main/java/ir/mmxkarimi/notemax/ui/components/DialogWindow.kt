package ir.mmxkarimi.notemax.ui.components

import android.view.Gravity
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

/**
 * Call from inside a Dialog/AlertDialog content. Some OEM skins (One UI) bottom-align dialog windows
 * that inherit the system dialog style; this pins the window to the centre with the Material layout.
 */
@Composable
fun CenterDialogWindow() {
  val view = LocalView.current
  SideEffect {
    val provider = (view.parent as? DialogWindowProvider) ?: (view as? DialogWindowProvider)
    provider?.window?.apply {
      setGravity(Gravity.CENTER)
      setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
  }
}
