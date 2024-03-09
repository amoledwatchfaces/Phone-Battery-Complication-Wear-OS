package com.weartools.phonebattcomp.tile

import android.os.Handler
import android.text.format.DateUtils
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders
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
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.data.DataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class PhoneBatteryTileService : TileService() {

    private lateinit var repo2: DataRepository
    override fun onTileRequest(requestParams: TileRequest): ListenableFuture<TileBuilders.Tile> {
        repo2 = DataRepository(this)

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
                                    runBlocking { repo2.batteryLevel.first() },
                                    runBlocking { repo2.nodeName.first() }
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
        nodeName: String
    ): Layout {

        return Layout.Builder()
            .setRoot(
                EdgeContentLayout.Builder(deviceParameters)
                    .setEdgeContent(
                        CircularProgressIndicator.Builder()
                            .setProgress(batteryLevel.toFloat() / 100f)
                            .setCircularProgressIndicatorColors(
                                ProgressIndicatorColors(
                                    ColorBuilders.argb(TileColors.Blue),
                                    ColorBuilders.argb(TileColors.White10Pc)
                                )
                            )
                            .build()
                    )

                    .setPrimaryLabelTextContent(
                        Text.Builder(this, nodeName)
                            .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                            .setColor(ColorBuilders.argb(TileColors.Blue))
                            .build()
                    )
                    .setSecondaryLabelTextContent(
                        Text.Builder(this, "Power")
                            .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                            .setColor(ColorBuilders.argb(TileColors.Gray))
                            .build()
                    )
                    .setContent(
                        Text.Builder(this, "${batteryLevel}%")
                            .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                            .setColor(ColorBuilders.argb(TileColors.White))
                            .setModifiers(
                                ModifiersBuilders.Modifiers.Builder()
                                .setClickable(launchAppClickable(openApp())).build())
                            .build()
                    )
                    .build()
            )
            .build()
    }
    /*
        override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> =
            Futures.immediateFuture(
                ResourceBuilders.Resources.Builder()
                    .setVersion(RESOURCES_VERSION)
                    .build()
            )

     */
    override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build()
        )
    }

    override fun onTileAddEvent(requestParams: EventBuilders.TileAddEvent) {
        super.onTileAddEvent(requestParams)
        //runBlocking { repo2.storeTileSetState(true) }
        MobileListener.sendPhoneBatteryRequest(0, applicationContext, true)
    }
    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        super.onTileEnterEvent(requestParams)
        MobileListener.sendPhoneBatteryRequest(0, applicationContext, true)
    }

    companion object {
        private const val RESOURCES_VERSION = "1"
        private const val FRESHNESS_INTERVAL = 15 * DateUtils.MINUTE_IN_MILLIS
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
    val Blue = android.graphics.Color.parseColor("#b9f7ff")
    val Gray = android.graphics.Color.parseColor("#BDC1C6")
    val White = Color.White.toArgb()
    val White10Pc = Color(1f, 1f, 1f, 0.1f).toArgb()
}
