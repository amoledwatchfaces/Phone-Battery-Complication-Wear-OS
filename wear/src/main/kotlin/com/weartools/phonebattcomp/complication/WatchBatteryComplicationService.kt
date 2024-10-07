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
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
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
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.data.UserPreferencesRepository
import com.weartools.phonebattcomp.receiver.getCurrentBatteryChargingStatus
import com.weartools.phonebattcomp.utils.updateComplication
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.time.Duration
import javax.inject.Inject

/** Global variables **/
var watchBatteryLevel: Int = 0
var watchIsCharging: Boolean? = null
val oneTimeWorkRequest = OneTimeWorkRequestBuilder<WatchBatteryUpdateWorker>()
    .setInitialDelay(Duration.ofSeconds(30))
    .build()
/** Use WorkManager to update Complication after 30 seconds
 * because battery level was same and can change anytime soon **/
fun scheduleComplicationUpdate(context: Context){
    WorkManager.getInstance(context).enqueueUniqueWork("watch_batt_update", ExistingWorkPolicy.REPLACE, oneTimeWorkRequest)
}

@AndroidEntryPoint
class WatchBatteryComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    private val batteryManager by lazy { applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager }

    fun openScreen(): PendingIntent? {
        val batteryIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
        return PendingIntent.getActivity(this, 0, batteryIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
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

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {

        val showPercentage = preferences.first().percentage
        val newBatteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        if (watchBatteryLevel != newBatteryLevel || watchBatteryLevel == 0) {
            watchBatteryLevel = newBatteryLevel
        }
        else if (watchIsCharging == true){
            scheduleComplicationUpdate(this)
        }
        else {
            scheduleComplicationUpdate(this)
        }

        val level = "$watchBatteryLevel${if (showPercentage) "%" else ""}"
        val watchIcon = if (watchIsCharging?: getCurrentBatteryChargingStatus(this)) Icon.createWithResource(this, R.drawable.ic_watch_charging_3) else Icon.createWithResource(this, R.drawable.ic_watch)

        return when (request.complicationType) {

            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = watchBatteryLevel.toFloat(),
                    min = 0f,
                    max = 100f,
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+" $watchBatteryLevel%").build())
                    .setText(PlainComplicationText.Builder(text = level).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = watchIcon).build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder (
                    text = PlainComplicationText.Builder(text = level).build(),
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+" $watchBatteryLevel%").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = watchIcon).build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = level).build(),
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+" $watchBatteryLevel%").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = watchIcon).build())
                    .setTitle(PlainComplicationText.Builder(text = getString(R.string.watch_battery_text)).build())
                    .build()
            }
            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(
                        Icon.createWithResource(this, when (watchBatteryLevel) {
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
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+" $watchBatteryLevel%").build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        image = Icon.createWithResource(this, when (watchBatteryLevel) {
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
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+" $watchBatteryLevel%").build())
                    .setTapAction(openScreen())
                    .build()
            }

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }

    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        super.onComplicationDeactivated(complicationInstanceId)
        WorkManager.getInstance(this).cancelUniqueWork("watch_batt_update")
    }
}

class WatchBatteryUpdateWorker(private val appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    private val batteryManager by lazy { applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager }
    override suspend fun doWork(): Result {

        /** When battery level changes, update complication, when not, schedule new update in 30 seconds **/
        val newBatteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        if (watchBatteryLevel != newBatteryLevel){ appContext.updateComplication(WatchBatteryComplicationService::class.java) }
        else { scheduleComplicationUpdate(appContext) }

        return Result.success()
    }
}


