package com.weartools.phonebattcomp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.wearable.CapabilityClient
import com.weartools.phonebattcomp.screens.MainApp
import com.weartools.phonebattcomp.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Use Hilt to inject the ViewModel
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var capabilityClient: CapabilityClient

    // Listener that forwards events to ViewModel
    private val listener = CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
        viewModel.onCapabilityChanged(capabilityInfo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        try {
            capabilityClient.addListener(listener, BuildConfig.CAPABILITY_WEAR_APP)
        } catch (e: Exception) {
            Log.e("MainActivity", "addListener failed: ${e.message}")
        }

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

    override fun onPause() {
        super.onPause()
        try {
            capabilityClient.removeListener(listener)
        } catch (e: Exception) {
            Log.e("MainActivity", "removeListener failed: ${e.message}")
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            capabilityClient.removeListener(listener)
        } catch (e: Exception) {
            Log.e("MainActivity", "removeListener failed: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            capabilityClient.removeListener(listener)
        } catch (e: Exception) {
            Log.e("MainActivity", "removeListener failed: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            capabilityClient.addListener(listener, BuildConfig.CAPABILITY_WEAR_APP)
        } catch (e: Exception) {
            Log.e("MainActivity", "addListener failed: ${e.message}")
        }
    }
}
