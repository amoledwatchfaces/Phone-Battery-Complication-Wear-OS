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
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.receiver.ComplicationTapBroadcastReceiver
import com.weartools.phonebattcomp.receiver.ComplicationToggleArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class WatchTempComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>

    fun getCurrentBatteryTemperature(): Int {
        val batteryStatus = this.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)?.div(10) ?: 0
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "35°C").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_temp)).build())
                    .build()
            }

            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val args = ComplicationToggleArgs(providerComponent = ComponentName(this, javaClass), complicationInstanceId = request.complicationInstanceId)
        val complicationPendingIntent = ComplicationTapBroadcastReceiver.getToggleIntent(context = this, args = args)

        val tempunit = dataStore.data.first().tempUnit

        val temp = getCurrentBatteryTemperature()

        val level = if (tempunit) temp else temp*9/5+32
        val unit = if (tempunit) "°C" else "°F"

        return when (request.complicationType) {

            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "$level$unit").build(),
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.temp_battery_at)+" $level$unit").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_temp)).build())
                    .setTapAction(complicationPendingIntent)
                    .build()
            }

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }
}