package com.weartools.phonebattcomp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import com.weartools.phonebattcomp.screens.MainApp
import com.weartools.phonebattcomp.ui.theme.MyApplicationTheme
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT,
        ))

        setContent {

            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

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
