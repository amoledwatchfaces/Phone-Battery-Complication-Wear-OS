package com.weartools.phonebattcomp

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class MainApplication : Application(), DefaultLifecycleObserver, CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default)