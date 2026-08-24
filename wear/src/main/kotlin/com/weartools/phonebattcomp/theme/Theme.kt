package com.weartools.phonebattcomp.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography
import androidx.wear.compose.material3.dynamicColorScheme


private val appColorScheme = ColorScheme(
    primary = primary,
    primaryDim = primaryDim,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,

    secondary = secondary,
    secondaryDim = secondaryDim,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,

    tertiary = tertiary,
    tertiaryDim = tertiaryDim,
    onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,

    surfaceContainerHigh = surfaceContainerHigh,
    surfaceContainer = surfaceContainer,
    surfaceContainerLow = surfaceContainerLow,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,

    outline = outline,
    outlineVariant = outlineVariant,

    background = background,
    onBackground = onBackground,

    error = error,
    errorDim = errorDim,
    onError = onError,
    errorContainer = errorContainer,
    onErrorContainer = onErrorContainer,
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

@Composable
fun PhoneBatteryAppTheme(
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    MaterialTheme(
        colorScheme = if (useDynamicColor) dynamicColorScheme(context) ?: appColorScheme else appColorScheme,
        content = content
    )
}

val Typography = Typography()