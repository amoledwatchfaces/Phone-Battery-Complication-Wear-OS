package com.weartools.phonebattcomp

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainApplication : Application(),
    DefaultLifecycleObserver, CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default) {
    val dataRepository by lazy { DataRepository(this) }
}