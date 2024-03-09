package com.weartools.phonebattcomp.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import com.weartools.phonebattcomp.data.DataRepository
import com.weartools.phonebattcomp.data.PassiveDataViewModel
import com.weartools.phonebattcomp.data.PassiveDataViewModelFactory
import com.weartools.phonebattcomp.theme.PhoneBatteryAppTheme

@Composable
fun PhoneBatteryApp(
    dataRepository: DataRepository
) {
    PhoneBatteryAppTheme {
        val listState = rememberScalingLazyListState()
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {focusRequester.requestFocus()}
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            timeText = { TimeText(modifier = Modifier.scrollAway(listState)) },
            positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
        ) {
            val viewModel: PassiveDataViewModel = viewModel(
                factory = PassiveDataViewModelFactory(
                    dataRepository = dataRepository
                )
            )
            val batteryLevel by viewModel.batteryLevel.collectAsState()
            val nodeName by viewModel.nodeName.collectAsState()
            val tempUnit by viewModel.tempUnit.collectAsState()
            val percentage by viewModel.percentage.collectAsState()

            PhoneBatteryAppScreen(
                listState = listState,
                focusRequester = focusRequester,
                nodeName = nodeName,
                batteryLevel = batteryLevel,
                tempUnit = tempUnit,
                percentage = percentage
            )
        }
    }
}