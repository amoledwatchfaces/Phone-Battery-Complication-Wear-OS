package com.weartools.phonebattcomp.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InstallMobile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.SwitchButtonDefaults
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.google.android.gms.wearable.DataClient
import com.weartools.phonebattcomp.BuildConfig
import com.weartools.phonebattcomp.MainViewModel
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.utils.openAppStoreOnPhone
import com.weartools.phonebattcomp.utils.openPlayStore

@Composable
fun MainScreen(
    navController: NavHostController,
    listState: TransformingLazyColumnState = rememberTransformingLazyColumnState(),
    transformationSpec: TransformationSpec,
    focusRequester: FocusRequester,
    viewModel: MainViewModel,
    dataClient: DataClient,
) {
    val context = LocalContext.current
    val preferences by viewModel.preferences.collectAsState()
    val isLoaded by viewModel.isPreferencesLoaded.collectAsState()
    var showCrashlyticsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isLoaded) {
        if (isLoaded && !preferences.crashlyticsNoticeAccepted) {
            showCrashlyticsDialog = true
        }
    }

    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            EdgeButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                ),
                onClick = {
                    viewModel.openExperimentalSettings(context)
                }
            ) {
                Text(
                    fontWeight = FontWeight.SemiBold,
                    text = stringResource(id = R.string.more_settings),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                )
            }
        }
    ) { paddingValues ->

        TransformingLazyColumn(
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 10.dp,
                bottom = paddingValues.calculateBottomPadding() + 0.dp,
                start = 12.dp,
                end = 12.dp
            ),
            modifier = Modifier
                .fillMaxSize()
                .rotaryScrollable(
                    RotaryScrollableDefaults.behavior(scrollableState = listState),
                    focusRequester = focusRequester
                ),
            state = listState,
        )
    {
        // List Header
        item { ListHeader {
            Text(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
                text = stringResource(id = R.string.app_info),
                style = MaterialTheme.typography.titleMedium
            )
        } }

        // Main
        item {
            DialogChip(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                text = if (preferences.phoneBatteryLevel==0) "--" else "${preferences.phoneBatteryLevel} %",
                icon = {
                    Icon(painter = painterResource(
                        when {
                            preferences.phoneIsConnected && preferences.phoneIsCharging && preferences.materialSymbols && preferences.chargingSymbolInsideIcon -> R.drawable.ic_phone_charging_inside_material_symbols
                            preferences.phoneIsConnected && preferences.phoneIsCharging && preferences.materialSymbols -> R.drawable.ic_phone_charging_material_symbols
                            preferences.phoneIsConnected && preferences.phoneIsCharging && preferences.chargingSymbolInsideIcon -> R.drawable.ic_phone_charging_inside
                            preferences.phoneIsConnected && preferences.phoneIsCharging -> R.drawable.ic_phone_charging
                            preferences.phoneIsConnected && preferences.materialSymbols -> R.drawable.ic_phone_material_symbols
                            preferences.phoneIsConnected -> R.drawable.ic_phone
                            preferences.materialSymbols -> R.drawable.ic_phone_disconnected_material_symbols
                            else -> R.drawable.ic_phone_disconnected
                        }
                        ),
                        contentDescription = "Play Store Icon", tint = MaterialTheme.colorScheme.secondary) },
                title = preferences.nodeName,
                onClick = {
                    if (preferences.lastUpdate == 0L){ context.openAppStoreOnPhone() }
                    MobileListener.sendPhoneBatteryRequest(0,dataClient,true)
                }
            )
        }
        item {
            DialogChip(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                text = stringResource(id = R.string.version),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Play Store Icon", tint = MaterialTheme.colorScheme.secondary) },
                title = BuildConfig.VERSION_NAME,
                onClick = {context.openPlayStore()}
            )
        }
        item {
            Row(
                modifier = Modifier.padding(top = 12.dp).transformedHeight(this, transformationSpec)
            ) {
                OutlinedButton(
                    border = ButtonDefaults.outlinedButtonBorder(true, MaterialTheme.colorScheme.primaryContainer, borderWidth = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(end = 6.dp),
                    transformation = SurfaceTransformation(transformationSpec),
                    onClick = { navController.navigate("guide_screen")}
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ContactSupport,
                        contentDescription = "Play Store Icon", tint = MaterialTheme.colorScheme.secondary)
                }
                OutlinedButton(
                    border = ButtonDefaults.outlinedButtonBorder(true, MaterialTheme.colorScheme.primaryContainer, borderWidth = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(start = 6.dp),
                    transformation = SurfaceTransformation(transformationSpec),
                    onClick = { context.openAppStoreOnPhone() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.InstallMobile,
                        contentDescription = "Play Store Icon", tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // Phone / Watch Battery Complications
        item {
            ListSubHeader(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                label = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.tertiary,
                        text = stringResource(id = R.string.percentage_section),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.percentage,
                onCheckedChange = {viewModel.togglePercentage(context)},
                label = { Text(stringResource(id = R.string.percentage_toggle)) },
                secondaryLabel = {
                    if (preferences.percentage) {
                        Text(text = stringResource(id = R.string.percentage_on))
                    } else
                        Text(text = stringResource(id = R.string.percentage_off))
                }
            )
        }
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.materialSymbols,
                colors = SwitchButtonDefaults.switchButtonColors(),
                onCheckedChange = { enabled -> viewModel.toggleMaterialSymbols(enabled,context) },
                label = { Text(stringResource(R.string.pbc_material_symbols)) },
                icon = {
                    Icon(
                        painter = if (preferences.materialSymbols){ painterResource(R.drawable.ic_phone_material_symbols) }
                        else {painterResource(R.drawable.ic_phone)},
                        contentDescription = "Phone Icon", tint = MaterialTheme.colorScheme.secondary) },
            )
        }
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.chargingSymbolInsideIcon,
                colors = SwitchButtonDefaults.switchButtonColors(),
                onCheckedChange = { enabled -> viewModel.toggleChargingSymbol(enabled,context) },
                label = { Text(stringResource(R.string.charging_symbol_inside_icon)) },
                icon = {
                    Icon(
                        painter = painterResource(
                            when {
                                preferences.materialSymbols && preferences.chargingSymbolInsideIcon -> R.drawable.ic_phone_charging_inside_material_symbols
                                preferences.materialSymbols -> R.drawable.ic_phone_charging_material_symbols
                                preferences.chargingSymbolInsideIcon-> R.drawable.ic_phone_charging_inside
                                else -> R.drawable.ic_phone_charging
                            }
                        ),
                        contentDescription = "Phone Icon", tint = MaterialTheme.colorScheme.secondary) },

            )
        }

        // Watch Battery Temperature Complication
        item {
            ListSubHeader(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                label = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.tertiary,
                        text = stringResource(id = R.string.setting_preference_category_title),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.tempUnit,
                onCheckedChange = {viewModel.toggleEnabled(context)},
                label = { Text(stringResource(id = R.string.temp_unit_pref_title)) },
                secondaryLabel = {
                    if (preferences.tempUnit) {
                        Text(text = stringResource(id = R.string.temp_unit_C))
                    } else
                        Text(text = stringResource(id = R.string.temp_unit_F))
                }
            )
        }

        // General
        item {
            ListSubHeader(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                label = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.tertiary,
                        text = stringResource(id = R.string.settings),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.useDynamicColor,
                onCheckedChange = { viewModel.setUseDynamicColor(it) },
                label = { Text(stringResource(id = R.string.dynamic_color)) },
            )
        }
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.crashlytics,
                onCheckedChange = { viewModel.setCrashlytics(it) },
                label = { Text(stringResource(id = R.string.crash_reports)) },
            )
        }

        // Phone Notifications Complication
        item {
            ListSubHeader(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                label = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.tertiary,
                        text = stringResource(id = R.string.notifications_section),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
        item {

            SwitchButton(
                colors = SwitchButtonDefaults.switchButtonColors(),
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.notificationsIconType == 2,
                onCheckedChange = {
                    viewModel.storeNotificationIconType(
                        context, if (it) 2 else 1
                    )
                },
                label = { Text(stringResource(id = R.string.notif_comp_force_icon_type)) },
                secondaryLabel = {
                    if (preferences.notificationsIconType == 2) {
                        Text(text = stringResource(id = R.string.type_icon))
                    } else
                        Text(text = stringResource(id = R.string.type_photo))
                }
            )
        }
        item {
            ListSubHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                label = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        text = "amoledwatchfaces.com",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        }
    }

    if (showCrashlyticsDialog) {
        ConfirmationDialog(
            showDialog = true,
            title = stringResource(id = R.string.crashlytics_title),
            message = stringResource(id = R.string.crashlytics_message),
            onConfirm = {
                viewModel.setCrashlytics(true)
                showCrashlyticsDialog = false
            },
            onCancel = {
                viewModel.setCrashlytics(false)
                showCrashlyticsDialog = false
            }
        )
    }
    }
}
