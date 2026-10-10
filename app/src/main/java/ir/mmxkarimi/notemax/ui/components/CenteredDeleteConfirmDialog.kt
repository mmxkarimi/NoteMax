package ir.mmxkarimi.notemax.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ir.mmxkarimi.notemax.R

/**
 * A delete-confirmation dialog that is always rendered in the exact center of
 * the screen. The stock platform AlertDialog inherits the device theme, which
 * on some OEM skins (e.g. One UI) places dialogs near the bottom of the
 * screen; this composable controls its own layout so it stays centered.
 */
@Composable
fun CenteredDeleteConfirmDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .padding(horizontal = 40.dp),
      contentAlignment = Alignment.Center
    ) {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        modifier = Modifier
          .widthIn(max = 340.dp)
          .fillMaxWidth()
          .testTag("confirm_delete_dialog")
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
          Text(
            text = stringResource(R.string.dialog_delete_title),
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = stringResource(R.string.dialog_delete_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 20.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            TextButton(
              onClick = onDismiss,
              modifier = Modifier.testTag("cancel_delete_button")
            ) {
              Text(text = stringResource(R.string.action_cancel))
            }
            TextButton(
              onClick = onConfirm,
              modifier = Modifier.testTag("confirm_delete_button")
            ) {
              Text(
                text = stringResource(R.string.action_delete),
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}
