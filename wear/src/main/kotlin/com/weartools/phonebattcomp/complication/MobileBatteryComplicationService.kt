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
import androidx.datastore.core.DataStore
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
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.receiver.ComplicationTapBroadcastReceiver
import com.weartools.phonebattcomp.receiver.ComplicationToggleArgs
import com.weartools.phonebattcomp.receiver.getCurrentBatteryChargingStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MobileBatteryComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>

    @Inject
    lateinit var dataClient: DataClient

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
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone)).build())
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "\uFEFF86%").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone))
                        .setAmbientImage(Icon.createWithResource(this, R.drawable.ic_watch))
                        .build())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "\uFEFF86%").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone)).build())
                    .setTitle(PlainComplicationText.Builder(text = "Pixel 9 Pro").build())
                    .setSmallImage(smallImage = SmallImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone), type = SmallImageType.ICON)
                        .setAmbientImage(ambientImage = Icon.createWithResource(this, R.drawable.ic_watch))
                        .build())
                    .build()
            }

            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val complicationPendingIntent = ComplicationTapBroadcastReceiver.getToggleIntent(this,ComplicationToggleArgs(ComponentName(this, javaClass),request.complicationInstanceId))

        val repository = dataStore.data.first()

        val activeSync = repository.activeSync
        val showPercentage = repository.percentage
        val materialSymbols = repository.materialSymbols
        val chargingSymbolInside = repository.chargingSymbolInsideIcon
        val watchCharging = watchIsCharging?: getCurrentBatteryChargingStatus(this)

        /** When Active Sync is not enabled **/
        if (activeSync.not()) {
            if (repository.afterMobileResult.not()) {
                MobileListener.sendPhoneBatteryRequest(repository.lastUpdate, dataClient, false)
            }
            else {
                dataStore.updateData { it.copy(afterMobileResult = false) }
            }
        }

        //Log.i("MobileBatteryComplicationService", "lastUpdate: ${batteryState.lastUpdate.value}")
        //Log.i("MobileBatteryComplicationService", "batteryLevel: ${batteryState.phoneBatteryLevel}")

        val level = if (repository.phoneBatteryLevel == 0) "\uFEFF-" else "\uFEFF${repository.phoneBatteryLevel}${if (showPercentage) "%" else ""}"
        val phoneIcon = Icon.createWithResource(this,
                when {
                    repository.phoneIsConnected && repository.phoneIsCharging && materialSymbols && chargingSymbolInside -> R.drawable.ic_phone_charging_inside_material_symbols
                    repository.phoneIsConnected && repository.phoneIsCharging && materialSymbols -> R.drawable.ic_phone_charging_material_symbols
                    repository.phoneIsConnected && repository.phoneIsCharging && chargingSymbolInside -> R.drawable.ic_phone_charging_inside
                    repository.phoneIsConnected && repository.phoneIsCharging -> R.drawable.ic_phone_charging
                    repository.phoneIsConnected && materialSymbols -> R.drawable.ic_phone_material_symbols
                    repository.phoneIsConnected -> R.drawable.ic_phone
                    materialSymbols -> R.drawable.ic_phone_disconnected_material_symbols
                    else -> R.drawable.ic_phone_disconnected
                }
            )

        val watchIcon = Icon.createWithResource(this,
                when {
                    watchCharging && materialSymbols && chargingSymbolInside -> R.drawable.ic_watch_charging_inside_material_symbols
                    watchCharging && materialSymbols -> R.drawable.ic_watch_charging_material_symbols
                    watchCharging && chargingSymbolInside -> R.drawable.ic_watch_charging_inside
                    watchCharging -> R.drawable.ic_watch_charging
                    materialSymbols -> R.drawable.ic_watch_material_symbols
                    else -> R.drawable.ic_watch
                }
            )

        return when (request.complicationType) {

            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = repository.phoneBatteryLevel.toFloat().coerceIn(0f, 100f),
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
                     .setTitle(PlainComplicationText.Builder(text = repository.nodeName).build())
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