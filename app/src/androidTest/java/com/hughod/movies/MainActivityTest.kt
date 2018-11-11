package com.hughod.movies


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    var rule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun mainActivityTest() {

        val recycler = onView(withId(R.id.recycler)).check(matches(isDisplayed()))

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))

        onView(withText("Popular Movies")).check(matches(isDisplayed()))

        recycler.perform(click())

        onView(allOf(withId(R.id.scroll_view), isDisplayed())).check(matches(isDisplayed()))

        pressBack()

        recycler.check(matches(isDisplayed()))
    }
}
