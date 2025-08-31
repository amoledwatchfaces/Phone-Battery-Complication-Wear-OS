/*
 * Copyright 2022 The Android Open Source Project
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
package com.weartools.phonebattcomp.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InstallMobile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
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

@Composable
fun PhoneBatteryAppScreen(
    listState: TransformingLazyColumnState = rememberTransformingLazyColumnState(),
    transformationSpec: TransformationSpec,
    focusRequester: FocusRequester,
    viewModel: MainViewModel,
    dataClient: DataClient,
) {
    val context = LocalContext.current
    val preferences = viewModel.preferences.collectAsState()
    var openHowTo by remember{ mutableStateOf(false) }

    TransformingLazyColumn(
        contentPadding = PaddingValues(top = 25.dp, bottom = 65.dp, start = 12.dp, end = 12.dp),
        modifier = Modifier
            .fillMaxSize()
            .rotaryScrollable(
                RotaryScrollableDefaults.behavior(scrollableState = listState),
                focusRequester = focusRequester
            ),
        state = listState,
    )
    {
        item { ListHeader {
            Text(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                text = stringResource(id = R.string.app_info),
                style = MaterialTheme.typography.titleSmall
            )
        } }

        // Main
        item {
            DialogChip(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                text = if (preferences.value.phoneBatteryLevel==0) "--" else "${preferences.value.phoneBatteryLevel} %",
                icon = {
                    Icon(painter = painterResource(
                        when {
                            preferences.value.phoneIsConnected && preferences.value.phoneIsCharging && preferences.value.materialSymbols && preferences.value.chargingSymbolInsideIcon -> R.drawable.ic_phone_charging_inside_material_symbols
                            preferences.value.phoneIsConnected && preferences.value.phoneIsCharging && preferences.value.materialSymbols -> R.drawable.ic_phone_charging_material_symbols
                            preferences.value.phoneIsConnected && preferences.value.phoneIsCharging && preferences.value.chargingSymbolInsideIcon -> R.drawable.ic_phone_charging_inside
                            preferences.value.phoneIsConnected && preferences.value.phoneIsCharging -> R.drawable.ic_phone_charging
                            preferences.value.phoneIsConnected && preferences.value.materialSymbols -> R.drawable.ic_phone_material_symbols
                            preferences.value.phoneIsConnected -> R.drawable.ic_phone
                            preferences.value.materialSymbols -> R.drawable.ic_phone_disconnected_material_symbols
                            else -> R.drawable.ic_phone_disconnected
                        }
                        ),
                        contentDescription = "Play Store Icon") },
                title = preferences.value.nodeName,
                onClick = {
                    if (preferences.value.lastUpdate == 0L){ context.openAppStoreOnPhone() }
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
                        contentDescription = "Play Store Icon") },
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
                    onClick = {openHowTo=openHowTo.not()}
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ContactSupport,
                        contentDescription = "Play Store Icon")
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
                        contentDescription = "Play Store Icon")
                }
            }
        }

        // Phone / Watch Battery Complications
        item { PreferenceCategory(title = stringResource(id = R.string.percentage_section)) }
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.value.percentage,
                onCheckedChange = {viewModel.togglePercentage(context)},
                label = { Text(stringResource(id = R.string.percentage_toggle)) },
                secondaryLabel = {
                    if (preferences.value.percentage) {
                        Text(text = stringResource(id = R.string.percentage_on), color = Color.LightGray)
                    } else Text(text = stringResource(id = R.string.percentage_off), color = Color.LightGray)
                }
            )
        }
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.value.materialSymbols,
                colors = SwitchButtonDefaults.switchButtonColors(),
                onCheckedChange = { enabled -> viewModel.toggleMaterialSymbols(enabled,context) },
                label = { Text(stringResource(R.string.pbc_material_symbols)) },
                icon = {
                    Icon(
                        painter = if (preferences.value.materialSymbols){ painterResource(R.drawable.ic_phone_material_symbols) }
                        else {painterResource(R.drawable.ic_phone)},
                        contentDescription = "Phone Icon", tint = MaterialTheme.colorScheme.primary) },
            )
        }
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.value.chargingSymbolInsideIcon,
                colors = SwitchButtonDefaults.switchButtonColors(),
                onCheckedChange = { enabled -> viewModel.toggleChargingSymbol(enabled,context) },
                label = { Text(stringResource(R.string.charging_symbol_inside_icon)) },
                icon = {
                    Icon(
                        painter = painterResource(
                            when {
                                preferences.value.materialSymbols && preferences.value.chargingSymbolInsideIcon -> R.drawable.ic_phone_charging_inside_material_symbols
                                preferences.value.materialSymbols -> R.drawable.ic_phone_charging_material_symbols
                                preferences.value.chargingSymbolInsideIcon-> R.drawable.ic_phone_charging_inside
                                else -> R.drawable.ic_phone_charging
                            }
                        ),
                        contentDescription = "Phone Icon", tint = MaterialTheme.colorScheme.primary) },

            )
        }

        // Watch Battery Temperature Complication
        item { PreferenceCategory(title = stringResource(id = R.string.setting_preference_category_title)) }
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.value.tempUnit,
                onCheckedChange = {viewModel.toggleEnabled(context)},
                label = { Text(stringResource(id = R.string.temp_unit_pref_title)) },
                secondaryLabel = {
                    if (preferences.value.percentage) {
                        Text(text = stringResource(id = R.string.temp_unit_C), color = Color.LightGray)
                    } else Text(text = stringResource(id = R.string.temp_unit_F), color = Color.LightGray)
                }
            )
        }

        // Phone Notifications Complication
        item { PreferenceCategory(title = stringResource(id = R.string.notifications_section)) }
        item {

            SwitchButton(
                colors = SwitchButtonDefaults.switchButtonColors(),
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.value.notificationsIconType == 2,
                onCheckedChange = {
                    viewModel.storeNotificationIconType(
                        context, if (it) 2 else 1
                    )
                },
                label = { Text(stringResource(id = R.string.notif_comp_force_icon_type)) },
                secondaryLabel = {
                    if (preferences.value.percentage) {
                        Text(text = stringResource(id = R.string.type_icon), color = Color.LightGray)
                    } else Text(text = stringResource(id = R.string.type_photo), color = Color.LightGray)
                }
            )
        }
        item {
            CompactButton(
                label = { Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text = stringResource(R.string.more_settings)
                )},
                modifier = Modifier.padding(top = 12.dp).transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.SettingsSuggest,
                        contentDescription = "Play Store Icon"
                    )
                },
                onClick = {
                    viewModel.openExperimentalSettings(context)
                }
            )
        }
        item {
            SectionText(
                text = "amoledwatchfaces.com",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 20.dp, end = 20.dp),
            )
        }

    }

    if (openHowTo){
        ListItemsWidget(titles = stringResource(id = R.string.faq), callback = {
            if (it == -1) {
                openHowTo = false
                return@ListItemsWidget
            } else {
                openHowTo = openHowTo.not()
            }
        })
    }
}
fun Context.openPlayStore() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
    } catch (_: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri()))
    }
}
