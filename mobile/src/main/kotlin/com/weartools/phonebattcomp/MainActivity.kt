package com.weartools.phonebattcomp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.weartools.phonebattcomp.screens.MainApp
import com.weartools.phonebattcomp.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val passiveDataRepository = (application as MainApplication).dataRepository


        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT,
        ))

        setContent {

            MyApplicationTheme {
                MainApp(
                    dataRepository = passiveDataRepository
                )
            }
        }
    }
}
