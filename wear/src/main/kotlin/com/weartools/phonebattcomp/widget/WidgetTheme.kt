package com.weartools.phonebattcomp.widget

import androidx.compose.remote.creation.compose.state.rc
import androidx.wear.compose.remote.material3.RemoteColorScheme
import com.weartools.phonebattcomp.theme.*

val WIDGET_COLOR_SCHEME = RemoteColorScheme(
    primary = primary.rc,
    primaryDim = primaryDim.rc,
    onPrimary = onPrimary.rc,
    primaryContainer = primaryContainer.rc,
    onPrimaryContainer = onPrimaryContainer.rc,

    secondary = secondary.rc,
    secondaryDim = secondaryDim.rc,
    onSecondary = onSecondary.rc,
    secondaryContainer = secondaryContainer.rc,
    onSecondaryContainer = onSecondaryContainer.rc,

    tertiary = tertiary.rc,
    tertiaryDim = tertiaryDim.rc,
    onTertiary = onTertiary.rc,
    tertiaryContainer = tertiaryContainer.rc,
    onTertiaryContainer = onTertiaryContainer.rc,

    surfaceContainerHigh = surfaceContainerHigh.rc,
    surfaceContainer = surfaceContainer.rc,
    surfaceContainerLow = surfaceContainerLow.rc,
    onSurface = onSurface.rc,
    onSurfaceVariant = onSurfaceVariant.rc,

    background = background.rc,
    onBackground = onBackground.rc,

    outline = outline.rc,
    outlineVariant = outlineVariant.rc,

    error = error.rc,
    errorDim = errorDim.rc,
    onError = onError.rc,
    errorContainer = errorContainer.rc,
    onErrorContainer = onErrorContainer.rc,
)
