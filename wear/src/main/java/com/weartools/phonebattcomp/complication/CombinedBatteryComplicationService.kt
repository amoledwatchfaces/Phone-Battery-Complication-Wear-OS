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
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.BatteryManager
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.DataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class CombinedBatteryComplicationService : SuspendingComplicationDataSourceService() {

    private val repository by lazy { DataRepository(this) }

    override fun onComplicationActivated(complicationInstanceId: Int, type: ComplicationType)
    {
        super.onComplicationActivated(complicationInstanceId, type)
        MobileListener.sendPhoneBatteryRequest(0,this,true)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                // PHONE BATTERY
                text = PlainComplicationText.Builder(text = "86%").build(),
                contentDescription = PlainComplicationText.Builder(text = "86%").build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this,
                        R.drawable.ic_phone_icon
                    )).build())

                // WATCH BATTERY
                .setTitle(PlainComplicationText.Builder(text = "56%").build())
                .setSmallImage(smallImage = SmallImage.Builder(
                    image = Icon.createWithResource(this, R.drawable.ic_watch),
                    type = SmallImageType.ICON
                ).build(),)
                .setTapAction(null)
                .build()

            else -> {
                null
            }
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val args = ComplicationToggleArgs(providerComponent = ComponentName(this, javaClass), complicationInstanceId = request.complicationInstanceId)
        val complicationPendingIntent = ComplicationTapBroadcastReceiver.getToggleIntent(context = this, args = args)

        val hasResult = repository.afterMobileResult.first()
        val isWatchConnected = repository.isConnected.first()
        val lastUpdateTime = repository.lastUpdate.first()
        val level = repository.batteryLevel.first()
        val level2: String = if (level==0) "-" else "$level"
        val watchBatteryLevel = this.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))!!.getIntExtra(
            BatteryManager.EXTRA_LEVEL, 0)

        if (!hasResult) { MobileListener.sendPhoneBatteryRequest(lastUpdateTime, applicationContext, false) }
        else { runBlocking { repository.storeResult(false) } }

         return when (request.complicationType) {

             /**
              * PHONE BATTERY = TEXT + MONOCHROMATIC IMAGE
              * WATCH BATTERY = TITLE + SMALL IMAGE
              */

             ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                 // PHONE BATTERY
                 text = PlainComplicationText.Builder(text = level2).build(),
                 contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_at)+level2).build())
                 .setMonochromaticImage(
                     if (isWatchConnected) { MonochromaticImage.Builder(image = Icon.createWithResource(this,
                         R.drawable.ic_phone_icon
                     )).build()}
                     else { MonochromaticImage.Builder(image = Icon.createWithResource(this,
                         R.drawable.ic_phone_disconnected
                     )).build()})

                 // WATCH BATTERY
                 .setTitle(PlainComplicationText.Builder(text = "$watchBatteryLevel").build())
                 .setSmallImage(smallImage = SmallImage.Builder(
                     image = Icon.createWithResource(this, R.drawable.ic_watch_icon_combined_smaller),
                     type = SmallImageType.ICON
                 ).build())
                 .setTapAction(complicationPendingIntent)
                 .build()

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }
}