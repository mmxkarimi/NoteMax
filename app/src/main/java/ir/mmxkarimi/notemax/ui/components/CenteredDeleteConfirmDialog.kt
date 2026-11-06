package ir.mmxkarimi.notemax.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import ir.mmxkarimi.notemax.R

/** Standard Material confirm dialog for destructive actions. */
@Composable
fun ConfirmDialog(
  title: String,
  message: String,
  confirmLabel: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = modifier.testTag("confirm_delete_dialog"),
    title = { Text(title) },
    text = { CenterDialogWindow(); Text(message) },
    confirmButton = {
      TextButton(onClick = onConfirm, modifier = Modifier.testTag("confirm_delete_button")) {
        Text(confirmLabel, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_delete_button")) {
        Text(stringResource(R.string.action_cancel))
      }
    }
  )
}

/** "Delete this note permanently?" with the app's default copy. */
@Composable
fun CenteredDeleteConfirmDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  ConfirmDialog(
    title = stringResource(R.string.dialog_delete_title),
    message = stringResource(R.string.dialog_delete_message),
    confirmLabel = stringResource(R.string.action_delete),
    onConfirm = onConfirm,
    onDismiss = onDismiss,
    modifier = modifier
  )
}
