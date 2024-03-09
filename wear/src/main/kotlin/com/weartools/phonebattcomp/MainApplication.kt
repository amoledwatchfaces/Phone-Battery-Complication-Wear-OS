package com.weartools.phonebattcomp

import android.app.Application
import com.weartools.phonebattcomp.data.DataRepository

class MainApplication : Application() {
    val dataRepository by lazy { DataRepository(this) }
}
