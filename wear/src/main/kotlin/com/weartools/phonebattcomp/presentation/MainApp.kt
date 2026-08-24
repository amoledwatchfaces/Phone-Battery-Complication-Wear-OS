package com.weartools.phonebattcomp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.gms.wearable.DataClient
import com.weartools.phonebattcomp.MainViewModel
import com.weartools.phonebattcomp.theme.PhoneBatteryAppTheme

@Composable
fun MainApp(
    viewModel: MainViewModel = hiltViewModel(),
    dataClient: DataClient
) {
    val preferences by viewModel.preferences.collectAsState()

    PhoneBatteryAppTheme(
        useDynamicColor = preferences.useDynamicColor
    ) {

        val scrollState = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()
        val focusRequester = remember { FocusRequester() }
        val navController = rememberSwipeDismissableNavController()

        AppScaffold {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "main_screen"
            ) {
                composable("main_screen") {
                    MainScreen(
                        navController = navController,
                        listState = scrollState,
                        transformationSpec = transformationSpec,
                        focusRequester = focusRequester,
                        viewModel = viewModel,
                        dataClient = dataClient,
                    )
                }

                composable("guide_screen") {
                    val scrollState = rememberTransformingLazyColumnState()
                    GuideScreen(
                        scrollState = scrollState,
                        transformationSpec = transformationSpec,
                        focusRequester = focusRequester
                    )
                }
            }
        }
    }
}
