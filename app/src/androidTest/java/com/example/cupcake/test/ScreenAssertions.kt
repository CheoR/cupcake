package com.example.cupcake.test

import androidx.navigation.NavController
import org.junit.Assert.assertEquals

// test helper functions
// notice how adding extension to adroidx.navigation
fun NavController.assertCurrentRouteName(expectedRouteName: String) {
    assertEquals(expectedRouteName, currentBackStackEntry?.destination?.route)
}
