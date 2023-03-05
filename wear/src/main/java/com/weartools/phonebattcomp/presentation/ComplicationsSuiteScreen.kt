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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.weartools.phonebattcomp.BuildConfig
import com.weartools.phonebattcomp.MainActivity
import com.weartools.phonebattcomp.Pref
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.theme.PhoneBatteryAppTheme

@Composable
fun PhoneBatteryAppScreen(
    listState: ScalingLazyListState = rememberScalingLazyListState()
) {
    val context = LocalContext.current
    val pref = Pref(context)
    var militaryTime by remember { mutableStateOf(pref.getTempUnit()) }
    var openHowTo by remember{ mutableStateOf(false) }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        autoCentering = AutoCenteringParams(itemIndex = 1),
        state = listState,
    ) {
        //SETTINGS TEST
        item { SettingsText() }

        // APP INFO SECTION
        //item { PreferenceCategory(title = stringResource(id = R.string.app_info)) }
        item {
            SimpleChip(
                text = stringResource(id = R.string.faq),
                iconId = R.drawable.ic_help,
                onClick = { openHowTo=openHowTo.not() }
            )
        }
        item {
            DialogChip(
                text = stringResource(id = R.string.version),
                title = BuildConfig.VERSION_NAME,
            )
        }
        item {
            SimpleChip(
                text = stringResource(id = R.string.install_chip),
                iconId = R.drawable.ic_playstore,
                onClick = { openAppStoreOnPhone(context) }
            )
        }

        // TEMPERATURE UNIT COMPLICATION
        item { PreferenceCategory(title = stringResource(id = R.string.setting_preference_category_title)) }
        item {
            ToggleChip(
                label = stringResource(id = R.string.temp_unit_pref_title),
                secondaryLabelOn = stringResource(id = R.string.temp_unit_C),
                secondaryLabelOff = stringResource(id = R.string.temp_unit_F),
                checked = militaryTime,
                onCheckedChange = {
                    militaryTime=it
                    pref.setTempUnit(it)
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
        ListItemsWidget(titles = stringResource(id = R.string.faq), callback ={
            if (it!=-1) {/**/}else openHowTo=false } )
    }

}

fun openAppStoreOnPhone(context: Context) {
    val remoteActivityHelper = RemoteActivityHelper(context)
    val intentAndroid = Intent(Intent.ACTION_VIEW)
        .addCategory(Intent.CATEGORY_BROWSABLE)
        .setData(Uri.parse(MainActivity.PLAY_STORE_APP_URI))
    remoteActivityHelper.startRemoteActivity(intentAndroid,targetNodeId = null)
    Toast.makeText(context, context.getString(R.string.check_phone), Toast.LENGTH_LONG).show()
}

@Composable
fun ComplicationsSuiteScreenPreview() {
    PhoneBatteryAppTheme {
        PhoneBatteryAppScreen()
    }
}
