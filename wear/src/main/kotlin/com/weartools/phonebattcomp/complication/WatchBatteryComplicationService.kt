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
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.BatteryManager
import android.provider.Settings
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
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
import com.weartools.phonebattcomp.data.DataStoreRepository
import com.weartools.phonebattcomp.receiver.WatchBatteryReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class WatchBatteryComplicationService : SuspendingComplicationDataSourceService() {

    @Inject lateinit var repository: DataStoreRepository
    private val batteryManager by lazy { applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager }

    fun openScreen(): PendingIntent? {
        val batteryIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
        return PendingIntent.getActivity(this, 0, batteryIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun getCurrentBatteryLevel(): Int {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = 35f,
                    min = 0f,
                    max = 100f,
                    contentDescription = ComplicationText.EMPTY)
                    .setText(PlainComplicationText.Builder(text = "35%").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_watch)).build())
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "35%").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_watch)).build())
                    .build()
            }
            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(Icon.createWithResource(this, R.drawable.ic_battery_7)).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        image = Icon.createWithResource(this, R.drawable.ic_battery_7),
                        type = SmallImageType.ICON).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }

            else -> {null}
        }
    }
    override fun onComplicationActivated(complicationInstanceId: Int, type: ComplicationType) {
        WatchBatteryReceiver.subscribeToUpdates(applicationContext)
    }
    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        WatchBatteryReceiver.unsubscribeFromUpdates(applicationContext)
    }
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {

        val percentage = if (repository.percentage.first()) "%" else ""
        var level = repository.watchBatteryLevel.first()
        //val isCharging = repository.watchIsCharging.first()

        if (WatchBatteryReceiver.isSubscribed.not()) {
            // Set current battery level with Battery Manager
            level = getCurrentBatteryLevel()
            // Subscribe to battery updates
            WatchBatteryReceiver.subscribeToUpdates(applicationContext)
        }

        return when (request.complicationType) {

            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = level.toFloat(),
                    min = 0f,
                    max = 100f,
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+"$level%").build())
                    .setText(PlainComplicationText.Builder(text = "$level$percentage").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_watch)).build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder (
                    text = PlainComplicationText.Builder(text = "$level$percentage").build(),
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+"$level%").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_watch)).build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(
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
            }
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
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
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+"$level%").build())
                    .setTapAction(openScreen())
                    .build()
            }

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }
}


