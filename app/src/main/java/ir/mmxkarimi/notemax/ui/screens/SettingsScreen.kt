package ir.mmxkarimi.notemax.ui.screens

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.data.settings.AppSettings
import ir.mmxkarimi.notemax.data.settings.SortMode
import ir.mmxkarimi.notemax.data.settings.ThemeMode
import ir.mmxkarimi.notemax.ui.components.CenterDialogWindow
import ir.mmxkarimi.notemax.ui.theme.Dimens

/** Grouped settings: a small section label, then rows joined into one rounded card per group. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  settings: AppSettings,
  onChange: ((AppSettings) -> AppSettings) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var dialog by remember { mutableStateOf<String?>(null) }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("settings_screen"),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.nav_settings), style = MaterialTheme.typography.titleLarge) },
        navigationIcon = { BackButton(onBack) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
      )
    }
  ) { inner ->
    val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(inner)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = Dimens.md)
        .navigationBarsPadding(),
      verticalArrangement = Arrangement.spacedBy(Dimens.xs)
    ) {
      SectionLabel(stringResource(R.string.settings_appearance))
      SettingsGroup {
        item {
          ChoiceRow(
            title = stringResource(R.string.settings_theme),
            summary = stringResource(themeLabel(settings.themeMode)),
            tag = "theme_row",
            onClick = { dialog = "theme" }
          )
        }
        if (dynamicSupported) {
          item {
            SwitchRow(
              title = stringResource(R.string.settings_dynamic_color),
              summary = stringResource(R.string.settings_dynamic_color_desc),
              checked = settings.dynamicColor,
              tag = "dynamic_color_switch",
              onCheckedChange = { c -> onChange { it.copy(dynamicColor = c) } }
            )
          }
        }
      }

      SectionLabel(stringResource(R.string.settings_notes))
      SettingsGroup {
        item {
          ChoiceRow(
            title = stringResource(R.string.settings_sort),
            summary = stringResource(sortLabel(settings.sortMode)),
            tag = "sort_row",
            onClick = { dialog = "sort" }
          )
        }
      }

      SectionLabel(stringResource(R.string.nav_trash))
      SettingsGroup {
        item {
          ChoiceRow(
            title = stringResource(R.string.settings_trash_retention),
            summary = if (settings.trashRetentionDays == 0) stringResource(R.string.retention_never)
            else stringResource(R.string.retention_days, settings.trashRetentionDays),
            tag = "retention_row",
            onClick = { dialog = "retention" }
          )
        }
      }
    }
  }

  when (dialog) {
    "theme" -> ChoiceDialog(
      title = stringResource(R.string.settings_theme),
      options = ThemeMode.entries.map { it to stringResource(themeLabel(it)) },
      selected = settings.themeMode,
      onSelect = { m -> onChange { it.copy(themeMode = m) }; dialog = null },
      onDismiss = { dialog = null }
    )
    "sort" -> ChoiceDialog(
      title = stringResource(R.string.settings_sort),
      options = SortMode.entries.map { it to stringResource(sortLabel(it)) },
      selected = settings.sortMode,
      onSelect = { m -> onChange { it.copy(sortMode = m) }; dialog = null },
      onDismiss = { dialog = null }
    )
    "retention" -> ChoiceDialog(
      title = stringResource(R.string.settings_trash_retention),
      options = listOf(7, 30, 90, 0).map {
        it to if (it == 0) stringResource(R.string.retention_never) else stringResource(R.string.retention_days, it)
      },
      selected = settings.trashRetentionDays,
      onSelect = { d -> onChange { it.copy(trashRetentionDays = d) }; dialog = null },
      onDismiss = { dialog = null }
    )
  }
}

private fun themeLabel(mode: ThemeMode) = when (mode) {
  ThemeMode.System -> R.string.theme_system
  ThemeMode.Light -> R.string.theme_light
  ThemeMode.Dark -> R.string.theme_dark
}

private fun sortLabel(mode: SortMode) = when (mode) {
  SortMode.Manual -> R.string.sort_manual
  SortMode.Edited -> R.string.sort_edited
  SortMode.Created -> R.string.sort_created
  SortMode.Title -> R.string.sort_title
}

@Composable
private fun SectionLabel(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.padding(start = Dimens.md, top = Dimens.md, bottom = Dimens.xxs)
  )
}

/** Collects rows and draws them as one card: 20dp outer corners, 4dp between rows, 2dp gap. */
private class GroupScope {
  val rows = mutableListOf<@Composable () -> Unit>()
  fun item(content: @Composable () -> Unit) { rows += content }
}

@Composable
private fun SettingsGroup(content: GroupScope.() -> Unit) {
  val scope = GroupScope().apply(content)
  Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
    scope.rows.forEachIndexed { index, row ->
      val outer = 20.dp
      val inner = 4.dp
      Surface(
        shape = RoundedCornerShape(
          topStart = if (index == 0) outer else inner,
          topEnd = if (index == 0) outer else inner,
          bottomStart = if (index == scope.rows.lastIndex) outer else inner,
          bottomEnd = if (index == scope.rows.lastIndex) outer else inner
        ),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
      ) { row() }
    }
  }
}

@Composable
private fun ChoiceRow(title: String, summary: String, tag: String, onClick: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = Dimens.md, vertical = Dimens.sm)
      .testTag(tag),
    verticalArrangement = Arrangement.spacedBy(2.dp)
  ) {
    Text(title, style = MaterialTheme.typography.bodyLarge)
    Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun SwitchRow(
  title: String,
  summary: String,
  checked: Boolean,
  tag: String,
  onCheckedChange: (Boolean) -> Unit,
  enabled: Boolean = true
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = enabled) { onCheckedChange(!checked) }
      .padding(horizontal = Dimens.md, vertical = Dimens.sm),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Dimens.md)
  ) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(title, style = MaterialTheme.typography.bodyLarge)
      Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled, modifier = Modifier.testTag(tag))
  }
}

@Composable
private fun <T> ChoiceDialog(
  title: String,
  options: List<Pair<T, String>>,
  selected: T,
  onSelect: (T) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      CenterDialogWindow()
      Column {
        options.forEach { (value, label) ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .selectable(selected = value == selected, role = Role.RadioButton, onClick = { onSelect(value) })
              .padding(vertical = Dimens.xxs),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = value == selected, onClick = null)
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = Dimens.md))
          }
        }
      }
    },
    confirmButton = {},
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
  )
}
