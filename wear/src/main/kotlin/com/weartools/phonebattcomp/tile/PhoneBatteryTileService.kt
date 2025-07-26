package com.weartools.phonebattcomp.tile

import android.os.Handler
import android.text.format.DateUtils
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import androidx.datastore.core.DataStore
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.ColorFilter
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
import androidx.wear.protolayout.LayoutElementBuilders.Image
import androidx.wear.protolayout.LayoutElementBuilders.Layout
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.ProgressIndicatorColors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.android.gms.wearable.DataClient
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.data.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

internal const val ID_PHONE = "ic_phone"
internal const val ID_PHONE_CHARGING = "ic_phone_charging"
internal const val ID_PHONE_CHARGING_INSIDE = "ic_phone_charging_inside"
internal const val ID_PHONE_CHARGING_INSIDE_MATERIAL_SYMBOLS = "ic_phone_charging_inside_material_symbols"
internal const val ID_PHONE_CHARGING_MATERIAL_SYMBOLS = "ic_phone_charging_material_symbols"
internal const val ID_PHONE_DISCONNECTED = "ic_phone_disconnected"
internal const val ID_PHONE_DISCONNECTED_MATERIAL_SYMBOLS = "ic_phone_disconnected_material_symbols"
internal const val ID_PHONE_MATERIAL_SYMBOLS = "ic_phone_material_symbols"
internal const val RESOURCES_VERSION = "1"

@AndroidEntryPoint
class PhoneBatteryTileService : TileService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    @Inject lateinit var dataClient: DataClient

    fun formatChargeTimeRemaining(
        timeRemaining: Long,
        isConnected: Boolean,
        isCharging: Boolean,
        level: Int
    ): String {
        val seconds = timeRemaining / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            isConnected.not() -> "Disconnected"
            isCharging.not() -> "Discharging"
            isCharging && level == 100 -> "Fully charged"
            isCharging && timeRemaining <= 0 -> "Charging..."
            hours > 0 -> String.format(Locale.getDefault(),"Full in %dh %dm", hours, minutes % 60)
            else -> String.format(Locale.getDefault(),"Full in %d min", minutes)
        }
    }

    override fun onTileRequest(requestParams: TileRequest): ListenableFuture<TileBuilders.Tile> {

        val preferences = runBlocking { preferences.first() }

        val handler = Handler(mainLooper)
        handler.postDelayed({ getUpdater(this).requestUpdate(PhoneBatteryTileService::class.java) }, 1000)

        return Futures.immediateFuture(
            TileBuilders.Tile.Builder()
                .setResourcesVersion(RESOURCES_VERSION)
                .setFreshnessIntervalMillis(FRESHNESS_INTERVAL)
                .setTileTimeline(
                    TimelineBuilders.Timeline.Builder()
                        .addTimelineEntry(
                            TimelineBuilders.TimelineEntry.Builder()
                                .setLayout(getPhoneBatteryTileLayout(
                                    requestParams.deviceConfiguration,
                                    preferences.phoneBatteryLevel,
                                    preferences.nodeName,
                                    preferences.chargeRemainingTime,
                                    preferences.phoneIsConnected,
                                    preferences.phoneIsCharging,
                                    preferences.materialSymbols,
                                    preferences.chargingSymbolInsideIcon

                                )
                                ).build()
                        ).build()
                )
                .build()
        )
    }

    private fun getPhoneBatteryTileLayout(
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        batteryLevel: Int,
        nodeName: String,
        chargeRemainingTime: Long,
        phoneIsConnected: Boolean,
        phoneIsCharging: Boolean,
        materialSymbols: Boolean,
        chargingSymbolInside: Boolean
    ): Layout {

        val phoneIcon =
            when {
                phoneIsConnected && phoneIsCharging && materialSymbols && chargingSymbolInside -> ID_PHONE_CHARGING_INSIDE_MATERIAL_SYMBOLS
                phoneIsConnected && phoneIsCharging && materialSymbols -> ID_PHONE_CHARGING_MATERIAL_SYMBOLS
                phoneIsConnected && phoneIsCharging && chargingSymbolInside -> ID_PHONE_CHARGING_INSIDE
                phoneIsConnected && phoneIsCharging -> ID_PHONE_CHARGING
                phoneIsConnected && materialSymbols -> ID_PHONE_MATERIAL_SYMBOLS
                phoneIsConnected -> ID_PHONE
                materialSymbols -> ID_PHONE_DISCONNECTED_MATERIAL_SYMBOLS
                else -> ID_PHONE_DISCONNECTED
            }

        return Layout.Builder()
            .setRoot(
                EdgeContentLayout.Builder(deviceParameters)
                    .setResponsiveContentInsetEnabled(true)
                    .setEdgeContent(
                        CircularProgressIndicator.Builder()
                            .setProgress(batteryLevel.toFloat() / 100f)
                            .setStrokeWidth(DimensionBuilders.dp(10f))
                            .setCircularProgressIndicatorColors(
                                ProgressIndicatorColors(
                                    ColorBuilders.argb(TileColors.Blue),
                                    ColorBuilders.argb(TileColors.White10Pc)
                                )
                            )
                            .build()
                    )
                    // Device Name
                    .setPrimaryLabelTextContent(
                        Text.Builder(this, nodeName)
                            /*
                            .setModifiers(ModifiersBuilders.Modifiers.Builder()
                                .setPadding(ModifiersBuilders.Padding.Builder()
                                    .setBottom(DimensionBuilders.dp(16f))
                                    .build())
                                .build())

                             */
                            .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                            .setColor(ColorBuilders.argb(TileColors.Gray))
                            .build()

                    )
                    // Battery Level + Status
                    .setContent(
                        LayoutElementBuilders.Column.Builder()
                            .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
                            .addContent(
                                Text.Builder(this, "${batteryLevel}%")
                                    .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                                    .setColor(ColorBuilders.argb(TileColors.White))
                                    .setModifiers(
                                        ModifiersBuilders.Modifiers.Builder()
                                            .setClickable(launchAppClickable(openApp())).build())
                                    .build()
                            )
                            .addContent(
                                Text.Builder(this, formatChargeTimeRemaining(
                                    chargeRemainingTime,
                                    phoneIsConnected,
                                    phoneIsCharging,
                                    batteryLevel
                                ))
                                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                                    .setColor(ColorBuilders.argb(TileColors.Gray))
                                    .build()
                            )
                            .build()
                    )
                    .setSecondaryLabelTextContent(
                        Image.Builder()
                            .setWidth(DimensionBuilders.dp(36f))
                            .setHeight(DimensionBuilders.dp(36f))
                            .setColorFilter(
                                ColorFilter.Builder().setTint(ColorBuilders.argb(TileColors.Blue))
                                .build())
                            .setResourceId(phoneIcon)
                            .build()
                    )

                    .build()
            )
            .build()
    }

    override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {

        val currentResources = requestParams.resourceIds

        val resources: ResourceBuilders.Resources = ResourceBuilders.Resources.Builder()
            .apply {
                if (currentResources.isEmpty()){
                    addIdToImageMapping(
                        ID_PHONE,
                        ResourceBuilders.ImageResource.Builder().setAndroidResourceByResId(
                            ResourceBuilders.AndroidImageResourceByResId.Builder()
                                .setResourceId(R.drawable.ic_phone)
                                .build())
                            .build()
                    )
                    addIdToImageMapping(
                        ID_PHONE_CHARGING,
                        ResourceBuilders.ImageResource.Builder().setAndroidResourceByResId(
                            ResourceBuilders.AndroidImageResourceByResId.Builder()
                                .setResourceId(R.drawable.ic_phone_charging)
                                .build())
                            .build()
                    )
                    addIdToImageMapping(
                        ID_PHONE_CHARGING_INSIDE,
                        ResourceBuilders.ImageResource.Builder().setAndroidResourceByResId(
                            ResourceBuilders.AndroidImageResourceByResId.Builder()
                                .setResourceId(R.drawable.ic_phone_charging_inside)
                                .build())
                            .build()
                    )
                    addIdToImageMapping(
                        ID_PHONE_CHARGING_INSIDE_MATERIAL_SYMBOLS,
                        ResourceBuilders.ImageResource.Builder().setAndroidResourceByResId(
                            ResourceBuilders.AndroidImageResourceByResId.Builder()
                                .setResourceId(R.drawable.ic_phone_charging_inside_material_symbols)
                                .build())
                            .build()
                    )
                    addIdToImageMapping(
                        ID_PHONE_CHARGING_MATERIAL_SYMBOLS,
                        ResourceBuilders.ImageResource.Builder().setAndroidResourceByResId(
                            ResourceBuilders.AndroidImageResourceByResId.Builder()
                                .setResourceId(R.drawable.ic_phone_charging_material_symbols)
                                .build())
                            .build()
                    )
                    addIdToImageMapping(
                        ID_PHONE_DISCONNECTED,
                        ResourceBuilders.ImageResource.Builder().setAndroidResourceByResId(
                            ResourceBuilders.AndroidImageResourceByResId.Builder()
                                .setResourceId(R.drawable.ic_phone_disconnected)
                                .build())
                            .build()
                    )
                    addIdToImageMapping(
                        ID_PHONE_DISCONNECTED_MATERIAL_SYMBOLS,
                        ResourceBuilders.ImageResource.Builder().setAndroidResourceByResId(
                            ResourceBuilders.AndroidImageResourceByResId.Builder()
                                .setResourceId(R.drawable.ic_phone_disconnected_material_symbols)
                                .build())
                            .build()
                    )
                    addIdToImageMapping(
                        ID_PHONE_MATERIAL_SYMBOLS,
                        ResourceBuilders.ImageResource.Builder().setAndroidResourceByResId(
                            ResourceBuilders.AndroidImageResourceByResId.Builder()
                                .setResourceId(R.drawable.ic_phone_material_symbols)
                                .build())
                            .build()
                    )
                }
            }
            .setVersion(RESOURCES_VERSION)
            .build()
        return Futures.immediateFuture(resources)
    }

    override fun onTileAddEvent(requestParams: EventBuilders.TileAddEvent) {
        super.onTileAddEvent(requestParams)
        //runBlocking { repo2.storeTileSetState(true) }
        MobileListener.sendPhoneBatteryRequest(0, dataClient, true)
    }

    override fun onRecentInteractionEventsAsync(events: List<EventBuilders.TileInteractionEvent?>): ListenableFuture<Void?> {
        MobileListener.sendPhoneBatteryRequest(0, dataClient, true)
        return super.onRecentInteractionEventsAsync(events)
    }

    companion object {
        private const val FRESHNESS_INTERVAL = 5 * DateUtils.MINUTE_IN_MILLIS
        //private const val animationDurationInMillis = 1000L // 2 seconds
    }
    }

internal fun launchAppClickable(
    androidActivity: ActionBuilders.AndroidActivity
) = ModifiersBuilders.Clickable.Builder()
    .setOnClick(
        ActionBuilders.LaunchAction.Builder()
            .setAndroidActivity(androidActivity)
            .build()
    )
    .build()

internal fun openApp() = ActionBuilders.AndroidActivity.Builder()
    .setPackageName("com.weartools.phonebattcomp")
    .setClassName("com.weartools.phonebattcomp.MainActivity")
    .build()

object TileColors {
    val Black = Color.Black.toArgb()
    val Blue = "#b9f7ff".toColorInt()
    val Gray = "#bdc1c6".toColorInt()
    val White = Color.White.toArgb()
    val White10Pc = Color(1f, 1f, 1f, 0.15f).toArgb()
}
