package com.weartools.phonebattcomp.widget

import android.content.Context
import androidx.compose.remote.creation.compose.capture.RemoteImageVector
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.state.animateRemoteFloat
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteFloat
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.glance.wear.AssociateWithGlanceWearWidget
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.GlanceWearWidgetService
import androidx.glance.wear.WearWidgetBrush
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.WearWidgetDocument
import androidx.glance.wear.color
import androidx.glance.wear.core.WearWidgetParams
import androidx.wear.compose.remote.material3.RemoteCircularProgressIndicator
import androidx.wear.compose.remote.material3.RemoteColorScheme
import androidx.wear.compose.remote.material3.RemoteIcon
import androidx.wear.compose.remote.material3.RemoteProgressIndicatorDefaults
import androidx.wear.compose.remote.material3.RemoteText
import com.weartools.phonebattcomp.data.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
@AssociateWithGlanceWearWidget(PhoneBatteryWidget::class)
class PhoneBatteryWidgetService : GlanceWearWidgetService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>

    override val widget: GlanceWearWidget by lazy {
        PhoneBatteryWidget(dataStore)
    }
}

class PhoneBatteryWidget(
    private val dataStore: DataStore<UserPreferences>
) : GlanceWearWidget() {

    override suspend fun provideWidgetData(
        context: Context,
        params: WearWidgetParams,
    ): WearWidgetData {

        // Read the snapshot of user preferences
        val preferences: UserPreferences = dataStore.data.first()

        val appColorScheme = WIDGET_COLOR_SCHEME

        return WearWidgetDocument(background = WearWidgetBrush.color(appColorScheme.surfaceContainerHigh)) {

            PhoneBatteryWidgetContent(
                appColorScheme =  appColorScheme,
                batteryLevel = 75,
                nodeName = "Pixel 9 Pro XL",
                chargeRemainingTime = formatChargeTimeRemaining(
                    300000,
                    true,
                    true,
                    75
                ),
                phoneIsConnected = true,
                phoneIsCharging = true,
                materialSymbols = true,
                chargingSymbolInside = false
            )
            // Original
            /*
            PhoneBatteryWidgetContent(
                appColorScheme =  appColorScheme,
                batteryLevel = preferences.phoneBatteryLevel,
                nodeName = preferences.nodeName,
                chargeRemainingTime = formatChargeTimeRemaining(
                    preferences.chargeRemainingTime,
                    preferences.phoneIsConnected,
                    preferences.phoneIsCharging,
                    preferences.phoneBatteryLevel
                ),
                phoneIsConnected = preferences.phoneIsConnected,
                phoneIsCharging = preferences.phoneIsCharging,
                materialSymbols = preferences.materialSymbols,
                chargingSymbolInside = preferences.chargingSymbolInsideIcon
            )

             */
        }
    }

    private fun formatChargeTimeRemaining(
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
            isCharging && (level == 100) -> "Fully charged"
            isCharging && (timeRemaining <= 0) -> "Charging..."
            hours > 0 -> String.format(Locale.getDefault(), "Full in %dh %dm", hours, (minutes % 60).toInt())
            else -> String.format(Locale.getDefault(), "Full in %d min", minutes.toInt())
        }
    }
}

@RemoteComposable
@Composable
fun PhoneBatteryWidgetContent(
    appColorScheme: RemoteColorScheme,
    batteryLevel: Int,
    nodeName: String,
    chargeRemainingTime: String,
    phoneIsConnected: Boolean,
    phoneIsCharging: Boolean,
    materialSymbols: Boolean,
    chargingSymbolInside: Boolean
) {
    RemoteColumn(
        modifier = RemoteModifier.fillMaxWidth(),
        verticalArrangement = RemoteArrangement.Top,
        horizontalAlignment = RemoteAlignment.Start,
    ) {
        RemoteRow() {
            RemoteText(
                text = nodeName.rs,
                color = appColorScheme.onSurfaceVariant
            )
        }
        RemoteRow() {
            RemoteColumn() {
                RemoteText(
                    text = "${batteryLevel}%".rs,
                    color = appColorScheme.onSecondaryContainer
                )
                RemoteText(
                    text = chargeRemainingTime.rs,
                    color = appColorScheme.secondary
                )
            }
            RemoteColumn() {
                RemoteBox(
                    contentAlignment = RemoteAlignment.Center
                ){
                    val progress = rememberMutableRemoteFloat { (1F * batteryLevel / 100).rf }
                    val animatedProgress = animateRemoteFloat(progress, 0.25f)

                    RemoteCircularProgressIndicator(
                        gapSize = 4.rdp,
                        progress = animatedProgress,
                        startAngle = 110.rf,
                        endAngle = 430.rf,
                        enabled = phoneIsConnected.rb
                    )

                    RemoteIcon(
                        imageVector = RemoteImageVector.fromResource()
                    )

                }
            }

        }
    }
}
