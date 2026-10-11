package ir.mmxkarimi.notemax.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.data.model.Note
import ir.mmxkarimi.notemax.ui.components.MarkdownPreview
import ir.mmxkarimi.notemax.ui.components.NoteCard
import ir.mmxkarimi.notemax.ui.components.UnifiedFloatingMenuBar
import ir.mmxkarimi.notemax.ui.viewmodel.NotesUiState
import ir.mmxkarimi.notemax.ui.viewmodel.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  uiState: NotesUiState,
  onToggleViewMode: () -> Unit,
  onNoteClick: (Note) -> Unit,
  onNewNote: () -> Unit,
  onTogglePin: (Note) -> Unit,
  onRemove: (Note) -> Unit,
  onRestore: (Note) -> Unit,
  onDeletePermanently: (Note) -> Unit,
  onToggleChecklistItem: (Note, Int) -> Unit,
  onReorderNotes: (List<Note>) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()
  val gridState = rememberLazyStaggeredGridState()
  val listState = rememberLazyListState()
  val haptics = LocalHapticFeedback.current

  var showSearchSheet by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  // Drag and Drop reordering state
  var draggingNoteId by remember { mutableStateOf<Long?>(null) }
  // True after the finger lifts, while the card is still gliding into its slot.
  var isDragReleased by remember { mutableStateOf(false) }
  var dragOffsetX by remember { mutableFloatStateOf(0f) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }
  var settleJob by remember { mutableStateOf<Job?>(null) }
  var localNotes by remember(uiState.activeNotes) { mutableStateOf(uiState.activeNotes) }
  val isGridMode = uiState.viewMode == ViewMode.GRID

  LaunchedEffect(uiState.activeNotes) {
    if (draggingNoteId == null) {
      localNotes = uiState.activeNotes
    }
  }

  val onDragStart: (Long) -> Unit = { noteId ->
    settleJob?.cancel()
    draggingNoteId = noteId
    isDragReleased = false
    dragOffsetX = 0f
    dragOffsetY = 0f
    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  val onDragFinished: () -> Unit = finish@{
    if (draggingNoteId == null || isDragReleased) return@finish

    if (localNotes != uiState.activeNotes) {
      onReorderNotes(localNotes)
    }

    // Glide the card into its new slot instead of snapping it there.
    isDragReleased = true
    val startX = dragOffsetX
    val startY = dragOffsetY
    settleJob?.cancel()
    settleJob = coroutineScope.launch {
      animate(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioNoBouncy,
          stiffness = Spring.StiffnessMedium
        )
      ) { progress, _ ->
        dragOffsetX = startX * progress
        dragOffsetY = startY * progress
      }
      dragOffsetX = 0f
      dragOffsetY = 0f
      draggingNoteId = null
      isDragReleased = false
    }
  }

  /** Swaps the dragged note with the one it is hovering over (grid mode). */
  val swapInGrid: (Long) -> Unit = swap@{ noteId ->
    val visibleItems = gridState.layoutInfo.visibleItemsInfo
    val fromItemInfo = visibleItems.firstOrNull {
      it.key == "pinned_$noteId" || it.key == "note_$noteId"
    } ?: return@swap

    val centerX = fromItemInfo.offset.x + fromItemInfo.size.width / 2f + dragOffsetX
    val centerY = fromItemInfo.offset.y + fromItemInfo.size.height / 2f + dragOffsetY
    val fromNote = localNotes.firstOrNull { it.id == noteId } ?: return@swap

    val pad = 16f
    val targetItemInfo = visibleItems.firstOrNull { itemInfo ->
      val candidateId = itemInfo.key.toString()
        .removePrefix("pinned_").removePrefix("note_").toLongOrNull()
      if (candidateId != null && candidateId != noteId) {
        val candidateNote = localNotes.firstOrNull { it.id == candidateId }
        if (candidateNote != null && candidateNote.isPinned == fromNote.isPinned) {
          centerX in (itemInfo.offset.x - pad)..(itemInfo.offset.x + itemInfo.size.width + pad) &&
            centerY in (itemInfo.offset.y - pad)..(itemInfo.offset.y + itemInfo.size.height + pad)
        } else false
      } else false
    } ?: return@swap

    val targetId = targetItemInfo.key.toString()
      .removePrefix("pinned_").removePrefix("note_").toLongOrNull() ?: return@swap
    val fullList = localNotes.toMutableList()
    val fromIdx = fullList.indexOfFirst { it.id == noteId }
    val toIdx = fullList.indexOfFirst { it.id == targetId }
    if (fromIdx != -1 && toIdx != -1 && fromIdx != toIdx) {
      fullList.add(toIdx, fullList.removeAt(fromIdx))
      localNotes = fullList

      // Keep the card under the finger while the slots change.
      dragOffsetX -= targetItemInfo.offset.x - fromItemInfo.offset.x
      dragOffsetY -= targetItemInfo.offset.y - fromItemInfo.offset.y
      haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
  }

  /** Swaps the dragged note with the one it is hovering over (list mode). */
  val swapInList: (Long) -> Unit = swap@{ noteId ->
    val visibleItems = listState.layoutInfo.visibleItemsInfo
    val fromItemInfo = visibleItems.firstOrNull { it.key == "list_$noteId" } ?: return@swap

    val centerY = fromItemInfo.offset + fromItemInfo.size / 2f + dragOffsetY
    val fromNote = localNotes.firstOrNull { it.id == noteId } ?: return@swap

    val pad = 16
    val targetItemInfo = visibleItems.firstOrNull { itemInfo ->
      val candidateId = itemInfo.key.toString().removePrefix("list_").toLongOrNull()
      if (candidateId != null && candidateId != noteId) {
        val candidateNote = localNotes.firstOrNull { it.id == candidateId }
        if (candidateNote != null && candidateNote.isPinned == fromNote.isPinned) {
          centerY >= itemInfo.offset - pad && centerY <= itemInfo.offset + itemInfo.size + pad
        } else false
      } else false
    } ?: return@swap

    val targetId = targetItemInfo.key.toString().removePrefix("list_").toLongOrNull() ?: return@swap
    val fullList = localNotes.toMutableList()
    val fromIdx = fullList.indexOfFirst { it.id == noteId }
    val toIdx = fullList.indexOfFirst { it.id == targetId }
    if (fromIdx != -1 && toIdx != -1 && fromIdx != toIdx) {
      fullList.add(toIdx, fullList.removeAt(fromIdx))
      localNotes = fullList

      dragOffsetY -= targetItemInfo.offset - fromItemInfo.offset
      haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
  }

  val onDragGrid: (Long, Offset) -> Unit = { noteId, dragAmount ->
    dragOffsetX += dragAmount.x
    dragOffsetY += dragAmount.y
    swapInGrid(noteId)
  }

  val onDragList: (Long, Offset) -> Unit = { noteId, dragAmount ->
    dragOffsetX += dragAmount.x
    dragOffsetY += dragAmount.y
    swapInList(noteId)
  }

  // Smooth auto-scroll: runs every frame while a card is held near the top or bottom edge,
  // even when the finger is not moving. Speed grows the closer the card gets to the edge.
  LaunchedEffect(draggingNoteId, isDragReleased, isGridMode) {
    val noteId = draggingNoteId ?: return@LaunchedEffect
    if (isDragReleased) return@LaunchedEffect

    while (isActive) {
      val viewport = (
        if (isGridMode) gridState.layoutInfo.viewportSize.height
        else listState.layoutInfo.viewportSize.height
        ).toFloat()

      val centerY: Float? = if (isGridMode) {
        gridState.layoutInfo.visibleItemsInfo
          .firstOrNull { it.key == "pinned_$noteId" || it.key == "note_$noteId" }
          ?.let { it.offset.y + it.size.height / 2f + dragOffsetY }
      } else {
        listState.layoutInfo.visibleItemsInfo
          .firstOrNull { it.key == "list_$noteId" }
          ?.let { it.offset + it.size / 2f + dragOffsetY }
      }

      if (centerY != null && viewport > 0f) {
        val delta = autoScrollSpeed(centerY, viewport)
        if (delta != 0f) {
          val consumed = if (isGridMode) gridState.scrollBy(delta) else listState.scrollBy(delta)
          // Content moved under the card, so move the card with it to stay under the finger.
          dragOffsetY += consumed
          if (isGridMode) swapInGrid(noteId) else swapInList(noteId)
        }
      }
      withFrameNanos { }
    }
  }

  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

  // Search Bottom Sheet Dialog
  if (showSearchSheet) {
    ModalBottomSheet(
      onDismissRequest = {
        showSearchSheet = false
        searchQuery = ""
      },
      sheetState = sheetState,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      modifier = Modifier.testTag("search_bottom_sheet")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
          .padding(bottom = 24.dp)
      ) {
        // Search Input Bar with circular corners
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text(stringResource(R.string.search_notes_placeholder)) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = stringResource(R.string.search_notes),
              tint = MaterialTheme.colorScheme.primary
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = stringResource(R.string.search_clear),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          },
          singleLine = true,
          shape = CircleShape,
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_text_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Results
        val trimmedQuery = searchQuery.trim()
        val searchResults = remember(trimmedQuery, uiState.activeNotes) {
          if (trimmedQuery.isEmpty()) {
            emptyList()
          } else {
            uiState.activeNotes.filter { note ->
              note.title.contains(trimmedQuery, ignoreCase = true) ||
                note.content.contains(trimmedQuery, ignoreCase = true)
            }
          }
        }

        if (trimmedQuery.isNotEmpty() && searchResults.isEmpty()) {
          // No Results State
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 40.dp)
              .testTag("empty_search_results"),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(44.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = stringResource(R.string.no_search_results),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = stringResource(R.string.no_search_results_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        } else {
          val displayList = if (trimmedQuery.isEmpty()) uiState.activeNotes.take(3) else searchResults
          if (displayList.isNotEmpty()) {
            Text(
              text = if (trimmedQuery.isEmpty()) {
                stringResource(R.string.search_recent_notes, displayList.size)
              } else {
                stringResource(R.string.search_results_count, displayList.size)
              },
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              ),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(vertical = 8.dp)
            )
          }

          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .fillMaxHeight(0.7f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(displayList, key = { "search_${it.id}" }) { note ->
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .clickable {
                    showSearchSheet = false
                    searchQuery = ""
                    onNoteClick(note)
                  }
                  .testTag("search_result_item_${note.id}")
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = if (note.title.isNotBlank()) note.title else stringResource(R.string.untitled_note),
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.weight(1f)
                    )
                    Text(
                      text = note.formattedDateShort,
                      style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  if (note.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    MarkdownPreview(
                      markdownText = note.content,
                      maxLines = 2
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .nestedScroll(scrollBehavior.nestedScrollConnection)
      .testTag("home_screen"),
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    containerColor = MaterialTheme.colorScheme.background,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      LargeTopAppBar(
        title = {
          Text(
            text = stringResource(R.string.app_name),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.testTag("home_app_title")
          )
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.background,
          scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        scrollBehavior = scrollBehavior
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = innerPadding.calculateTopPadding())
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
        val currentNotes = localNotes

        if (currentNotes.isEmpty()) {
          // Empty State
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Box(
                modifier = Modifier
                  .size(72.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(36.dp)
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = stringResource(R.string.no_notes_yet),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = stringResource(R.string.no_notes_description),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        } else {
          val pinnedNotes = remember(localNotes) { localNotes.filter { it.isPinned } }
          val unpinnedNotes = remember(localNotes) { localNotes.filter { !it.isPinned } }

          // Grid or List View
          if (uiState.viewMode == ViewMode.GRID) {
            LazyVerticalStaggeredGrid(
              columns = StaggeredGridCells.Fixed(2),
              state = gridState,
              modifier = Modifier
                .fillMaxSize()
                .testTag("notes_staggered_grid"),
              contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalItemSpacing = 10.dp
            ) {
              // Pinned Notes Section Header
              if (pinnedNotes.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                  Text(
                    text = stringResource(R.string.pinned_section),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.ExtraBold,
                      letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                  )
                }

                items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                  val isDragging = note.id == draggingNoteId
                  val itemModifier = rememberReorderModifier(
                    noteId = note.id,
                    draggingNoteId = draggingNoteId,
                    isReleased = isDragReleased,
                    offsetX = { dragOffsetX },
                    offsetY = { dragOffsetY },
                    placement = Modifier.animateItem(placementSpec = ReorderPlacementSpec),
                    onDragStart = onDragStart,
                    onDrag = onDragGrid,
                    onDragEnd = onDragFinished
                  )

                  NoteCard(
                    note = note,
                    onClick = { onNoteClick(note) },
                    onTogglePin = { onTogglePin(note) },
                    onRemove = { onRemove(note) },
                    onRestore = { onRestore(note) },
                    onDeletePermanently = { onDeletePermanently(note) },
                    onToggleChecklistItem = { idx -> onToggleChecklistItem(note, idx) },
                    isDragging = isDragging && !isDragReleased,
                    modifier = itemModifier
                  )
                }

                if (unpinnedNotes.isNotEmpty()) {
                  item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                      text = stringResource(R.string.others_section),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                      ),
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
                    )
                  }
                }
              }

              val listToRender = if (pinnedNotes.isNotEmpty()) unpinnedNotes else currentNotes

              items(listToRender, key = { "note_${it.id}" }) { note ->
                val isDragging = note.id == draggingNoteId
                val itemModifier = rememberReorderModifier(
                  noteId = note.id,
                  draggingNoteId = draggingNoteId,
                  isReleased = isDragReleased,
                  offsetX = { dragOffsetX },
                  offsetY = { dragOffsetY },
                  placement = Modifier.animateItem(placementSpec = ReorderPlacementSpec),
                  onDragStart = onDragStart,
                  onDrag = onDragGrid,
                  onDragEnd = onDragFinished
                )

                NoteCard(
                  note = note,
                  onClick = { onNoteClick(note) },
                  onTogglePin = { onTogglePin(note) },
                  onRemove = { onRemove(note) },
                  onRestore = { onRestore(note) },
                  onDeletePermanently = { onDeletePermanently(note) },
                  onToggleChecklistItem = { idx -> onToggleChecklistItem(note, idx) },
                  isDragging = isDragging && !isDragReleased,
                  modifier = itemModifier
                )
              }
            }
          } else {
            // List View
            LazyColumn(
              state = listState,
              modifier = Modifier
                .fillMaxSize()
                .testTag("notes_list_view"),
              contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              if (pinnedNotes.isNotEmpty()) {
                item(key = "header_pinned") {
                  Text(
                    text = stringResource(R.string.pinned_section),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.ExtraBold,
                      letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                  )
                }

                items(pinnedNotes, key = { "list_${it.id}" }) { note ->
                  val isDragging = note.id == draggingNoteId
                  val itemModifier = rememberReorderModifier(
                    noteId = note.id,
                    draggingNoteId = draggingNoteId,
                    isReleased = isDragReleased,
                    offsetX = { dragOffsetX },
                    offsetY = { dragOffsetY },
                    placement = Modifier.animateItem(placementSpec = ReorderPlacementSpec),
                    onDragStart = onDragStart,
                    onDrag = onDragList,
                    onDragEnd = onDragFinished
                  )

                  NoteCard(
                    note = note,
                    onClick = { onNoteClick(note) },
                    onTogglePin = { onTogglePin(note) },
                    onRemove = { onRemove(note) },
                    onRestore = { onRestore(note) },
                    onDeletePermanently = { onDeletePermanently(note) },
                    onToggleChecklistItem = { idx -> onToggleChecklistItem(note, idx) },
                    isDragging = isDragging && !isDragReleased,
                    modifier = itemModifier
                  )
                }

                if (unpinnedNotes.isNotEmpty()) {
                  item(key = "header_unpinned") {
                    Text(
                      text = stringResource(R.string.others_section),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                      ),
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
                    )
                  }
                }
              }

              val listToRender = if (pinnedNotes.isNotEmpty()) unpinnedNotes else currentNotes

              items(listToRender, key = { "list_${it.id}" }) { note ->
                val isDragging = note.id == draggingNoteId
                val itemModifier = rememberReorderModifier(
                  noteId = note.id,
                  draggingNoteId = draggingNoteId,
                  isReleased = isDragReleased,
                  offsetX = { dragOffsetX },
                  offsetY = { dragOffsetY },
                  placement = Modifier.animateItem(placementSpec = ReorderPlacementSpec),
                  onDragStart = onDragStart,
                  onDrag = onDragList,
                  onDragEnd = onDragFinished
                )

                NoteCard(
                  note = note,
                  onClick = { onNoteClick(note) },
                  onTogglePin = { onTogglePin(note) },
                  onRemove = { onRemove(note) },
                  onRestore = { onRestore(note) },
                  onDeletePermanently = { onDeletePermanently(note) },
                  onToggleChecklistItem = { idx -> onToggleChecklistItem(note, idx) },
                  isDragging = isDragging && !isDragReleased,
                  modifier = itemModifier
                )
              }
            }
          }
        }
      }

      // Streamlined Floating Menu Bar (ViewMode + Sort + Search + Add)
      UnifiedFloatingMenuBar(
        viewMode = uiState.viewMode,
        onToggleViewMode = onToggleViewMode,
        onSearchClick = { showSearchSheet = true },
        onCreateNote = onNewNote,
        modifier = Modifier.align(Alignment.BottomCenter)
      )
    }
  }
}

/** How cards slide to make room for the dragged card. Smooth, no bounce. */
private val ReorderPlacementSpec: FiniteAnimationSpec<IntOffset> = spring(
  dampingRatio = Spring.DampingRatioNoBouncy,
  stiffness = Spring.StiffnessMediumLow,
  visibilityThreshold = IntOffset.VisibilityThreshold
)

private const val LIFTED_SCALE = 1.04f

/** Scroll speed (px per frame) for a card held near an edge. Faster closer to the edge. */
private fun autoScrollSpeed(
  centerY: Float,
  viewportHeight: Float,
  edgeZone: Float = 140f,
  maxSpeed: Float = 26f
): Float = when {
  centerY < edgeZone -> -((edgeZone - centerY) / edgeZone).coerceIn(0f, 1f) * maxSpeed
  centerY > viewportHeight - edgeZone ->
    ((centerY - (viewportHeight - edgeZone)) / edgeZone).coerceIn(0f, 1f) * maxSpeed
  else -> 0f
}

/**
 * Shared look and gesture for a reorderable note card (grid and list).
 *
 * - The card lifts with a soft spring when picked up and settles back when dropped.
 * - Other cards glide to their new place using [placement].
 * - Long press then drag starts the move.
 */
@Composable
private fun rememberReorderModifier(
  noteId: Long,
  draggingNoteId: Long?,
  isReleased: Boolean,
  offsetX: () -> Float,
  offsetY: () -> Float,
  placement: Modifier,
  onDragStart: (Long) -> Unit,
  onDrag: (Long, Offset) -> Unit,
  onDragEnd: () -> Unit
): Modifier {
  val isDragging = noteId == draggingNoteId
  val isLifted = isDragging && !isReleased

  val scale by animateFloatAsState(
    targetValue = if (isLifted) LIFTED_SCALE else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioNoBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "note_drag_scale"
  )

  val currentOnDragStart by rememberUpdatedState(onDragStart)
  val currentOnDrag by rememberUpdatedState(onDrag)
  val currentOnDragEnd by rememberUpdatedState(onDragEnd)

  val look = if (isDragging) {
    Modifier
      .zIndex(10f)
      .graphicsLayer {
        translationX = offsetX()
        translationY = offsetY()
        scaleX = scale
        scaleY = scale
      }
  } else {
    Modifier
      .zIndex(0f)
      .then(placement)
  }

  return look.pointerInput(noteId) {
    detectDragGesturesAfterLongPress(
      onDragStart = { currentOnDragStart(noteId) },
      onDrag = { change, dragAmount ->
        change.consume()
        currentOnDrag(noteId, dragAmount)
      },
      onDragEnd = { currentOnDragEnd() },
      onDragCancel = { currentOnDragEnd() }
    )
  }
}