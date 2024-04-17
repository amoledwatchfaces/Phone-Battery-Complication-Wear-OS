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
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.DataRepository
import kotlinx.coroutines.flow.first

class MobileBatteryComplicationService : SuspendingComplicationDataSourceService() {

    private val repository by lazy { DataRepository(this) }

    override fun onComplicationActivated(complicationInstanceId: Int, type: ComplicationType)
    {
        super.onComplicationActivated(complicationInstanceId, type)
        MobileListener.sendPhoneBatteryRequest(0,this,true)
    }

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
                text = PlainComplicationText.Builder(text = "86%").build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_preview_desc)).build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this,
                    R.drawable.ic_phone_icon
                )).build())
                .setTapAction(null)
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "100%").build(),
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

        if (!repository.activeSync.first()) {
            if (!repository.afterMobileResult.first()) {
                MobileListener.sendPhoneBatteryRequest(repository.lastUpdate.first(), applicationContext, false)
            }
            else {
                repository.storeResult(false)
            }
        }

        val isCharging = repository.isCharging.first()
        val isWatchConnected = repository.isConnected.first()
        val percentage = if (repository.percentage.first()) "%" else ""
        val level = repository.batteryLevel.first()
        val level2: String = if (level==0) "-" else "$level$percentage"
        val icon = when {
            isWatchConnected && isCharging -> MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone_charging_3)).build()
            isWatchConnected -> MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone_icon)).build()
            else -> MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone_disconnected)).build()
        }

         return when (request.complicationType) {

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                    value = level.toFloat(),
                    min = 0f,
                    max = 100f,
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_at)+level2).build())
                    .setText(PlainComplicationText.Builder(text = level2).build())
                    .setMonochromaticImage(icon)
                    .setTapAction(complicationPendingIntent)
                    .build()

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = level2).build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_at)+level2).build())
                .setMonochromaticImage(icon)
                .setTapAction(complicationPendingIntent)
                .build()

             ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                 text = PlainComplicationText.Builder(text = level2).build(),
                 contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_at)+level2).build())
                 .setMonochromaticImage(icon)
                 .setTitle(PlainComplicationText.Builder(text = "Phone Battery").build())
                 .setTapAction(complicationPendingIntent)
                 .build()

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }
}