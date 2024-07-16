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
import com.weartools.phonebattcomp.data.DataStoreRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MobileBatteryComplicationService : SuspendingComplicationDataSourceService() {

    @Inject lateinit var repository: DataStoreRepository
    @Inject lateinit var dataClient: DataClient

    override fun onComplicationActivated(complicationInstanceId: Int, type: ComplicationType)
    {
        super.onComplicationActivated(complicationInstanceId, type)
        MobileListener.sendPhoneBatteryRequest(0,dataClient,true)
    }

    // TODO: IMPORTANT!!! (\uFEFF) is used for combining watch batteries

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = 86f,
                min = 0f,
                max = 100f,
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_preview_desc)).build())
                .setText(PlainComplicationText.Builder(text = "86%").build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this,
                    R.drawable.ic_phone_icon
                )).build())
                .setTapAction(null)
                .build()

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "\uFEFF86%").build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_preview_desc)).build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this,
                    R.drawable.ic_phone_icon
                )).build())
                .setTapAction(null)
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "\uFEFF86%").build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_preview_desc)).build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this,
                    R.drawable.ic_phone_icon
                )).build())
                .setTitle(PlainComplicationText.Builder(text = "Phone Battery").build())
                .setTapAction(null)
                .build()
            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val args = ComplicationToggleArgs(providerComponent = ComponentName(this, javaClass), complicationInstanceId = request.complicationInstanceId)
        val complicationPendingIntent = ComplicationTapBroadcastReceiver.getToggleIntent(context = this, args = args)

        //Log.d("COMPLICATION UPDATE", "Updating Phone Battery Complication")

        if (!repository.activeSync.first()) {
            if (!repository.afterMobileResult.first()) {
                MobileListener.sendPhoneBatteryRequest(repository.lastUpdate.first(), dataClient, false)
            }
            else {
                repository.storeResult(false)
            }
        }

        val isCharging = repository.isCharging.first()
        val isWatchConnected = repository.isConnected.first()
        val percentage = if (repository.percentage.first()) "%" else ""
        val level = repository.batteryLevel.first()
        val level2: String = if (level==0) "-" else "\uFEFF$level$percentage"
        val icon = when {
            isWatchConnected && isCharging -> Icon.createWithResource(this, R.drawable.ic_phone_charging_3)
            isWatchConnected -> Icon.createWithResource(this, R.drawable.ic_phone_icon)
            else -> Icon.createWithResource(this, R.drawable.ic_phone_disconnected)
        }

         return when (request.complicationType) {

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                    value = level.toFloat(),
                    min = 0f,
                    max = 100f,
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_at)+level2).build())
                    .setText(PlainComplicationText.Builder(text = level2).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = icon).build())
                    .setTapAction(complicationPendingIntent)
                    .build()

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = level2).build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_at)+level2).build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = icon).build())
                // TODO: SMALL_IMAGE_AMBIENT is used for combining watch batteries
                .setSmallImage(smallImage = SmallImage.Builder(image = icon, type = SmallImageType.ICON).setAmbientImage(ambientImage = Icon.createWithResource(this, R.drawable.ic_watch)).build())
                .setTapAction(complicationPendingIntent)
                .build()

             ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                 text = PlainComplicationText.Builder(text = level2).build(),
                 contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_at)+level2).build())
                 .setMonochromaticImage(MonochromaticImage.Builder(image = icon).build())
                 .setTitle(PlainComplicationText.Builder(text = "Phone Battery").build())
                 // TODO: SMALL_IMAGE_AMBIENT is used for combining watch batteries
                 .setSmallImage(smallImage = SmallImage.Builder(image = icon, type = SmallImageType.ICON).setAmbientImage(ambientImage = Icon.createWithResource(this, R.drawable.ic_watch)).build())
                 .setTapAction(complicationPendingIntent)
                 .build()

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }
}