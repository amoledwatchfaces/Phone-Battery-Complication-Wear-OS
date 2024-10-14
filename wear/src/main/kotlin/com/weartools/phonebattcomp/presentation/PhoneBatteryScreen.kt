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
import android.net.Uri
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
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.OutlinedCompactChip
import androidx.wear.compose.material.Text
import com.google.android.gms.wearable.DataClient
import com.weartools.phonebattcomp.BuildConfig
import com.weartools.phonebattcomp.MainViewModel
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.theme.wearColorPalette
import com.weartools.phonebattcomp.utils.openAppStoreOnPhone

@Composable
fun PhoneBatteryAppScreen(
    listState: ScalingLazyListState = rememberScalingLazyListState(),
    focusRequester: FocusRequester,
    viewModel: MainViewModel,
    dataClient: DataClient,
) {
    val context = LocalContext.current
    val preferences = viewModel.preferences.collectAsState()
    var openHowTo by remember{ mutableStateOf(false) }

    ScalingLazyColumn(
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
                color = MaterialTheme.colors.primary,
                text = stringResource(id = R.string.app_info),
                style = MaterialTheme.typography.title3
            )
        } }

        // APP INFO SECTION
        //item { PreferenceCategory(title = stringResource(id = R.string.app_info)) }
        item {
            DialogChip(
                text = if (preferences.value.phoneBatteryLevel==0) "--" else "${preferences.value.phoneBatteryLevel} %",
                icon = {
                    Icon(
                        painter =
                        if (preferences.value.phoneIsConnected.not()) painterResource(id = R.drawable.ic_phone_disconnected)
                        else if (preferences.value.phoneIsCharging) painterResource(id = R.drawable.ic_phone_charging_3) else painterResource(id = R.drawable.ic_phone_icon),
                        contentDescription = "Play Store Icon",
                        tint = wearColorPalette.secondaryVariant) },
                title = preferences.value.nodeName,
                onClick = {
                    if (preferences.value.lastUpdate == 0L){ context.openAppStoreOnPhone() }
                    MobileListener.sendPhoneBatteryRequest(0,dataClient,true)
                }
            )
        }
        item {
            DialogChip(
                text = stringResource(id = R.string.version),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Play Store Icon",
                        tint = wearColorPalette.secondaryVariant) },
                title = BuildConfig.VERSION_NAME,
                onClick = {context.openPlayStore()}
            )
        }
        item{
            Row(modifier = Modifier.padding(top = 12.dp)) {
                OutlinedButton(
                    modifier = Modifier.padding(end = 12.dp),
                    onClick = {openHowTo=openHowTo.not()}
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Outlined.ContactSupport, contentDescription = "Play Store Icon", tint = wearColorPalette.secondaryVariant)
                }
                OutlinedButton(
                    onClick = { context.openAppStoreOnPhone() }
                ) {
                    Icon(imageVector = Icons.Outlined.InstallMobile, contentDescription = "Play Store Icon", tint = wearColorPalette.secondaryVariant)
                }
            }
        }

        // PERCENTAGE COMPLICATION
        item { PreferenceCategory(title = stringResource(id = R.string.percentage_section)) }
        item {
            ToggleChip(
                label = stringResource(id = R.string.percentage_toggle),
                secondaryLabelOn = stringResource(id = R.string.percentage_on),
                secondaryLabelOff = stringResource(id = R.string.percentage_off),
                checked = preferences.value.percentage,
                onCheckedChange = {viewModel.togglePercentage(context)}
            )
        }

        // TEMPERATURE UNIT COMPLICATION
        item { PreferenceCategory(title = stringResource(id = R.string.setting_preference_category_title)) }
        item {
            ToggleChip(
                label = stringResource(id = R.string.temp_unit_pref_title),
                secondaryLabelOn = stringResource(id = R.string.temp_unit_C),
                secondaryLabelOff = stringResource(id = R.string.temp_unit_F),
                checked = preferences.value.tempUnit,
                onCheckedChange = {viewModel.toggleEnabled(context)}
            )
        }

        item { PreferenceCategory(title = stringResource(id = R.string.notifications_section)) }
        item {
            ToggleChip(
                label = stringResource(id = R.string.notif_comp_force_icon_type),
                secondaryLabelOn = stringResource(id = R.string.type_icon),
                secondaryLabelOff = stringResource(id = R.string.type_photo),
                checked = preferences.value.notificationsIconType == 0,
                onCheckedChange = {
                    viewModel.storeNotificationIconType(
                        context, if (it) 0 else 1
                    )
                }
            )
        }

        item{
            OutlinedCompactChip(
                colors = ChipDefaults.chipColors(
                    backgroundColor = Color(0xFF0E1011)
                ),
                border = ChipDefaults.outlinedChipBorder(),
                label = { Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = wearColorPalette.secondary,
                    text = stringResource(R.string.more_settings)
                )},
                modifier = Modifier.padding(top = 12.dp),
                icon = {
                    Icon(imageVector = Icons.Filled.SettingsSuggest, contentDescription = "Play Store Icon", tint = wearColorPalette.secondaryVariant)
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
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
    }
}
