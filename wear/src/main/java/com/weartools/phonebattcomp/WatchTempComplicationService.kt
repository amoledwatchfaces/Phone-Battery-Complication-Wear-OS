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
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

class WatchTempComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType)
    {
        super.onComplicationActivated(complicationInstanceId, type)
        Log.d(TAG, "activated: $complicationInstanceId")
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData {
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "35°").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.temp_battery_text)).build())
            .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_temp)).build())
            .setTapAction(null)
            .build()
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        Log.d(TAG, "Update: ${request.complicationInstanceId}")

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val tempunit = preferences.getBoolean(getString(R.string.temp_unit), true)
        val temp = (this.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))!!.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10

        val complicationPendingIntent = TempVoltageTapBroadcastReceiver.getToggleIntent(
            this,
            ComponentName(this,javaClass),
            request.complicationInstanceId
        )

        val level = if (tempunit) temp else temp*9/5+32
        val unit = if (tempunit) "°C" else "°F"


        return when (request.complicationType) {

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "$level$unit").build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.temp_battery_at)+level+unit).build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_temp)).build())
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
        private val TAG = WatchTempComplicationService::class.java.simpleName
        @JvmStatic
        fun updateComplication(context: Context?) {
            Log.d(TAG, "Updating Watch Battery Temperature Complication")
            val componentName = ComponentName(context!!, WatchTempComplicationService::class.java)
            val req = ComplicationDataSourceUpdateRequester.create(context,componentName)
            req.requestUpdateAll()
        }
    }
}