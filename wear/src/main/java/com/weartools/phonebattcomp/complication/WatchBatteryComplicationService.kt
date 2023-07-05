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

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.BatteryManager
import android.provider.Settings
import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.DataRepository
import kotlinx.coroutines.flow.first

class WatchBatteryComplicationService : SuspendingComplicationDataSourceService() {

    private val repository by lazy { DataRepository(this) }

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType)
    {
        super.onComplicationActivated(complicationInstanceId, type)
        Log.d(TAG, "activated: $complicationInstanceId")

    }
    fun openScreen(): PendingIntent? {
        val batteryIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
        batteryIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 0, batteryIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = 35f,
                min = 0f,
                max = 100f,
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_text)).build())
                .setText(PlainComplicationText.Builder(text = "35%").build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this,
                    R.drawable.ic_watch
                )).build())
                .setTapAction(null)
                .build()

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "35%").build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_text)).build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this,
                    R.drawable.ic_watch
                )).build())
                .setTapAction(null)
                .build()
            ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
                monochromaticImage = MonochromaticImage.Builder(
                    Icon.createWithResource(this, R.drawable.ic_battery_7)
                )
                    .build(),
                contentDescription = PlainComplicationText.Builder(text = "MONO_IMG.").build()
            )
                .setTapAction(null)
                .build()
            ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
                smallImage = SmallImage.Builder(
                    image = Icon.createWithResource(this, R.drawable.ic_battery_7),
                    type = SmallImageType.ICON
                ).build(),
                contentDescription = PlainComplicationText.Builder(text = "SMALL_IMAGE.").build()
            )
                .setTapAction(null)
                .build()
            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        Log.d(TAG, "Updating Watch Battery Complication")

        val percentage = if (repository.percentage.first()) "%" else ""
        val level = this.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))!!.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)

        return when (request.complicationType) {

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = level.toFloat(),
                min = 0f,
                max = 100f,
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+"$level%").build())
                .setText(PlainComplicationText.Builder(text = "$level$percentage").build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this,
                    R.drawable.ic_watch
                )).build())
                .setTapAction(openScreen())
                .build()

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder (
                text = PlainComplicationText.Builder(text = "$level$percentage").build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+"$level%").build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this,
                    R.drawable.ic_watch
                )).build())
                .setTapAction(openScreen())
                .build()

            ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
                monochromaticImage = MonochromaticImage.Builder(
                    Icon.createWithResource(this, when (level) {
                        in 0..5 -> R.drawable.ic_batt_low
                        in 6..15 -> R.drawable.ic_battery_1
                        in 16..25 -> R.drawable.ic_battery_2
                        in 26..35 -> R.drawable.ic_battery_3
                        in 36..45 -> R.drawable.ic_battery_4
                        in 46..55 -> R.drawable.ic_battery_5
                        in 56..65 -> R.drawable.ic_battery_6
                        in 66..75 -> R.drawable.ic_battery_7
                        in 76..85 -> R.drawable.ic_battery_8
                        in 86..95 -> R.drawable.ic_battery_9
                        else -> R.drawable.ic_battery_10
                    })
                ).build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+"$level%").build())
                .setTapAction(openScreen())
                .build()


            ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
                smallImage = SmallImage.Builder(
                    image = Icon.createWithResource(this, when (level) {
                        in 0..5 -> R.drawable.ic_batt_low
                        in 6..15 -> R.drawable.ic_battery_1
                        in 16..25 -> R.drawable.ic_battery_2
                        in 26..35 -> R.drawable.ic_battery_3
                        in 36..45 -> R.drawable.ic_battery_4
                        in 46..55 -> R.drawable.ic_battery_5
                        in 56..65 -> R.drawable.ic_battery_6
                        in 66..75 -> R.drawable.ic_battery_7
                        in 76..85 -> R.drawable.ic_battery_8
                        in 86..95 -> R.drawable.ic_battery_9
                        else -> R.drawable.ic_battery_10
                    }),
                    type = SmallImageType.ICON
                ).build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+"$level%").build()
            )
                .setTapAction(openScreen())
                .build()

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }

    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        super.onComplicationDeactivated(complicationInstanceId)
        Log.d(TAG, "Deactivated: $complicationInstanceId")
    }

    companion object {
        private val TAG = WatchBatteryComplicationService::class.java.simpleName
    }

}


