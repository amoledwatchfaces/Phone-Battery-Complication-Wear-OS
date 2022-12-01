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
package com.weartools.phonebattcomp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.BatteryManager
import android.util.Log
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import java.text.DecimalFormat

class WatchVoltageComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType)
    {
        super.onComplicationActivated(complicationInstanceId, type)
        Log.d(TAG, "activated: $complicationInstanceId")
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData {
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "5.00V").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.voltage_battery_text)).build())
            .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_voltage)).build())
            .setTapAction(null)
            .build()
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        Log.d(TAG, "Update: ${request.complicationInstanceId}")

        val complicationPendingIntent = TempVoltageTapBroadcastReceiver.getToggleIntent(
            this,
            ComponentName(this,javaClass),
            request.complicationInstanceId
        )

        val voltage = this.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))!!.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toFloat() / 1000
        val unit = "V"

        val df = DecimalFormat("#.##")
        val level = df.format(voltage)

        return when (request.complicationType) {

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "$level$unit").build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.voltage_battery_at)+level+getString(R.string.voltage_volts)).build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_voltage)).build())
                .setTapAction(complicationPendingIntent)
                .build()

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }

    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        super.onComplicationDeactivated(complicationInstanceId)
        Log.d(TAG, "Deactivated: $complicationInstanceId")
    }

    companion object {
        private val TAG = WatchVoltageComplicationService::class.java.simpleName
        @JvmStatic
        fun updateComplication(context: Context?) {
            Log.d(TAG, "Updating Watch Battery Voltage Complication")
            val componentName = ComponentName(context!!, WatchVoltageComplicationService::class.java)
            val req = ComplicationDataSourceUpdateRequester.create(context,componentName)
            req.requestUpdateAll()
        }
    }
}