package ir.mmxkarimi.notemax.ui.screens

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import ir.mmxkarimi.notemax.R
import ir.mmxkarimi.notemax.data.model.Note
import ir.mmxkarimi.notemax.ui.components.MarkdownPreview
import ir.mmxkarimi.notemax.ui.components.NoteCard
import ir.mmxkarimi.notemax.ui.components.NoteSkeletonLoading
import ir.mmxkarimi.notemax.ui.components.UnifiedFloatingMenuBar
import ir.mmxkarimi.notemax.ui.viewmodel.NotesUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  uiState: NotesUiState,
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
  val listState = rememberLazyListState()
  val haptics = LocalHapticFeedback.current
  val density = LocalDensity.current
  val latestUiState by rememberUpdatedState(uiState)

  var showSearchSheet by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  // ---------------------------------------------------------------------------------------------
  // Drag and drop reordering
  //
  // The dragged card's position is kept as an ABSOLUTE position ([dragBase], top-left corner in the
  // lazy layout's coordinate space). The visual translation is always derived from it:
  //
  //     translation = (dragBase - currentLayoutPosition) * settleFraction
  //
  // That means we never have to patch offsets by hand when the list reorders or when auto-scroll
  // moves the content. The card simply stays under the finger.
  // ---------------------------------------------------------------------------------------------
  var draggingNoteId by remember { mutableStateOf<Long?>(null) }
  // True after the finger lifts, while the card is still gliding into its slot.
  var isDragReleased by remember { mutableStateOf(false) }
  var dragBase by remember { mutableStateOf(Offset.Zero) }
  // 1 = card is where the finger put it, 0 = card sits exactly in its layout slot.
  var settleFraction by remember { mutableFloatStateOf(1f) }
  var settleJob by remember { mutableStateOf<Job?>(null) }
  var localNotes by remember { mutableStateOf(uiState.activeNotes) }
  var externalChangeWhileDragging by remember { mutableStateOf(false) }
  val swapGuard = remember { SwapGuard() }

  // Never replace the working list while a drag is in progress.
  LaunchedEffect(uiState.activeNotes) {
    if (draggingNoteId == null) {
      localNotes = uiState.activeNotes
    } else {
      externalChangeWhileDragging = true
    }
  }

  val edgeZonePx = with(density) { 72.dp.toPx() }
  val maxScrollPerFramePx = with(density) { 10.dp.toPx() }

  // --- Layout access ---------------------------------------------------------------------------

  fun allBoxes(): List<ItemBox> {
    val info = listState.layoutInfo
    return info.visibleItemsInfo.map {
      ItemBox(
        key = it.key,
        index = it.index,
        topLeft = Offset(0f, it.offset.toFloat()),
        size = Size(info.viewportSize.width.toFloat(), it.size.toFloat())
      )
    }
  }

  fun findBox(noteId: Long): ItemBox? {
    val info = listState.layoutInfo
    for (it in info.visibleItemsInfo) {
      if (noteIdFromKey(it.key) == noteId) {
        return ItemBox(
          key = it.key,
          index = it.index,
          topLeft = Offset(0f, it.offset.toFloat()),
          size = Size(info.viewportSize.width.toFloat(), it.size.toFloat())
        )
      }
    }
    return null
  }

  fun viewportStartEnd(): Pair<Float, Float> {
    val info = listState.layoutInfo
    return info.viewportStartOffset.toFloat() to info.viewportEndOffset.toFloat()
  }

  val dragTranslation: (Long) -> Offset = { noteId ->
    val box = findBox(noteId)
    if (box == null) Offset.Zero else (dragBase - box.topLeft) * settleFraction
  }

  // --- Reordering ------------------------------------------------------------------------------

  /** Moves the dragged note into the slot of the note its centre is hovering over. */
  fun moveIfHovering(noteId: Long) {
    val boxes = allBoxes()
    val own = boxes.firstOrNull { noteIdFromKey(it.key) == noteId } ?: return

    // We just reordered and the lazy layout has not caught up yet: its positions are stale.
    if (swapGuard.isWaiting(own.index)) return

    val center = dragBase + Offset(own.size.width / 2f, own.size.height / 2f)
    // Still above its own slot, nothing to do.
    if (own.contains(center)) return

    val fromNote = localNotes.firstOrNull { it.id == noteId } ?: return
    val target = boxes.firstOrNull { box ->
      val id = noteIdFromKey(box.key)
      id != null && id != noteId && box.contains(center) &&
        localNotes.firstOrNull { it.id == id }?.isPinned == fromNote.isPinned
    } ?: return
    val targetId = noteIdFromKey(target.key) ?: return

    val fullList = localNotes.toMutableList()
    val fromIdx = fullList.indexOfFirst { it.id == noteId }
    val toIdx = fullList.indexOfFirst { it.id == targetId }
    if (fromIdx != -1 && toIdx != -1 && fromIdx != toIdx) {
      fullList.add(toIdx, fullList.removeAt(fromIdx))
      localNotes = fullList
      swapGuard.arm(target.index)
      haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
  }

  val onDragStart: (Long) -> Unit = { noteId ->
    settleJob?.cancel()
    swapGuard.reset()
    dragBase = findBox(noteId)?.topLeft ?: Offset.Zero
    settleFraction = 1f
    draggingNoteId = noteId
    isDragReleased = false
    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  val onDragFinished: () -> Unit = finish@{
    if (draggingNoteId == null || isDragReleased) return@finish

    if (localNotes != latestUiState.activeNotes) {
      onReorderNotes(localNotes)
    }

    // Glide the card into its new slot instead of snapping it there.
    isDragReleased = true
    settleJob?.cancel()
    settleJob = coroutineScope.launch {
      animate(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioNoBouncy,
          stiffness = Spring.StiffnessMedium
        )
      ) { progress, _ -> settleFraction = progress }
      settleFraction = 1f
      draggingNoteId = null
      isDragReleased = false
      if (externalChangeWhileDragging) {
        externalChangeWhileDragging = false
        localNotes = latestUiState.activeNotes
      }
    }
  }

  val onDrag: (Long, Offset) -> Unit = { noteId, dragAmount ->
    // List mode moves vertically.
    dragBase += Offset(0f, dragAmount.y)
    moveIfHovering(noteId)
  }

  // Smooth auto-scroll: runs every frame while a card is held near the top or bottom edge,
  // even when the finger is not moving. The edges are the real content edges
  // (viewportStart/EndOffset), so the bottom padding reserved for the floating menu bar counts as
  // "past the edge" and the list keeps scrolling until the last note is reachable.
  LaunchedEffect(draggingNoteId, isDragReleased) {
    val noteId = draggingNoteId ?: return@LaunchedEffect
    if (isDragReleased) return@LaunchedEffect

    while (isActive) {
      val own = findBox(noteId)
      if (own != null) {
        val (top, bottom) = viewportStartEnd()
        val centerY = dragBase.y + own.size.height / 2f
        val delta = autoScrollSpeed(centerY, top, bottom, edgeZonePx, maxScrollPerFramePx)
        if (delta != 0f) {
          listState.scrollBy(delta)
        }
        // Content may have moved under a stationary finger, so re-check what we hover.
        moveIfHovering(noteId)
      }
      withFrameNanos { }
    }
  }

  val placementFor: (Long) -> FiniteAnimationSpec<IntOffset>? = { id ->
    // The dragged card is positioned by the finger, it must not animate its own placement.
    if (id == draggingNoteId) null else ReorderPlacementSpec
  }

  val renderNote: @Composable (Note, Modifier) -> Unit =
    { note, placement ->
      val isDragging = note.id == draggingNoteId
      val itemModifier = rememberReorderModifier(
        noteId = note.id,
        draggingNoteId = draggingNoteId,
        isReleased = isDragReleased,
        translation = dragTranslation,
        placement = placement,
        onDragStart = onDragStart,
        onDrag = onDrag,
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

        if (uiState.isLoading) {
          // Shimmer Skeleton Loading State
          NoteSkeletonLoading(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp)
          )
        } else if (currentNotes.isEmpty()) {
          // Empty State with smooth fade-in
          AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
          ) {
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
          }
        } else {
          val pinnedNotes = remember(localNotes) { localNotes.filter { it.isPinned } }
          val unpinnedNotes = remember(localNotes) { localNotes.filter { !it.isPinned } }

          // Single-column List View
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
                renderNote(
                  note,
                  Modifier.animateItem(placementSpec = placementFor(note.id))
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
              renderNote(
                note,
                Modifier.animateItem(placementSpec = placementFor(note.id))
              )
            }
          }
        }
      }

      // Streamlined Floating Menu Bar (Search + Add)
      UnifiedFloatingMenuBar(
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

/** A visible lazy item, normalised so grid and list share the same drag logic. */
private data class ItemBox(
  val key: Any,
  val index: Int,
  val topLeft: Offset,
  val size: Size
) {
  fun contains(p: Offset): Boolean =
    p.x >= topLeft.x && p.x <= topLeft.x + size.width &&
      p.y >= topLeft.y && p.y <= topLeft.y + size.height
}

/**
 * After a reorder the lazy layout needs a frame to catch up. Until the dragged item reports its
 * new index, the layout positions are stale and must not be used for another reorder.
 */
private class SwapGuard {
  private var expectedIndex: Int? = null
  private var armedAtNanos = 0L

  fun arm(index: Int) {
    expectedIndex = index
    armedAtNanos = System.nanoTime()
  }

  fun reset() {
    expectedIndex = null
  }

  fun isWaiting(currentIndex: Int): Boolean {
    val expected = expectedIndex ?: return false
    val timedOut = System.nanoTime() - armedAtNanos > 300_000_000L
    if (currentIndex == expected || timedOut) {
      expectedIndex = null
      return false
    }
    return true
  }
}

/** Only keys that really belong to a note card map to an id (headers and others return null). */
private fun noteIdFromKey(key: Any?): Long? {
  val s = key as? String ?: return null
  val raw = when {
    s.startsWith("pinned_") -> s.removePrefix("pinned_")
    s.startsWith("note_") -> s.removePrefix("note_")
    s.startsWith("list_") -> s.removePrefix("list_")
    else -> return null
  }
  return raw.toLongOrNull()
}

/** Scroll speed (px per frame) for a card held near an edge. Faster closer to the edge. */
private fun autoScrollSpeed(
  centerY: Float,
  viewportTop: Float,
  viewportBottom: Float,
  edgeZone: Float,
  maxSpeed: Float
): Float = when {
  centerY < viewportTop + edgeZone ->
    -((viewportTop + edgeZone - centerY) / edgeZone).coerceIn(0f, 1f) * maxSpeed
  centerY > viewportBottom - edgeZone ->
    ((centerY - (viewportBottom - edgeZone)) / edgeZone).coerceIn(0f, 1f) * maxSpeed
  else -> 0f
}

/**
 * Shared look and gesture for a reorderable note card (grid and list).
 *
 * - The modifier chain has the same shape whether or not the card is dragged, so the pointer
 *   gesture is never torn down in the middle of a drag.
 * - The dragged card is placed by [translation] (absolute, finger driven); other cards glide
 *   using [placement].
 * - Long press then drag starts the move.
 */
@Composable
private fun rememberReorderModifier(
  noteId: Long,
  draggingNoteId: Long?,
  isReleased: Boolean,
  translation: (Long) -> Offset,
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
  val currentTranslation by rememberUpdatedState(translation)

  return Modifier
    .then(placement)
    .zIndex(if (isDragging) 10f else 0f)
    .graphicsLayer {
      if (isDragging) {
        val t = currentTranslation(noteId)
        translationX = t.x
        translationY = t.y
        scaleX = scale
        scaleY = scale
      }
    }
    .pointerInput(noteId) {
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