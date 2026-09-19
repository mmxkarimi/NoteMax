package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.ChecklistItem
import com.example.data.model.Note
import com.example.ui.components.NoteCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        NoteCard(
          note = Note(
            id = 1L,
            title = "NoteMax Launch Sprint",
            content = "- [x] Setup M3 Dynamic Color\n- [ ] BiDi Verification",
            isChecklist = true,
            checklistItems = listOf(
              ChecklistItem(text = "Setup M3 Dynamic Color with #c6f135", isChecked = true),
              ChecklistItem(text = "Verify BiDi text support", isChecked = false)
            ),
            isEncrypted = true,
            isUnlocked = true,
            isPinned = true
          ),
          onClick = {},
          onUnlockRequest = {},
          onTogglePin = {},
          onRemove = {},
          onRestore = {},
          onDeletePermanently = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
