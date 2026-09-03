package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.SavedPage
import com.example.ui.components.SavedPageCard
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
    val samplePage = SavedPage(
      id = 1,
      title = "The Education Hills - Official Portal",
      url = "https://educationhills.netlify.app/",
      category = "Portal",
      notes = "Official portal and campus hub",
      isPinned = true
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        SavedPageCard(
          page = samplePage,
          onOpen = {},
          onTogglePin = {},
          onEdit = {},
          onDelete = {},
          onShare = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
