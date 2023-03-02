/*
 * Copyright 2022 amoledwatchfaces™
 * support@amoledwatchfaces.com
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
package com.weartools.phonebattcomp.complication

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.wear.remote.interactions.RemoteActivityHelper
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.weartools.phonebattcomp.MainActivity
import com.weartools.phonebattcomp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ComplicationTapBroadcastReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent) {
        val args = intent.getArgs()
        val result = goAsync()
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val hasMobileApp = preferences.getBoolean(context.getString(R.string.key_pref_has_mobile_app), false)

        scope.launch {
            try {
                if (args.providerComponent.toString() == "ComponentInfo{com.weartools.phonebattcomp/com.weartools.phonebattcomp.complication.MobileBatteryComplicationService}" && (!hasMobileApp)){
                    openAppStoreOnPhone(context = context)
                    Log.d(TAG, "Opening Play Store Listing!")
                    Toast.makeText(context, context.getString(R.string.install_companion), Toast.LENGTH_LONG).show()
                    ComplicationDataSourceUpdateRequester
                        .create(context = context, complicationDataSourceComponent = args.providerComponent)
                        .requestUpdate(args.complicationInstanceId)
                }
                else {
                ComplicationDataSourceUpdateRequester
                    .create(context = context, complicationDataSourceComponent = args.providerComponent)
                    .requestUpdate(args.complicationInstanceId)
                }
            } finally {
                result.finish()
            }
        }
    }
    fun openAppStoreOnPhone(context: Context) {
        val remoteActivityHelper = RemoteActivityHelper(context)
        val intentAndroid = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse(MainActivity.PLAY_STORE_APP_URI))
        remoteActivityHelper.startRemoteActivity(intentAndroid, targetNodeId = null)
    }

    companion object {
        private val TAG = ComplicationTapBroadcastReceiver::class.java.simpleName
        private const val EXTRA_ARGS = "arguments"
        fun getToggleIntent(
            context: Context,
            args: ComplicationToggleArgs
        ): PendingIntent {
            val intent = Intent(context, ComplicationTapBroadcastReceiver::class.java).apply {
                putExtra(EXTRA_ARGS, args)
            }

            return PendingIntent.getBroadcast(
                context,
                args.complicationInstanceId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        private fun Intent.getArgs(): ComplicationToggleArgs = requireNotNull(
            extras?.getParcelable(EXTRA_ARGS)
        )
    }
}
