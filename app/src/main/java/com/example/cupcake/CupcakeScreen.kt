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

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cupcake.data.DataSource
import com.example.cupcake.ui.OrderSummaryScreen
import com.example.cupcake.ui.OrderViewModel
import com.example.cupcake.ui.SelectOptionScreen
import com.example.cupcake.ui.StartOrderScreen

enum class CupcakeScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Flavor(title = R.string.choose_flavor),
    Pickup(title = R.string.choose_pickup_date),
    Summary(title = R.string.order_summary),
}


/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@Composable
fun CupcakeAppBar(
    currentScreen: CupcakeScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
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
    /*
        show Up button only  if there's a composable on the back stack
        If the app has no screens on the back stack show StartOrderScreen and hidethe Up button
        To check this, you need a reference to the back stack.
     */
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = CupcakeScreen.valueOf(
        backStackEntry?.destination?.route ?: CupcakeScreen.Start.name
    )

    Scaffold(
        topBar = {
            CupcakeAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = {  navController.navigateUp() }
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
                   modifier = Modifier
                       .fillMaxSize()
                       .padding(dimensionResource(R.dimen.padding_medium)),
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
                   onCancelButtonClicked = {
                       cancelOrderAndNavigateToStart(viewModel, navController)
                   },
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
                   onCancelButtonClicked = {
                       cancelOrderAndNavigateToStart(viewModel, navController)
                   },
                   options = uiState.pickupOptions,
                   onSelectionChanged = { viewModel.setDate(it) },
                   modifier = Modifier.fillMaxHeight(),
               )
           }

           composable(route = CupcakeScreen.Summary.name) {
               // reference to the context object so that you can pass it to the shareOrder() function
               val context = LocalContext.current

               OrderSummaryScreen(
                   orderUiState = uiState,
                   onCancelButtonClicked = {
                       cancelOrderAndNavigateToStart(viewModel, navController)
                   },
                   onSendButtonClicked = { subject: String, summary: String ->
                       shareOrder(context, subject = subject, summary = summary)
                   },
                   modifier = Modifier.fillMaxHeight(),
               )
           }

       }
    }
}

/*
    popBackStack() - pop/remove all screens from the back stack and return to the starting
    screen. method has two required parameters.
        - route: string representing destination route to navigate back to
        - inclusive: Boolean, if true, also pops (removes) specified route,
        if false, popBackStack() will remove all destinations on top of—but not including—the
        start destination, leaving it as the topmost screen visible to the user.

    e.g. pressing cancel sends user back to starting screen and made to reset data
 */
private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    viewModel.resetOrder()
    navController.popBackStack(
        CupcakeScreen.Start.name,
        inclusive = false
    )
}

private fun shareOrder(context: Context, subject: String, summary: String) {
    /*
        ShareSheet — user interface component that covers the bottom part of the screen—that shows
        sharing options.
            - provided by the Android operating system
            - call System UI, such as the sharing screen, with Intent
        Intent - request system to perform some action, commonly presenting a new activity
            - supply intent with some data, such as a string, and present appropriate sharing
            actions for that data

        Basic Intent process setup:
            1. Create  intent object and specify the intent, such as ACTION_SEND.
            2. Specify the type of additional data to send with the intent e.g.
                    - text - use "text/plain"
                    - img - "image/ *" or "video/ *"
                    - ect
             3. use putExtra() to ass any additional data to share to the intent, e.g.
                   - text
                   - image to share
            4.  Call context's startActivity(), passing in an activity created from the intent.
     */

    /*
        Because doing this in a function passed into apply(), noneed to refer to the object's
        identifier, intent.
     */
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, summary)
    }

    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.new_cupcake_order)
        )
    )
}
