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
package com.weartools.phonebattcomp.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.google.android.gms.wearable.DataClient
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.complication.MobileBatteryComplicationService
import com.weartools.phonebattcomp.complication.WatchBatteryComplicationService
import com.weartools.phonebattcomp.utils.updateComplication
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
class ComplicationTapBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var dataClient: DataClient
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent) {
        val args = intent.getArgs()
        val result = goAsync()

        scope.launch {
            try {
                when (args.providerComponent) {
                    ComponentName(context, MobileBatteryComplicationService::class.java) -> {
                        MobileListener.sendPhoneBatteryRequest(0, dataClient, true)
                    }
                    ComponentName(context, WatchBatteryComplicationService::class.java) -> {
                        context.updateComplication(WatchBatteryComplicationService::class.java)
                    }
                    else -> {
                        ComplicationDataSourceUpdateRequester
                            .create(context = context, complicationDataSourceComponent = args.providerComponent)
                            .requestUpdate(args.complicationInstanceId)
                    }
                }
            } finally {
                result.finish()
            }
        }
    }
    companion object {
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
            @Suppress("DEPRECATION")
            extras?.getParcelable(EXTRA_ARGS)
        )
    }
}
@Parcelize
data class ComplicationToggleArgs(
    val providerComponent: ComponentName,
    val complicationInstanceId: Int
) : Parcelable
