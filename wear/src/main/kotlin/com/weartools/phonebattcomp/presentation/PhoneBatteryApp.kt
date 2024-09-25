package com.weartools.phonebattcomp.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import com.google.android.gms.wearable.DataClient
import com.weartools.phonebattcomp.MainViewModel
import com.weartools.phonebattcomp.theme.PhoneBatteryAppTheme

@Composable
fun PhoneBatteryApp(
    viewModel: MainViewModel = hiltViewModel(),
    dataClient: DataClient
) {
    PhoneBatteryAppTheme {
        val listState = rememberScalingLazyListState()
        val focusRequester = remember { FocusRequester() }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            timeText = { TimeText(modifier = Modifier.scrollAway(listState)) },
            positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
        ) {
            val nodeName by viewModel.nodeName.collectAsState()
            val tempUnit by viewModel.tempUnit.collectAsState()
            val percentage by viewModel.percentage.collectAsState()
            val notificationIconType by viewModel.notificationIconType.collectAsState()

            PhoneBatteryAppScreen(
                listState = listState,
                focusRequester = focusRequester,
                nodeName = nodeName,
                tempUnit = tempUnit,
                percentage = percentage,
                viewModel = viewModel,
                dataClient = dataClient,
                notificationIconType = notificationIconType
            )
        }
    }
}