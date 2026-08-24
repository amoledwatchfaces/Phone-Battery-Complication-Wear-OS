package com.weartools.phonebattcomp.widget

import android.content.Context
import androidx.compose.remote.creation.compose.action.valueChange
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.state.animateRemoteFloat
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteFloat
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.state.rsp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
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
import androidx.wear.compose.remote.material3.RemoteIconButtonDefaults
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteProgressIndicatorDefaults
import androidx.wear.compose.remote.material3.RemoteText
import com.weartools.phonebattcomp.R
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
                dynamicColors = preferences.useDynamicColor,
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
    dynamicColors: Boolean,
    appColorScheme: RemoteColorScheme,
    batteryLevel: Int,
    nodeName: String,
    chargeRemainingTime: String,
    phoneIsConnected: Boolean,
    phoneIsCharging: Boolean,
    materialSymbols: Boolean,
    chargingSymbolInside: Boolean
) {
    RemoteMaterialTheme(
        colorScheme = if (dynamicColors) RemoteMaterialTheme.colorScheme else appColorScheme,
    ) {
        RemoteColumn(
            modifier = RemoteModifier.fillMaxWidth(),
            verticalArrangement = RemoteArrangement.Center,
            horizontalAlignment = RemoteAlignment.CenterHorizontally,
        ) {
            RemoteRow(
                modifier = RemoteModifier.fillMaxWidth().weight(2.rf),
                horizontalArrangement = RemoteArrangement.Start,
            ) {
                RemoteText(
                    fontWeight = FontWeight.Medium,
                    text = nodeName.rs,
                    color = RemoteMaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RemoteRow(
                modifier = RemoteModifier.fillMaxWidth().weight(4.rf),
                horizontalArrangement = RemoteArrangement.SpaceBetween,
                verticalAlignment = RemoteAlignment.CenterVertically
            ) {
                RemoteColumn(
                    modifier = RemoteModifier.weight(3.rf),
                ) {
                    RemoteText(
                        fontSize = 24.rsp,
                        fontWeight = FontWeight.Medium,
                        text = "${batteryLevel}%".rs,
                        color = RemoteMaterialTheme.colorScheme.onSecondaryContainer
                    )
                    RemoteText(
                        fontWeight = FontWeight.Medium,
                        text = chargeRemainingTime.rs,
                        color = appColorScheme.secondary
                    )
                }
                RemoteColumn(
                    modifier = RemoteModifier.weight(2.rf),
                ) {
                    val progressStart = rememberMutableRemoteFloat { 0.rf }
                    val progress = rememberMutableRemoteFloat { (1F * batteryLevel / 100).rf }
                    val animatedProgress = animateRemoteFloat(progress, 0.25f)

                    RemoteBox(
                        contentAlignment = RemoteAlignment.Center
                    ){
                        LaunchedEffect(Unit) {
                            valueChange(progressStart, (progress))
                        }

                        RemoteCircularProgressIndicator(
                            strokeWidth = 6.rdp,
                            gapSize = RemoteProgressIndicatorDefaults.IndeterminateStrokeWidth,
                            progress = animatedProgress,
                            startAngle = 110.rf,
                            endAngle = 430.rf,
                            enabled = phoneIsConnected.rb
                        )

                        RemoteIcon(
                            modifier = RemoteModifier.size(RemoteIconButtonDefaults.SmallIconSize),
                            imageVector = ImageVector.vectorResource(
                                when {
                                    phoneIsConnected && phoneIsCharging -> R.drawable.mobile_charge_24px
                                    phoneIsConnected -> R.drawable.mobile_24px
                                    else -> R.drawable.mobile_cancel_24px
                                }
                            ),
                            contentDescription = "Battery Status".rs,
                            tint = RemoteMaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
