package com.weartools.phonebattcomp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val darkColorScheme = darkColorScheme(
    primary = Color(0xFFC9F6FF),
    primaryContainer = Color(0xFF18363C),
    onPrimaryContainer = Color(0xFFDBF9FF),
    secondaryContainer = Color(0xFF2B5F69),
    onSecondaryContainer = Color(0xFFDCF9FF),

    background = Color(0xFF141414),
    onBackground = Color(0xFFE5EBEC),
    surface = Color(0xFF131313),
    surfaceContainer = Color(0xFF1E2425),
    onSurface = Color(0xFFE5EDEE),
    surfaceVariant = Color(0xFF41494B),
    onSurfaceVariant = Color(0xFFC1CCCE),

    )

private val lightColorScheme = lightColorScheme(
    primary = Color(0xFF6FE4FF),
    primaryContainer = Color(0xFFDDF9FF),
    onPrimaryContainer = Color(0xFF003944),
    secondaryContainer = Color(0xFFE0FAFF),
    onSecondaryContainer = Color(0xFF112124),

    background = Color(0xFFF7FEFF),
    onBackground = Color(0xFF161D1F),
    surface = Color(0xFFF7FEFF),
    surfaceContainer = Color(0xFFEDF6F8),
    onSurface = Color(0xFF181F20),
    surfaceVariant = Color(0xFF434B4D),
    onSurfaceVariant = Color(0xFF434B4D),
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        /*
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
         */

        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme.not()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}