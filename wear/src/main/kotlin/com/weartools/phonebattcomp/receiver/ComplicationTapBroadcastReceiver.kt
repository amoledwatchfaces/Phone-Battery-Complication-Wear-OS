/*
 * Copyright 2022-2026 amoledwatchfaces™
 * support@amoledwatchfaces.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
