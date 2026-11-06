package ir.mmxkarimi.notemax.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyStaggeredGridState
import ir.mmxkarimi.notemax.data.settings.SortMode
import androidx.compose.material3.SnackbarDuration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.data.model.Note
import ir.mmxkarimi.notemax.data.settings.AppSettings
import ir.mmxkarimi.notemax.ui.components.ColorSheet
import ir.mmxkarimi.notemax.ui.components.EmptyState
import ir.mmxkarimi.notemax.ui.components.NoteCard
import ir.mmxkarimi.notemax.ui.components.NoteSkeletonLoading
import ir.mmxkarimi.notemax.ui.components.UnifiedFloatingMenuBar
import ir.mmxkarimi.notemax.ui.theme.Dimens
import ir.mmxkarimi.notemax.ui.viewmodel.NotesUiState
import kotlinx.coroutines.launch

private val IdSetSaver = listSaver<Set<Long>, Long>(save = { it.toList() }, restore = { it.toSet() })

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  uiState: NotesUiState,
  settings: AppSettings,
  onNoteClick: (Note) -> Unit,
  onNewNote: () -> Unit,
  onOpenSearch: () -> Unit,
  onOpenArchive: () -> Unit,
  onOpenTrash: () -> Unit,
  onOpenSettings: () -> Unit,
  onToggleView: () -> Unit,
  onToggleChecklistItem: (Note, Int) -> Unit,
  onBulkTrash: (List<Note>) -> Unit,
  onBulkRestore: (List<Note>) -> Unit,
  onBulkArchive: (List<Note>, Boolean) -> Unit,
  onBulkPin: (List<Note>, Boolean) -> Unit,
  onBulkColor: (List<Note>, Long) -> Unit,
  onReorderNotes: (List<Note>) -> Unit,
  modifier: Modifier = Modifier
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val haptics = LocalHapticFeedback.current

  var selectedIds by rememberSaveable(stateSaver = IdSetSaver) { mutableStateOf(emptySet<Long>()) }
  var activeTag by rememberSaveable { mutableStateOf<String?>(null) }
  var menuOpen by remember { mutableStateOf(false) }
  var showColorSheet by remember { mutableStateOf(false) }

  val effectiveTag = activeTag?.takeIf { it in uiState.allTags }
  val visible = remember(uiState.activeNotes, effectiveTag) {
    if (effectiveTag == null) uiState.activeNotes else uiState.activeNotes.filter { effectiveTag in it.tags }
  }
  // Drop selections that no longer exist (e.g. after trash/archive).
  val selectedNotes = visible.filter { it.id in selectedIds }
  val selectionMode = selectedNotes.isNotEmpty()

  BackHandler(enabled = selectionMode) { selectedIds = emptySet() }

  fun toggleSelect(note: Note) {
    selectedIds = if (note.id in selectedIds) selectedIds - note.id else selectedIds + note.id
  }

  val trashedMsg = stringResource(R.string.snackbar_moved_to_trash)
  val archivedMsg = stringResource(R.string.snackbar_archived)
  val undoLabel = stringResource(R.string.action_undo)

  val gridState = rememberLazyStaggeredGridState()
  var localNotes by remember { mutableStateOf(visible) }
  var dragging by remember { mutableStateOf(false) }
  androidx.compose.runtime.LaunchedEffect(visible) { if (!dragging) localNotes = visible }
  // Notes can be dragged while in select mode, in manual order and without a tag filter.
  val canReorder = selectionMode && settings.sortMode == SortMode.Manual && effectiveTag == null
  val reorderState = rememberReorderableLazyStaggeredGridState(gridState) { from, to ->
    val fromId = noteIdFromKey(from.key)
    val toId = noteIdFromKey(to.key)
    if (fromId != null && toId != null) {
      val list = localNotes.toMutableList()
      val fi = list.indexOfFirst { it.id == fromId }
      val ti = list.indexOfFirst { it.id == toId }
      if (fi >= 0 && ti >= 0 && list[fi].isPinned == list[ti].isPinned) {
        list.add(ti, list.removeAt(fi))
        localNotes = list
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      }
    }
  }

  val navBottom = with(LocalDensity.current) { WindowInsets.navigationBars.getBottom(this).toDp() }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .testTag("home_screen"),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    snackbarHost = {
      SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(bottom = 80.dp + navBottom)
      )
    },
    topBar = {
      if (selectionMode) {
        TopAppBar(
          title = {
            Text(
              text = pluralStringResource(R.plurals.selected_count, selectedNotes.size, selectedNotes.size),
              style = MaterialTheme.typography.titleLarge
            )
          },
          navigationIcon = {
            IconButton(onClick = { selectedIds = emptySet() }, modifier = Modifier.testTag("selection_close")) {
              Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel))
            }
          },
          actions = {
            val allPinned = selectedNotes.all { it.isPinned }
            IconButton(onClick = { onBulkPin(selectedNotes, !allPinned); selectedIds = emptySet() }) {
              Icon(
                if (allPinned) Icons.Outlined.PushPin else Icons.Default.PushPin,
                contentDescription = stringResource(if (allPinned) R.string.action_unpin else R.string.action_pin)
              )
            }
            IconButton(onClick = { showColorSheet = true }) {
              Icon(Icons.Default.Palette, contentDescription = stringResource(R.string.options_color))
            }
            IconButton(onClick = {
              val batch = selectedNotes
              onBulkArchive(batch, true)
              selectedIds = emptySet()
              scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val r = snackbarHostState.showSnackbar(archivedMsg, actionLabel = undoLabel, duration = SnackbarDuration.Short)
                if (r == SnackbarResult.ActionPerformed) onBulkArchive(batch, false)
              }
            }) {
              Icon(Icons.Default.Archive, contentDescription = stringResource(R.string.action_archive))
            }
            IconButton(onClick = {
              val batch = selectedNotes
              onBulkTrash(batch)
              selectedIds = emptySet()
              scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val r = snackbarHostState.showSnackbar(trashedMsg, actionLabel = undoLabel, duration = SnackbarDuration.Short)
                if (r == SnackbarResult.ActionPerformed) onBulkRestore(batch)
              }
            }, modifier = Modifier.testTag("selection_trash")) {
              Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_remove), tint = MaterialTheme.colorScheme.error)
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        )
      } else {
        TopAppBar(
          title = {
            Text(
              text = stringResource(R.string.app_name),
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.testTag("home_app_title")
            )
          },
          actions = {
            IconButton(onClick = onToggleView, modifier = Modifier.testTag("toggle_view_button")) {
              Icon(
                imageVector = if (settings.gridView) Icons.Default.ViewAgenda else Icons.Default.ViewModule,
                contentDescription = stringResource(if (settings.gridView) R.string.view_list else R.string.view_grid)
              )
            }
            Box {
              IconButton(onClick = { menuOpen = true }, modifier = Modifier.testTag("home_menu_button")) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.action_options))
              }
              DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                  text = { Text(stringResource(R.string.nav_archive)) },
                  leadingIcon = { Icon(Icons.Default.Archive, null) },
                  onClick = { menuOpen = false; onOpenArchive() }
                )
                DropdownMenuItem(
                  text = { Text(stringResource(R.string.nav_trash)) },
                  leadingIcon = { Icon(Icons.Default.Delete, null) },
                  onClick = { menuOpen = false; onOpenTrash() }
                )
                DropdownMenuItem(
                  text = { Text(stringResource(R.string.nav_settings)) },
                  leadingIcon = { Icon(Icons.Default.Settings, null) },
                  onClick = { menuOpen = false; onOpenSettings() }
                )
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
          )
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = innerPadding.calculateTopPadding())
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.allTags.isNotEmpty() && !uiState.isLoading) {
          TagFilterRow(
            tags = uiState.allTags,
            selected = effectiveTag,
            onSelect = { activeTag = it }
          )
        }

        when {
          uiState.isLoading -> NoteSkeletonLoading(contentPadding = PaddingValues(Dimens.md))

          uiState.activeNotes.isEmpty() -> EmptyState(
            icon = Icons.AutoMirrored.Filled.NoteAdd,
            title = stringResource(R.string.no_notes_yet),
            description = stringResource(R.string.no_notes_description),
            modifier = Modifier.testTag("home_empty_state")
          )

          visible.isEmpty() -> EmptyState(
            icon = Icons.Default.FilterAlt,
            title = stringResource(R.string.no_filter_results),
            description = stringResource(R.string.no_filter_results_description),
            actionLabel = stringResource(R.string.filter_clear),
            onAction = { activeTag = null }
          )

          else -> {
            val pinned = localNotes.filter { it.isPinned }
            val others = localNotes.filter { !it.isPinned }
            LazyVerticalStaggeredGrid(
              state = gridState,
              columns = StaggeredGridCells.Fixed(if (settings.gridView) 2 else 1),
              modifier = Modifier
                .fillMaxSize()
                .testTag("notes_list_view"),
              contentPadding = PaddingValues(
                start = Dimens.md, end = Dimens.md, top = Dimens.xs,
                bottom = 88.dp + navBottom
              ),
              horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
              verticalItemSpacing = Dimens.sm
            ) {
              if (pinned.isNotEmpty()) {
                item(key = "header_pinned", span = StaggeredGridItemSpan.FullLine) {
                  SectionHeader(stringResource(R.string.pinned_section))
                }
                items(pinned, key = { "note_${it.id}" }) { note ->
                  ReorderableItem(reorderState, key = "note_${note.id}") { _ ->
                    HomeCard(
                      note, selectedIds, selectionMode, ::toggleSelect, onNoteClick, onToggleChecklistItem,
                      haptics::performHapticFeedback,
                      Modifier.longPressDraggableHandle(
                        enabled = canReorder,
                        onDragStarted = { dragging = true; haptics.performHapticFeedback(HapticFeedbackType.LongPress) },
                        onDragStopped = { dragging = false; onReorderNotes(localNotes) }
                      )
                    )
                  }
                }
                if (others.isNotEmpty()) {
                  item(key = "header_others", span = StaggeredGridItemSpan.FullLine) {
                    SectionHeader(stringResource(R.string.others_section))
                  }
                }
              }
              items(others, key = { "note_${it.id}" }) { note ->
                ReorderableItem(reorderState, key = "note_${note.id}") { _ ->
                  HomeCard(
                    note, selectedIds, selectionMode, ::toggleSelect, onNoteClick, onToggleChecklistItem,
                    haptics::performHapticFeedback,
                    Modifier.longPressDraggableHandle(
                      enabled = canReorder,
                      onDragStarted = { dragging = true; haptics.performHapticFeedback(HapticFeedbackType.LongPress) },
                      onDragStopped = { dragging = false; onReorderNotes(localNotes) }
                    )
                  )
                }
              }
            }
          }
        }
      }

      // Bottom dock, over a soft fade so scrolled content never collides with it.
      AnimatedVisibility(
        visible = !selectionMode && !uiState.isLoading,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.92f), MaterialTheme.colorScheme.background)
              )
            )
            .padding(top = Dimens.lg)
        ) {
          UnifiedFloatingMenuBar(onSearchClick = onOpenSearch, onCreateNote = onNewNote)
        }
      }
    }
  }

  if (showColorSheet) {
    ColorSheet(
      selected = selectedNotes.map { it.colorHex }.distinct().singleOrNull() ?: -1L,
      onSelect = { color ->
        onBulkColor(selectedNotes, color)
        showColorSheet = false
        selectedIds = emptySet()
      },
      onDismiss = { showColorSheet = false }
    )
  }
}

@Composable
private fun HomeCard(
  note: Note,
  selectedIds: Set<Long>,
  selectionMode: Boolean,
  onToggleSelect: (Note) -> Unit,
  onNoteClick: (Note) -> Unit,
  onToggleChecklistItem: (Note, Int) -> Unit,
  haptic: (HapticFeedbackType) -> Unit,
  modifier: Modifier = Modifier
) {
  NoteCard(
    modifier = modifier,
    note = note,
    selected = note.id in selectedIds,
    selectionMode = selectionMode,
    onClick = { if (selectionMode) onToggleSelect(note) else onNoteClick(note) },
    onLongClick = if (selectionMode) null else {
      {
        haptic(HapticFeedbackType.LongPress)
        onToggleSelect(note)
      }
    },
    onToggleChecklistItem = { idx -> onToggleChecklistItem(note, idx) }
  )
}

@Composable
private fun SectionHeader(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.padding(horizontal = Dimens.xxs, vertical = Dimens.xxs)
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagFilterRow(tags: List<String>, selected: String?, onSelect: (String?) -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = Dimens.md, vertical = Dimens.xs)
      .testTag("tag_filter_row"),
    horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
  ) {
    FilterChip(
      selected = selected == null,
      onClick = { onSelect(null) },
      label = { Text(stringResource(R.string.filter_all)) }
    )
    tags.forEach { tag ->
      FilterChip(
        selected = selected == tag,
        onClick = { onSelect(if (selected == tag) null else tag) },
        label = { Text("#$tag") }
      )
    }
  }
}

private fun noteIdFromKey(key: Any): Long? = (key as? String)?.removePrefix("note_")?.takeIf { key.startsWith("note_") }?.toLongOrNull()
