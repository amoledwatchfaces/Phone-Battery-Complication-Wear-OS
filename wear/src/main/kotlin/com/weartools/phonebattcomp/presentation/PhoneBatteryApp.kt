package com.weartools.phonebattcomp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import com.google.android.gms.wearable.DataClient
import com.weartools.phonebattcomp.MainViewModel
import com.weartools.phonebattcomp.theme.PhoneBatteryAppTheme

@Composable
fun PhoneBatteryApp(
    viewModel: MainViewModel = hiltViewModel(),
    dataClient: DataClient
) {
    PhoneBatteryAppTheme {
        val state = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()
        val focusRequester = remember { FocusRequester() }

        AppScaffold {
            ScreenScaffold(state) {
                PhoneBatteryAppScreen(
                    listState = state,
                    transformationSpec = transformationSpec,
                    focusRequester = focusRequester,
                    viewModel = viewModel,
                    dataClient = dataClient,
                )
            }
        }
    }
}