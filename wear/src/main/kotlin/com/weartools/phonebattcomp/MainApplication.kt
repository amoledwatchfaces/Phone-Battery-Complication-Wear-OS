package com.weartools.phonebattcomp

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class MainApplication : Application(){

    /** Global variables **/
    var phoneBatteryLevel by mutableIntStateOf(0)
    var phoneIsCharging by mutableStateOf(false)
    var phoneIsConnected by mutableStateOf(false)
    var afterMobileResult by mutableStateOf(false)
    var lastUpdate = mutableStateOf<Long?>(null)

}

@Module
@InstallIn(SingletonComponent::class)
class DataClientModule {
    @Provides
    fun provideDataClient(
        @ApplicationContext context: Context
    ): DataClient =  Wearable.getDataClient(context)
}
