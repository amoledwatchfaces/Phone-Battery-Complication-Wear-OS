package com.weartools.phonebattcomp

import android.app.Application
import android.content.Context
import android.os.BatteryManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class MainApplication : Application(), DefaultLifecycleObserver, CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default)

@Module
@InstallIn(SingletonComponent::class)
class CapabilityModule {
    @Provides
    fun provideCapabilityClient(
        @ApplicationContext context: Context
    ): CapabilityClient = Wearable.getCapabilityClient(context)
}

@Module
@InstallIn(SingletonComponent::class)
class NodeModule {
    @Provides
    fun provideNodeClient(
        @ApplicationContext context: Context
    ): NodeClient = Wearable.getNodeClient(context)
}

@Module
@InstallIn(SingletonComponent::class)
class RemoteActivityHelperModule {
    @Provides
    fun provideRemoteActivityHelper(
        @ApplicationContext context: Context
    ): RemoteActivityHelper = RemoteActivityHelper(context)
}

@Module
@InstallIn(SingletonComponent::class)
class DataClientModule {
    @Provides
    fun provideDataClient(
        @ApplicationContext context: Context
    ): DataClient =  Wearable.getDataClient(context)
}

@Module
@InstallIn(SingletonComponent::class)
class BatteryManagerModule {
    @Provides
    fun provideBatteryManager(
        @ApplicationContext context: Context
    ): BatteryManager =  context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
}