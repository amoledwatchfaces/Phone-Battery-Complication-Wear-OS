package com.weartools.phonebattcomp.di

import android.os.Handler
import android.os.Looper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)  // This will make it available application-wide
object CalendarObserverModule {

    @Provides
    @Singleton
    fun provideHandler(): Handler {
        return Handler(Looper.getMainLooper())
    }
}
