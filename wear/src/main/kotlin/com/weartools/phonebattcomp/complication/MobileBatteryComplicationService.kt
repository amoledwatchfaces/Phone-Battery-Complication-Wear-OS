/*
 * Copyright 2022 amoledwatchfaces™
 * support@amoledwatchfaces.com
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
package com.weartools.phonebattcomp.complication

import android.content.ComponentName
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.google.android.gms.wearable.DataClient
import com.weartools.phonebattcomp.MainApplication
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.DataStoreRepository
import com.weartools.phonebattcomp.receiver.ComplicationTapBroadcastReceiver
import com.weartools.phonebattcomp.receiver.ComplicationToggleArgs
import com.weartools.phonebattcomp.receiver.getCurrentBatteryChargingStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MobileBatteryComplicationService : SuspendingComplicationDataSourceService() {

    @Inject lateinit var repository: DataStoreRepository
    @Inject lateinit var dataClient: DataClient

    val phoneChargingIcon by lazy { Icon.createWithResource(this, R.drawable.ic_phone_charging_3) }
    val phoneConnectedIcon by lazy { Icon.createWithResource(this, R.drawable.ic_phone_icon) }
    val phoneDisconnectedIcon by lazy { Icon.createWithResource(this, R.drawable.ic_phone_disconnected) }
    val watchChargingIcon by lazy { Icon.createWithResource(this, R.drawable.ic_watch_charging_3) }
    val watchNormalIcon by lazy { Icon.createWithResource(this, R.drawable.ic_watch) }

    override fun onComplicationActivated(complicationInstanceId: Int, type: ComplicationType) {
        super.onComplicationActivated(complicationInstanceId, type)
        MobileListener.sendPhoneBatteryRequest(0,dataClient,true)
    }

    /**
     * IMPORTANT!!! (\uFEFF) is used for combining watch batteries
     * MONOCHROMATIC_IMAGE_AMBIENT is used for dual batteries support (Watch Icon Drawable) in SHORT_TEXT
     * SMALL_IMAGE_AMBIENT is used for dual batteries support (Watch Icon Drawable) in LONG_TEXT
     */

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = 86f,
                    min = 0f,
                    max = 100f,
                    contentDescription = ComplicationText.EMPTY)
                    .setText(PlainComplicationText.Builder(text = "86%").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone_icon)).build())
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "\uFEFF86%").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone_icon))
                        .setAmbientImage(Icon.createWithResource(this, R.drawable.ic_watch))
                        .build())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "\uFEFF86%").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone_icon)).build())
                    .setTitle(PlainComplicationText.Builder(text = "Pixel 9 Pro").build())
                    .setSmallImage(smallImage = SmallImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone_icon), type = SmallImageType.ICON)
                        .setAmbientImage(ambientImage = Icon.createWithResource(this, R.drawable.ic_watch))
                        .build())
                    .build()
            }

            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val complicationPendingIntent = ComplicationTapBroadcastReceiver.getToggleIntent(this,ComplicationToggleArgs(ComponentName(this, javaClass),request.complicationInstanceId))

        val batteryState = application as MainApplication

        val activeSync = repository.activeSync.first()
        val showPercentage = repository.percentage.first()

        /** When Active Sync is not enabled **/
        if (activeSync.not()) {
            if (batteryState.afterMobileResult.not()) {
                MobileListener.sendPhoneBatteryRequest(batteryState.lastUpdate.value?:0L, dataClient, false)
            }
            else {
                batteryState.afterMobileResult = false
            }
        }
        /** When Active Sync is enabled but last update time was reset **/
        else {
            if (batteryState.lastUpdate.value == null){
                MobileListener.sendPhoneBatteryRequest(0L, dataClient, true)
            }
        }
        //Log.i("MobileBatteryComplicationService", "lastUpdate: ${batteryState.lastUpdate.value}")
        //Log.i("MobileBatteryComplicationService", "batteryLevel: ${batteryState.phoneBatteryLevel}")

        val level = if (batteryState.phoneBatteryLevel == 0) "\uFEFF-" else "\uFEFF${batteryState.phoneBatteryLevel}${if (showPercentage) "%" else ""}"
        val phoneIcon = when {
            batteryState.phoneIsConnected && batteryState.phoneIsCharging -> phoneChargingIcon
            batteryState.phoneIsConnected -> phoneConnectedIcon
            else -> phoneDisconnectedIcon
        }
        val watchIcon = if (watchIsCharging?: getCurrentBatteryChargingStatus(this)) watchChargingIcon else watchNormalIcon

        return when (request.complicationType) {

            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = batteryState.phoneBatteryLevel.toFloat(),
                    min = 0f,
                    max = 100f,
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_at)+" $level").build())
                    .setText(PlainComplicationText.Builder(text = level).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = phoneIcon).build())
                    .setTapAction(complicationPendingIntent)
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = level).build(),
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_at)+" $level").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = phoneIcon)
                        .setAmbientImage(watchIcon)
                        .build())
                    .setTapAction(complicationPendingIntent)
                    .build()
            }
             ComplicationType.LONG_TEXT -> {
                 LongTextComplicationData.Builder(
                     text = PlainComplicationText.Builder(text = level).build(),
                     contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_at)+" $level").build())
                     .setMonochromaticImage(MonochromaticImage.Builder(image = phoneIcon).build())
                     .setTitle(PlainComplicationText.Builder(text = repository.nodeName.first()).build())
                     .setSmallImage(smallImage = SmallImage.Builder(image = phoneIcon, type = SmallImageType.ICON)
                         .setAmbientImage(watchIcon)
                         .build())
                     .setTapAction(complicationPendingIntent)
                     .build()
             }

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }
}