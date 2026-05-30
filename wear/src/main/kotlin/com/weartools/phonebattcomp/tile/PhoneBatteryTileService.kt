package com.weartools.phonebattcomp.tile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ProtoLayoutScope
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.expression.DynamicBuilders
import androidx.wear.protolayout.expression.PlatformEventSources
import androidx.wear.protolayout.layout.androidImageResource
import androidx.wear.protolayout.layout.imageResource
import androidx.wear.protolayout.material3.ButtonDefaults.filledVariantButtonColors
import androidx.wear.protolayout.material3.CardDefaults.filledTonalCardColors
import androidx.wear.protolayout.material3.CircularProgressIndicatorDefaults
import androidx.wear.protolayout.material3.GraphicDataCardDefaults.constructGraphic
import androidx.wear.protolayout.material3.PrimaryLayoutMargins
import androidx.wear.protolayout.material3.circularProgressIndicator
import androidx.wear.protolayout.material3.graphicDataCard
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.materialScopeWithResources
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.tile
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
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@AndroidEntryPoint
class PhoneBatteryTileService : TileService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    @Inject
    lateinit var dataClient: DataClient

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
            hours > 0 -> String.format(Locale.getDefault(), "Full in %dh %dm", hours, minutes % 60)
            else -> String.format(Locale.getDefault(), "Full in %d min", minutes)
        }
    }


    private fun layout(
        context: Context,
        scope: ProtoLayoutScope,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        batteryLevel: Int,
        nodeName: String,
        chargeRemainingTime: Long,
        phoneIsConnected: Boolean,
        phoneIsCharging: Boolean,
        materialSymbols: Boolean,
        chargingSymbolInside: Boolean
    ) = materialScopeWithResources(context, scope, deviceParameters) {
        primaryLayout(
            titleSlot = { text(nodeName.layoutString) },
            margins = PrimaryLayoutMargins.MIN_PRIMARY_LAYOUT_MARGIN,
            mainSlot = {
                graphicDataCard(
                    onClick = clickable(),
                    height = expand(),
                    colors = filledTonalCardColors(),
                    title = {
                        text(
                            "${batteryLevel}%".layoutString
                        )
                    },
                    content = {
                        text(
                            scalable = true,
                            maxLines = 2,
                            text = formatChargeTimeRemaining(
                                chargeRemainingTime,
                                phoneIsConnected,
                                phoneIsCharging,
                                batteryLevel
                            ).layoutString
                        )
                    },
                    horizontalAlignment = LayoutElementBuilders.HORIZONTAL_ALIGN_END,
                    graphic = {
                        constructGraphic(
                            mainContent = {
                                circularProgressIndicator(
                                    staticProgress = 1F * batteryLevel / 100,
                                    // On supported devices, animate the arc
                                    dynamicProgress =
                                        DynamicBuilders.DynamicFloat
                                            .onCondition(
                                                PlatformEventSources.isLayoutVisible()
                                            ).use(1F * batteryLevel / 100)
                                            .elseUse(0F)
                                            .animate(
                                                CircularProgressIndicatorDefaults
                                                    .recommendedAnimationSpec
                                            ),
                                    startAngleDegrees = 200F,
                                    endAngleDegrees = 520F
                                )
                            },
                            iconContent = {
                                icon(
                                    imageResource(
                                        androidImageResource(
                                            when {


                                                phoneIsConnected && phoneIsCharging && materialSymbols && chargingSymbolInside -> R.drawable.ic_phone_charging_inside_material_symbols
                                                phoneIsConnected && phoneIsCharging && materialSymbols -> R.drawable.ic_phone_charging_material_symbols
                                                phoneIsConnected && phoneIsCharging && chargingSymbolInside -> R.drawable.ic_phone_charging_inside
                                                phoneIsConnected && phoneIsCharging -> R.drawable.ic_phone_charging
                                                phoneIsConnected && materialSymbols -> R.drawable.ic_phone_material_symbols
                                                phoneIsConnected -> R.drawable.ic_phone
                                                materialSymbols -> R.drawable.ic_phone_disconnected_material_symbols
                                                else -> R.drawable.ic_phone_disconnected
                                            }
                                        )
                                    )
                                )
                            }
                        )
                    }
                )
            },
            bottomSlot = {
                textEdgeButton(
                    colors = filledVariantButtonColors(),
                    onClick = launchAppClickable(openApp()))
                { text("Open App".layoutString) }
            }
        )
    }

    override fun onTileRequest(requestParams: TileRequest): ListenableFuture<TileBuilders.Tile> {
        val preferences = runBlocking { preferences.first() }

        return Futures.immediateFuture(
            tile(
                freshness = 5.toDuration(DurationUnit.MINUTES),
                resourcesVersion = "1",
                timeline = Timeline.fromLayoutElement(
                    layout(
                        context = applicationContext,
                        scope = requestParams.scope,
                        deviceParameters = requestParams.deviceConfiguration,
                        batteryLevel = preferences.phoneBatteryLevel,
                        nodeName = preferences.nodeName,
                        chargeRemainingTime = preferences.chargeRemainingTime,
                        phoneIsConnected = preferences.phoneIsConnected,
                        phoneIsCharging = preferences.phoneIsCharging,
                        materialSymbols = preferences.materialSymbols,
                        chargingSymbolInside = preferences.chargingSymbolInsideIcon
                    )
                )
            )
        )
    }

    override fun onTileAddEvent(requestParams: EventBuilders.TileAddEvent) {
        super.onTileAddEvent(requestParams)
        MobileListener.sendPhoneBatteryRequest(0, dataClient, true)
    }

    override fun onRecentInteractionEventsAsync(events: List<EventBuilders.TileInteractionEvent?>): ListenableFuture<Void?> {
        MobileListener.sendPhoneBatteryRequest(0, dataClient, true)
        return super.onRecentInteractionEventsAsync(events)
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
}
