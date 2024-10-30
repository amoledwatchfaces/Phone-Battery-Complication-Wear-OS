package com.weartools.phonebattcomp.di

import kotlinx.coroutines.flow.MutableSharedFlow

object ServiceCommunication {
    // A shared flow to trigger sending to the watch
    val sendToWatchFlow = MutableSharedFlow<Unit>(replay = 0)  // No replay needed
    val listenerSettingsFlow = MutableSharedFlow<Unit>(replay = 0) // No replay needed
}