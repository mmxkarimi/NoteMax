package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import java.util.Collections
import com.example.R
import com.example.data.model.Note
import com.example.ui.components.NoteCard
import com.example.ui.components.UnifiedFloatingMenuBar
import com.example.ui.viewmodel.NotesUiState
import com.example.ui.viewmodel.SortOrder
import com.example.ui.viewmodel.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  uiState: NotesUiState,
  onToggleViewMode: () -> Unit,
  onSortOrderSelected: (SortOrder) -> Unit,
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
  var showSearchSheet by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  // Drag and Drop reordering state
  var draggingNoteId by remember { mutableStateOf<Long?>(null) }
  var dragOffsetX by remember { mutableFloatStateOf(0f) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }
  var localNotes by remember(uiState.activeNotes) { mutableStateOf(uiState.activeNotes) }

  LaunchedEffect(uiState.activeNotes) {
    if (draggingNoteId == null) {
      localNotes = uiState.activeNotes
    }
  }

  val onDragFinished: () -> Unit = {
    if (localNotes != uiState.activeNotes) {
      onReorderNotes(localNotes)
    }
    draggingNoteId = null
    dragOffsetX = 0f
    dragOffsetY = 0f
  }

  val handleReorder = { fromId: Long, deltaY: Float, deltaX: Float, isGrid: Boolean ->
    val fromNote = localNotes.find { it.id == fromId }
    if (fromNote != null) {
      val sectionNotes = localNotes.filter { it.isPinned == fromNote.isPinned }
      val idx = sectionNotes.indexOfFirst { it.id == fromId }
      if (idx != -1) {
        val thresholdY = 160f
        val thresholdX = 220f
        var targetSectionIdx = -1
        var consumedY = 0f
        var consumedX = 0f

        if (isGrid) {
          if (deltaY > thresholdY && idx + 2 < sectionNotes.size) {
            targetSectionIdx = idx + 2
            consumedY = thresholdY
          } else if (deltaY < -thresholdY && idx - 2 >= 0) {
            targetSectionIdx = idx - 2
            consumedY = -thresholdY
          } else if (deltaX > thresholdX && idx + 1 < sectionNotes.size) {
            targetSectionIdx = idx + 1
            consumedX = thresholdX
          } else if (deltaX < -thresholdX && idx - 1 >= 0) {
            targetSectionIdx = idx - 1
            consumedX = -thresholdX
          }
        } else {
          if (deltaY > thresholdY && idx + 1 < sectionNotes.size) {
            targetSectionIdx = idx + 1
            consumedY = thresholdY
          } else if (deltaY < -thresholdY && idx - 1 >= 0) {
            targetSectionIdx = idx - 1
            consumedY = -thresholdY
          }
        }

        if (targetSectionIdx != -1) {
          val toNote = sectionNotes[targetSectionIdx]
          val fullList = localNotes.toMutableList()
          val fullFromIdx = fullList.indexOfFirst { it.id == fromNote.id }
          val fullToIdx = fullList.indexOfFirst { it.id == toNote.id }
          if (fullFromIdx != -1 && fullToIdx != -1) {
            Collections.swap(fullList, fullFromIdx, fullToIdx)
            localNotes = fullList
          }
          dragOffsetY -= consumedY
          dragOffsetX -= consumedX
        }
      }
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
                  contentDescription = "Clear",
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
          val displayList = if (trimmedQuery.isEmpty()) uiState.activeNotes else searchResults
          if (trimmedQuery.isEmpty() && displayList.isNotEmpty()) {
            Text(
              text = "ALL NOTES (${displayList.size})",
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
                      text = if (note.title.isNotBlank()) note.title else "Untitled Note",
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
                    Text(
                      text = note.content,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis
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
                  val dragHandleModifier = Modifier.pointerInput(note.id) {
                    detectDragGestures(
                      onDragStart = {
                        draggingNoteId = note.id
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                      },
                      onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y
                        handleReorder(note.id, dragOffsetY, dragOffsetX, true)
                      },
                      onDragEnd = { onDragFinished() },
                      onDragCancel = { onDragFinished() }
                    )
                  }
                  val cardDragModifier = Modifier
                    .zIndex(if (isDragging) 10f else 0f)
                    .graphicsLayer {
                      if (isDragging) {
                        translationX = dragOffsetX
                        translationY = dragOffsetY
                        scaleX = 1.04f
                        scaleY = 1.04f
                      }
                    }
                    .pointerInput(note.id) {
                      detectDragGesturesAfterLongPress(
                        onDragStart = {
                          draggingNoteId = note.id
                          dragOffsetX = 0f
                          dragOffsetY = 0f
                        },
                        onDrag = { change, dragAmount ->
                          change.consume()
                          dragOffsetX += dragAmount.x
                          dragOffsetY += dragAmount.y
                          handleReorder(note.id, dragOffsetY, dragOffsetX, true)
                        },
                        onDragEnd = { onDragFinished() },
                        onDragCancel = { onDragFinished() }
                      )
                    }

                  NoteCard(
                    note = note,
                    onClick = { onNoteClick(note) },
                    onTogglePin = { onTogglePin(note) },
                    onRemove = { onRemove(note) },
                    onRestore = { onRestore(note) },
                    onDeletePermanently = { onDeletePermanently(note) },
                    onToggleChecklistItem = { idx -> onToggleChecklistItem(note, idx) },
                    isDragging = isDragging,
                    dragHandleModifier = dragHandleModifier,
                    modifier = cardDragModifier
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
                val dragHandleModifier = Modifier.pointerInput(note.id) {
                  detectDragGestures(
                    onDragStart = {
                      draggingNoteId = note.id
                      dragOffsetX = 0f
                      dragOffsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                      change.consume()
                      dragOffsetX += dragAmount.x
                      dragOffsetY += dragAmount.y
                      handleReorder(note.id, dragOffsetY, dragOffsetX, true)
                    },
                    onDragEnd = { onDragFinished() },
                    onDragCancel = { onDragFinished() }
                  )
                }
                val cardDragModifier = Modifier
                  .zIndex(if (isDragging) 10f else 0f)
                  .graphicsLayer {
                    if (isDragging) {
                      translationX = dragOffsetX
                      translationY = dragOffsetY
                      scaleX = 1.04f
                      scaleY = 1.04f
                    }
                  }
                  .pointerInput(note.id) {
                    detectDragGesturesAfterLongPress(
                      onDragStart = {
                        draggingNoteId = note.id
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                      },
                      onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y
                        handleReorder(note.id, dragOffsetY, dragOffsetX, true)
                      },
                      onDragEnd = { onDragFinished() },
                      onDragCancel = { onDragFinished() }
                    )
                  }

                NoteCard(
                  note = note,
                  onClick = { onNoteClick(note) },
                  onTogglePin = { onTogglePin(note) },
                  onRemove = { onRemove(note) },
                  onRestore = { onRestore(note) },
                  onDeletePermanently = { onDeletePermanently(note) },
                  onToggleChecklistItem = { idx -> onToggleChecklistItem(note, idx) },
                  isDragging = isDragging,
                  dragHandleModifier = dragHandleModifier,
                  modifier = cardDragModifier
                )
              }
            }
          } else {
            // List View
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .testTag("notes_list_view"),
              contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(currentNotes, key = { "list_${it.id}" }) { note ->
                val isDragging = note.id == draggingNoteId
                val dragHandleModifier = Modifier.pointerInput(note.id) {
                  detectDragGestures(
                    onDragStart = {
                      draggingNoteId = note.id
                      dragOffsetX = 0f
                      dragOffsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                      change.consume()
                      dragOffsetX += dragAmount.x
                      dragOffsetY += dragAmount.y
                      handleReorder(note.id, dragOffsetY, dragOffsetX, false)
                    },
                    onDragEnd = { onDragFinished() },
                    onDragCancel = { onDragFinished() }
                  )
                }
                val cardDragModifier = Modifier
                  .zIndex(if (isDragging) 10f else 0f)
                  .graphicsLayer {
                    if (isDragging) {
                      translationX = dragOffsetX
                      translationY = dragOffsetY
                      scaleX = 1.04f
                      scaleY = 1.04f
                    }
                  }
                  .pointerInput(note.id) {
                    detectDragGesturesAfterLongPress(
                      onDragStart = {
                        draggingNoteId = note.id
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                      },
                      onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y
                        handleReorder(note.id, dragOffsetY, dragOffsetX, false)
                      },
                      onDragEnd = { onDragFinished() },
                      onDragCancel = { onDragFinished() }
                    )
                  }

                NoteCard(
                  note = note,
                  onClick = { onNoteClick(note) },
                  onTogglePin = { onTogglePin(note) },
                  onRemove = { onRemove(note) },
                  onRestore = { onRestore(note) },
                  onDeletePermanently = { onDeletePermanently(note) },
                  onToggleChecklistItem = { idx -> onToggleChecklistItem(note, idx) },
                  isDragging = isDragging,
                  dragHandleModifier = dragHandleModifier,
                  modifier = cardDragModifier
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
        sortOrder = uiState.sortOrder,
        onSortOrderChanged = onSortOrderSelected,
        onSearchClick = { showSearchSheet = true },
        onCreateNote = onNewNote,
        modifier = Modifier.align(Alignment.BottomCenter)
      )
    }
  }
}
