package com.example.cupcake.test

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.rules.ActivityScenarioRule

/*
    A number of tests require interacting with UI components.
    Components are often found using a resource string.
    Use Context.getString() to access composables by its resource string
    Normally when writing a UI test in compose, implementing this method looks like this:

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.my_string)

    Extension function reduces the amount of code when finding a UI component by
    its string resource. Instead of writing this:

    composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.my_string)

    vs

    composeTestRule.onNodeWithStringId(R.string.my_string)

 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.onNodeWithStringId(
    @StringRes id: Int
): SemanticsNodeInteraction = onNodeWithText(activity.getString(id))
