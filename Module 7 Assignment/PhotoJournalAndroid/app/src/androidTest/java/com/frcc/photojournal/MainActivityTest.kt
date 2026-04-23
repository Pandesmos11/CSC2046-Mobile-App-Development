// ============================================================
// Name:        Shane Potts
// Date:        04/22/2026
// Course:      CSC2046 - Mobile App Development
// Module:      7 - Testing and Debugging
// File:        MainActivityTest.kt
// Description: Espresso UI tests for MainActivity (gallery screen).
//              Verifies that key UI elements are present and that
//              the About dialog can be opened via the menu.  Each
//              test clears SharedPreferences so the gallery starts
//              in the empty-state, ensuring a predictable baseline.
// ============================================================

package com.frcc.photojournal

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun clearPrefs() {
        ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("photo_journal", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    // ----------------------------------------------------------
    // Layout / visibility
    // ----------------------------------------------------------

    @Test
    fun toolbar_displaysAppName() {
        onView(withText("Photo Journal")).check(matches(isDisplayed()))
    }

    @Test
    fun fab_isDisplayed() {
        onView(withId(R.id.fab_add)).check(matches(isDisplayed()))
    }

    @Test
    fun menuButton_isDisplayed() {
        onView(withId(R.id.action_menu)).check(matches(isDisplayed()))
    }

    @Test
    fun emptyStateLabel_isDisplayed_whenNoEntries() {
        onView(withId(R.id.txt_empty)).check(matches(isDisplayed()))
    }

    @Test
    fun recyclerView_isDisplayed() {
        onView(withId(R.id.recycler_gallery)).check(matches(isDisplayed()))
    }

    // ----------------------------------------------------------
    // About dialog
    // ----------------------------------------------------------

    @Test
    fun aboutDialog_opensFromMenu() {
        onView(withId(R.id.action_menu)).perform(click())
        onView(withText("About")).perform(click())
        onView(withText("About")).check(matches(isDisplayed()))
    }

    @Test
    fun aboutDialog_containsDeveloperName() {
        onView(withId(R.id.action_menu)).perform(click())
        onView(withText("About")).perform(click())
        onView(withText(org.hamcrest.Matchers.containsString("Shane Potts"))).check(matches(isDisplayed()))
    }

    @Test
    fun aboutDialog_containsModuleInfo() {
        onView(withId(R.id.action_menu)).perform(click())
        onView(withText("About")).perform(click())
        onView(withText(org.hamcrest.Matchers.containsString("Module 6"))).check(matches(isDisplayed()))
    }

    @Test
    fun aboutDialog_dismissesOnOk() {
        onView(withId(R.id.action_menu)).perform(click())
        onView(withText("About")).perform(click())
        onView(withText("OK")).perform(click())
        onView(withId(R.id.fab_add)).check(matches(isDisplayed()))
    }
}
