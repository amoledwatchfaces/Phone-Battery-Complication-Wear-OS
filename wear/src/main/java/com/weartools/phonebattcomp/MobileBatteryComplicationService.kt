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
import android.graphics.drawable.Icon
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

class MobileBatteryComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType)
        {
        super.onComplicationActivated(complicationInstanceId, type)

        Log.d(TAG, "activated: $complicationInstanceId")

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        val hasResult = preferences.getBoolean(getString(R.string.key_pref_after_mobile_result), false)
        val isWatchConnected = preferences.getBoolean(getString(R.string.key_pref_connected), true)

        editor
            .putBoolean(getString(R.string.key_pref_battery_complication_activated), true)
            .putInt(getString(R.string.key_pref_battery_complication_id), complicationInstanceId).apply()

        if (!hasResult && isWatchConnected) { SendMessageService.sndMSG(this,"/request_battery") }
        else { editor.putBoolean(getString(R.string.key_pref_after_mobile_result), false) }
        editor.apply()
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = 86f,
                min = 0f,
                max = 100f,
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_preview_desc)).build())
                .setText(PlainComplicationText.Builder(text = "86%").build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone_icon)).build())
                .setTapAction(null)
                .build()

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "86%").build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_preview_desc)).build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone_icon)).build())
                .setTapAction(null)
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = getString(R.string.phone_battery_long_text)+"86%").build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_preview_desc)).build())
                .setTapAction(null)
                .build()
            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        Log.d(TAG, "Update: ${request.complicationInstanceId}")

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        editor.putInt(getString(R.string.key_pref_battery_complication_id), request.complicationInstanceId)

        val hasResult = preferences.getBoolean(getString(R.string.key_pref_after_mobile_result), false)
        val isWatchConnected = preferences.getBoolean(getString(R.string.key_pref_connected), false)
        val level = preferences.getInt(getString(R.string.key_pref_mobile_battery_level), 0)
        val level2: String = if (level==0) "-" else "$level%"

        val complicationPendingIntent = MobileBatteryComplicationTapBroadcastReceiver.getToggleIntent(
            this,
            ComponentName(this,javaClass),
            request.complicationInstanceId
        )

        if (!hasResult) { SendMessageService.sndMSG(this,"/request_battery") }
        else { editor.putBoolean(getString(R.string.key_pref_after_mobile_result), false) }
        editor.apply()

         return when (request.complicationType) {

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                    value = level.toFloat(),
                    min = 0f,
                    max = 100f,
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_at)+level2).build())
                    .setText(PlainComplicationText.Builder(text = level2).build())
                    .setMonochromaticImage(
                        if (isWatchConnected) { MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone_icon)).build()}
                        else { MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone_disconnected)).build()})
                    .setTapAction(complicationPendingIntent)
                    .build()

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = level2).build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_at)+level2).build())
                .setMonochromaticImage(
                    if (isWatchConnected) { MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone_icon)).build()}
                    else { MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_phone_disconnected)).build()})
                .setTapAction(complicationPendingIntent)
                .build()

             ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                 text = PlainComplicationText.Builder(text = getString(R.string.phone_battery_long_text)+level2).build(),
                 contentDescription = PlainComplicationText.Builder(text = getString(R.string.phone_battery_at)+level2).build())
                 .setTapAction(complicationPendingIntent)
                 .build()

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }

    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        super.onComplicationDeactivated(complicationInstanceId)
        Log.d(TAG, "Deactivated: $complicationInstanceId")
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        editor.putBoolean(getString(R.string.key_pref_battery_complication_activated), false)
        editor.apply()
    }

    companion object {
        private val TAG = MobileBatteryComplicationService::class.java.simpleName
        @JvmStatic
        fun updateBatteryComplication(context: Context?) {
            Log.d(TAG, "Updating Phone Battery Complication")
            val componentName = ComponentName(context!!, MobileBatteryComplicationService::class.java)
            val req = ComplicationDataSourceUpdateRequester.create(context,componentName)
            req.requestUpdateAll()
        }
    }
}