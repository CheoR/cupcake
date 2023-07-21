package com.example.cupcake.test

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.example.cupcake.CupcakeApp
import com.example.cupcake.CupcakeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import org.junit.Assert.assertEquals

/*
    Reference TestNavHostController instance to check the navigation route of the nav host to
    make sure app navigates to the correct place

    lateinit - declare that property can be initialized after object declared

    Every test in the Test class involves testing an aspect of navigation.
    Each test depends on the TestNavHostController object below. Instead of manually calling navhost setup
    e.g setupCupcakeNavHost() function for every test to set up the nav controller, using @Before
    annotation to automatically run before every method annotated with @Test.
 */
class CupcakeScreenNavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController


    fun setupCupcakeNavHost() {
        composeTestRule.setContent {
            CupcakeApp()
        }
    }


    @Before
    fun setupCupcakeNavHost() {
        composeTestRule.setContent {
            //  automatically launches the app
            // displaying app composable before the execution of any @Test
            //  use this object later to determine the navigation state,
            //  as the app uses the controller to navigate the various screens in app.
            navController = TestNavHostController(LocalContext.current).apply {
                // register controller
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            CupcakeApp(navController = navController)
        }
    }

    @Test
    fun cupcakeNavHost_verifyStartDestination() {
        //  nav controller's current back stack entry
        assertEquals(CupcakeScreen.Start.name, navController.currentBackStackEntry?.destination?.route)
    }

}