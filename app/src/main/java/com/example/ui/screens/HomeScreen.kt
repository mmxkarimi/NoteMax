package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
  onNewStandardNote: () -> Unit,
  onNewChecklistNote: () -> Unit,
  onTogglePin: (Note) -> Unit,
  onRemove: (Note) -> Unit,
  onRestore: (Note) -> Unit,
  onDeletePermanently: (Note) -> Unit,
  onToggleChecklistItem: (Note, Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding()
      .testTag("home_screen"),
    containerColor = MaterialTheme.colorScheme.background,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              fontSize = 22.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
        val currentNotes = uiState.activeNotes

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
                  painter = painterResource(R.drawable.ic_note_add),
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
              if (uiState.pinnedNotes.isNotEmpty()) {
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

                items(uiState.pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                  NoteCard(
                    note = note,
                    onClick = { onNoteClick(note) },
                    onTogglePin = { onTogglePin(note) },
                    onRemove = { onRemove(note) },
                    onRestore = { onRestore(note) },
                    onDeletePermanently = { onDeletePermanently(note) },
                    onToggleChecklistItem = { idx -> onToggleChecklistItem(note, idx) }
                  )
                }

                if (uiState.unpinnedNotes.isNotEmpty()) {
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

              val listToRender = if (uiState.pinnedNotes.isNotEmpty()) uiState.unpinnedNotes else currentNotes

              items(listToRender, key = { "note_${it.id}" }) { note ->
                NoteCard(
                  note = note,
                  onClick = { onNoteClick(note) },
                  onTogglePin = { onTogglePin(note) },
                  onRemove = { onRemove(note) },
                  onRestore = { onRestore(note) },
                  onDeletePermanently = { onDeletePermanently(note) },
                  onToggleChecklistItem = { idx -> onToggleChecklistItem(note, idx) }
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
                NoteCard(
                  note = note,
                  onClick = { onNoteClick(note) },
                  onTogglePin = { onTogglePin(note) },
                  onRemove = { onRemove(note) },
                  onRestore = { onRestore(note) },
                  onDeletePermanently = { onDeletePermanently(note) },
                  onToggleChecklistItem = { idx -> onToggleChecklistItem(note, idx) }
                )
              }
            }
          }
        }
      }

      // Streamlined Floating Menu Bar (ViewMode + Sort + Add)
      UnifiedFloatingMenuBar(
        viewMode = uiState.viewMode,
        onToggleViewMode = onToggleViewMode,
        sortOrder = uiState.sortOrder,
        onSortOrderChanged = onSortOrderSelected,
        onCreateStandardNote = onNewStandardNote,
        onCreateChecklistNote = onNewChecklistNote,
        modifier = Modifier.align(Alignment.BottomCenter)
      )
    }
  }
}
