package com.weartools.phonebattcomp

import android.app.Application
import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class MainApplication : Application()

@Module
@InstallIn(SingletonComponent::class)
class DataClientModule {
    @Provides
    fun provideDataClient(
        @ApplicationContext context: Context
    ): DataClient =  Wearable.getDataClient(context)
}
