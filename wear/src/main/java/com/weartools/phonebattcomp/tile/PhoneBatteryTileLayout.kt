/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weartools.phonebattcomp.tile

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.ProgressIndicatorColors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.images.drawableResToImageResource
import com.weartools.phonebattcomp.R

internal fun phoneBatteryTileLayout(
    state: PhoneBatteryTileData.PhoneBatteryData,
    context: Context,
    deviceParameters: DeviceParameters,
) = EdgeContentLayout.Builder(deviceParameters)
    .setEdgeContent(
        CircularProgressIndicator.Builder()
            .setProgress(state.batteryLevel.toFloat() / 100f)
            .setCircularProgressIndicatorColors(
                ProgressIndicatorColors(
                    ColorBuilders.argb(TileColors.Blue),
                    ColorBuilders.argb(TileColors.White10Pc)
            ))
            .build()


    )

    .setPrimaryLabelTextContent(
        Text.Builder(context, state.nodeName)
            .setTypography(Typography.TYPOGRAPHY_CAPTION1)
            .setColor(ColorBuilders.argb(TileColors.Blue))
            .build()
    )
    .setSecondaryLabelTextContent(
        Text.Builder(context, "Power")
            .setTypography(Typography.TYPOGRAPHY_CAPTION1)
            .setColor(ColorBuilders.argb(TileColors.Gray))
            .build()
    )
    .setContent(
        Text.Builder(context, "${state.batteryLevel}%")
            .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
            .setColor(ColorBuilders.argb(TileColors.White))
            .setModifiers(ModifiersBuilders.Modifiers.Builder()
                .setClickable(launchAppClickable(openApp())).build())
            .build()
    )
    .build()

/*
    .setContent(
        LayoutElementBuilders.Image.Builder()
            .setWidth(DimensionBuilders.dp(32f))
            .setHeight(DimensionBuilders.dp(32f))
            .setResourceId("phone_icon")
            .build()
    )
 */

internal fun launchAppClickable(
    androidActivity: ActionBuilders.AndroidActivity
) = ModifiersBuilders.Clickable.Builder()
    .setOnClick(
        ActionBuilders.LaunchAction.Builder()
            .setAndroidActivity(androidActivity)
            .build()
    )
    .build()

internal fun openApp() = ActionBuilders.AndroidActivity.Builder()
    .setPackageName("com.weartools.phonebattcomp")
    .setClassName("com.weartools.phonebattcomp.MainActivity")
    .build()

@WearLargeRoundDevicePreview
@Composable
private fun MessageTilePreview() {
    val context = LocalContext.current
    val state = PhoneBatteryTileData.PhoneBatteryData(50, "Pixel 2XL")
    LayoutRootPreview(
        phoneBatteryTileLayout(
            state,
            context,
            buildDeviceParameters(context.resources)
        )
    ) {
        addIdToImageMapping(
            "phone_icon",
            drawableResToImageResource(R.drawable.ic_phone_icon)
        )
    }
}

object TileColors {
    val Black = Color.Black.toArgb()
    val Blue = android.graphics.Color.parseColor("#b9f7ff")
    val Gray = android.graphics.Color.parseColor("#BDC1C6")
    val White = Color.White.toArgb()
    val White10Pc = Color(1f, 1f, 1f, 0.1f).toArgb()
}