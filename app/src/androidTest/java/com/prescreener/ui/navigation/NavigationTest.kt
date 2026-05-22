package com.prescreener.ui.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import com.prescreener.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testStartDestinationIsWelcome() {
        // The AppNavigation is set in MainActivity, so it should start at Welcome
        composeTestRule.onNodeWithText("⚠️ Important Medical Disclaimer").assertIsDisplayed()
    }

    @Test
    fun testNavigationFromWelcomeToPatientInfo() {
        // Find the disclaimer scrollable area and scroll to bottom
        composeTestRule.onNode(hasScrollAction()).performTouchInput {
            swipeUp()
            swipeUp() // Ensure we reach the bottom
        }

        // Wait for button to be enabled and click it
        composeTestRule.onNodeWithText("I Understand — Get Started")
            .assertIsEnabled()
            .performClick()

        // Verify we are on the Patient Info screen
        composeTestRule.onNodeWithText("Patient Information").assertIsDisplayed()
        composeTestRule.onNodeWithText("Age").assertIsDisplayed()
    }
}
