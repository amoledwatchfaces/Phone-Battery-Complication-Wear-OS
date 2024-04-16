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
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InstallMobile
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.OutlinedCompactChip
import androidx.wear.compose.material.Text
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.weartools.phonebattcomp.BuildConfig
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.PassiveDataViewModel
import com.weartools.phonebattcomp.presentation.rotary.rotaryWithScroll
import com.weartools.phonebattcomp.theme.wearColorPalette

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun PhoneBatteryAppScreen(
    listState: ScalingLazyListState = rememberScalingLazyListState(),
    focusRequester: FocusRequester,
    nodeName: String,
    batteryLevel: Int,
    tempUnit: Boolean,
    percentage: Boolean
) {
    val viewModel: PassiveDataViewModel = viewModel()
    val context = LocalContext.current
    var openHowTo by remember{ mutableStateOf(false) }
    var openExperimental by remember{ mutableStateOf(false) }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .rotaryWithScroll(
                scrollableState = listState,
                focusRequester = focusRequester
            ),
        autoCentering = AutoCenteringParams(itemIndex = 1),
        state = listState,
    )
    {
        //SETTINGS TEST
        item { SettingsText() }

        // APP INFO SECTION
        //item { PreferenceCategory(title = stringResource(id = R.string.app_info)) }
        item {
            DialogChip(
                text = if (batteryLevel==0) "--" else "$batteryLevel %",
                icon = { Icon(imageVector = Icons.Outlined.Smartphone, contentDescription = "Play Store Icon", tint = wearColorPalette.secondaryVariant) },
                title = nodeName,
                onClick = {
                    MobileListener.sendPhoneBatteryRequest(0,context,true)
                }
            )
        }
        item {
            DialogChip(
                text = stringResource(id = R.string.version),
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = "Play Store Icon", tint = wearColorPalette.secondaryVariant) },
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
                    onClick = { openAppStoreOnPhone(context) }
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
                checked = percentage,
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
                checked = tempUnit,
                onCheckedChange = {viewModel.toggleEnabled(context)}
            )
        }

        item{
            OutlinedCompactChip(
                colors = ChipDefaults.chipColors(
                    backgroundColor = Color(0xFF0E1011)
                ),
                border = ChipDefaults.outlinedChipBorder(),
                label = { Text(color = wearColorPalette.secondary, text = "Experimental")},
                modifier = Modifier.padding(top = 12.dp),
                icon = {
                    Icon(imageVector = Icons.Filled.Science, contentDescription = "Play Store Icon", tint = wearColorPalette.secondaryVariant)
                       },
                onClick = { openExperimental=openExperimental.not() }
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
    if (openExperimental){
        ExperimentalWidget(viewModel= viewModel,context = context, callback = {
            if (it == -1) {
                openExperimental = false
                return@ExperimentalWidget
            } else {
                openExperimental = openExperimental.not()
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
fun openAppStoreOnPhone(context: Context) {
    val remoteActivityHelper = RemoteActivityHelper(context)
    val intentAndroid = Intent(Intent.ACTION_VIEW)
        .addCategory(Intent.CATEGORY_BROWSABLE)
        .setData(Uri.parse(BuildConfig.PLAY_STORE_APP_URI))
    remoteActivityHelper.startRemoteActivity(intentAndroid,targetNodeId = null)
    Toast.makeText(context, context.getString(R.string.check_phone), Toast.LENGTH_LONG).show()
}

