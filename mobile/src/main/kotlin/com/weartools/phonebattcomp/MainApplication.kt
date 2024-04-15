package com.weartools.phonebattcomp

import android.app.Application

class MainApplication : Application() {
    val dataRepository by lazy { DataRepository(this) }
}