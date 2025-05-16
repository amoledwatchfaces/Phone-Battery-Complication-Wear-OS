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
import androidx.work.WorkManager
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.receiver.getCurrentBatteryChargingStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

var watchIsCharging: Boolean? = null

@AndroidEntryPoint
class WatchBatteryComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>

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

        val repository = dataStore.data.first()
        val showPercentage = repository.percentage
        val materialSymbols = repository.materialSymbols
        val chargingSymbolInside = repository.chargingSymbolInsideIcon
        val watchCharging = watchIsCharging?: getCurrentBatteryChargingStatus(this)

        val batteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        val level = "$batteryLevel${if (showPercentage) "%" else ""}"

        val watchIcon = Icon.createWithResource(this,
            when {
                watchCharging && materialSymbols && chargingSymbolInside -> R.drawable.ic_watch_charging_inside_material_symbols
                watchCharging && materialSymbols -> R.drawable.ic_watch_charging_material_symbols
                watchCharging && chargingSymbolInside -> R.drawable.ic_watch_charging_inside
                watchCharging -> R.drawable.ic_watch_charging
                materialSymbols -> R.drawable.ic_watch_material_symbols
                else -> R.drawable.ic_watch
            }
        )
        return when (request.complicationType) {

            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = batteryLevel.toFloat().coerceIn(0f, 100f),
                    min = 0f,
                    max = 100f,
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+" $batteryLevel%").build())
                    .setText(PlainComplicationText.Builder(text = level).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = watchIcon).build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder (
                    text = PlainComplicationText.Builder(text = level).build(),
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+" $batteryLevel%").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = watchIcon).build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = level).build(),
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+" $batteryLevel%").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = watchIcon).build())
                    .setTitle(PlainComplicationText.Builder(text = getString(R.string.watch_battery_text)).build())
                    .build()
            }
            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(
                        Icon.createWithResource(this, when (batteryLevel) {
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
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+" $batteryLevel%").build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        image = Icon.createWithResource(this, when (batteryLevel) {
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
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+" $batteryLevel%").build())
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


