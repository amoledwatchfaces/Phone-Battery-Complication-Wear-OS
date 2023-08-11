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
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ResourceBuilders
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.images.drawableResToImageResource
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer
import com.weartools.phonebattcomp.R


@OptIn(ExperimentalHorologistApi::class)
class PhoneBatteryTileRenderer(context: Context) :
    SingleTileLayoutRenderer<PhoneBatteryTileData.PhoneBatteryData, PhoneBatteryTileData>(context) {

    override fun renderTile(
        state: PhoneBatteryTileData.PhoneBatteryData,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
    ): LayoutElement {
        return phoneBatteryTileLayout(state, context, deviceParameters)
    }

    override fun ResourceBuilders.Resources.Builder.produceRequestedResources(
        resourceState: PhoneBatteryTileData,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        resourceIds: MutableList<String>
    ) {
            addIdToImageMapping("phone_icon", drawableResToImageResource(R.drawable.ic_phone_icon))
    }
}
