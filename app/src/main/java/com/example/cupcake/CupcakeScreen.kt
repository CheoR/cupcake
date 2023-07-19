/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.cupcake

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cupcake.data.DataSource
import com.example.cupcake.ui.OrderSummaryScreen
import com.example.cupcake.ui.OrderViewModel
import com.example.cupcake.ui.SelectOptionScreen
import com.example.cupcake.ui.StartOrderScreen

enum class CupcakeScreen() {
    Start,
    Flavor,
    Pickup,
    Summary
}


/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@Composable
fun CupcakeAppBar(
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Composable
fun CupcakeApp(
    viewModel: OrderViewModel = viewModel(),
    // NavController - NavHostController class instance
    // Responsible for navigating between destinations e.g. app screens
    // use  to navigate between screens, e.g call navigate() to navigate to another destination
    // call rememberNavController() from a composable function to get NavHostController
    navController: NavHostController = rememberNavController()
) {

    Scaffold(
        topBar = {
            CupcakeAppBar(
                canNavigateBack = false,
                navigateUp = { /* TODO: implement back navigation */ }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()
        // NavHost - Composable displays other composable destinations, based on a given route
        /*
            A benefit of using a NavHost to handle app's navigation is that navigation logic is
            kept separate from individual UI. This avoids some of the major drawbacks of
            passing the navController as a parameter. E.g.
                - Navigation logic is kept in one place, which can make code easier to maintain and
                prevent bugs by not accidentally giving individual screens free reign of navigation
                in app.
                - In apps that need to work on different form factors (like portrait mode phone,
                foldable phone, or large screen tablet), a button may or may not trigger navigation,
                depending on the app's layout. Individual screens should be self-contained and don't
                need to be aware of other screens in the app.

            Better approach is to pass a function type into each composable for what should happen
            when a user does some action, e.g. clicks button. That way, the composable and any of
            its child composables decide when to call the function. However, navigation logic isn't
            exposed to the individual screens in app. All the navigation behavior is handled in the
            NavHost.
         */
       NavHost(
           navController = navController,
           // string route defining the destination shown by default when the app first displays the NavHost
           startDestination = CupcakeScreen.Start.name,
           modifier = Modifier.padding(innerPadding)
       ) {
           // Composable container displays NavGraph's current destination
           // route is a string that maps to a destination and serves as its unique identifier
           // A destination is typically a single Composable or group of Composables corresponding
           // to what the user sees.
           composable(route = CupcakeScreen.Start.name) {
               // content: Here you can call a composable that you want to display for the given
               // route.
               StartOrderScreen(
                   quantityOptions = DataSource.quantityOptions,
                   onNextButtonClicked = {
                       viewModel.setQuantity(it)
                       navController.navigate(CupcakeScreen.Flavor.name)
                   },
               )
           }

           /*
                Context is an abstract class whose implementation is provided by the Android system.
                It allows access to application-specific resources and classes, as well as up-calls
                for application-level operations such as launching activities, etc.

                You can use this variable to get the strings from the list of resource IDs in the
                view model to display the list of flavors.
            */
           composable(route = CupcakeScreen.Flavor.name) {
               val context = LocalContext.current
               /*
                The flavor screen needs to display and update the subtotal when the user selects
                 a flavor

                 The flavor screen gets the list of flavors from the app's string resources.
                 Transform the list of resource IDs into a list of strings with
                 context.resources.getString(id) for each flavor.
                */
               SelectOptionScreen(
                   subtotal = uiState.price,
                   onNextButtonClicked = { navController.navigate(CupcakeScreen.Pickup.name) },
                   onCancelButtonClicked = {},
                   options = DataSource.flavors.map { id -> context.resources.getString(id) },
                   // lambda expression that calls setFlavor() on the view model
                   // passing in it (the argument passed into onSelectionChanged()).
                   onSelectionChanged = { viewModel.setFlavor(it) },
                   modifier = Modifier.fillMaxHeight(),
               )
           }

           composable(route = CupcakeScreen.Pickup.name) {
               SelectOptionScreen(
                   subtotal = uiState.price,
                   onNextButtonClicked = { navController.navigate(CupcakeScreen.Summary.name) },
                   onCancelButtonClicked = {},
                   options = uiState.pickupOptions,
                   onSelectionChanged = { viewModel.setDate(it) },
                   modifier = Modifier.fillMaxHeight(),
               )
           }

           composable(route = CupcakeScreen.Summary.name) {
               OrderSummaryScreen(
                   orderUiState = uiState,
                   onCancelButtonClicked = {},
                   onSendButtonClicked = { subject: String, summary: String ->

                   },
                   modifier = Modifier.fillMaxHeight(),
               )
           }

       }
    }
}
