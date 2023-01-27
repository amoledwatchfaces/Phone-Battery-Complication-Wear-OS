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
package com.weartools.phonebattcomp

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import java.util.concurrent.ExecutionException

class SendMessageService {
    class SendMsgToPhone : Thread() {
        inner class SendThread
            (var context: Context, var path: String) : Thread() {
            override fun run() {
                val nodeListTask = Wearable.getNodeClient(context).connectedNodes
                try {
                    val nodes = Tasks.await(nodeListTask)
                    for (node in nodes) {
                        val sendMessageTask = Wearable.getMessageClient(context)
                            .sendMessage(node.id, path, ByteArray(0))
                        try {
                            Tasks.await(sendMessageTask)
                            Log.v(TAG, "SendThread: message send to " + node.displayName)
                        } catch (exception: ExecutionException) {
                            Log.e(TAG, "Task failed: $exception")
                        } catch (exception: InterruptedException) {
                            Log.e(TAG, "Interrupt occurred: $exception")
                        }
                    }
                } catch (exception: ExecutionException) {
                    Log.e(TAG, "Task failed: $exception")
                } catch (exception: InterruptedException) {
                    Log.e(TAG, "Interrupt occurred: $exception")
                }
            }
        }
    }

    companion object {
        private val TAG = SendMsgToPhone::class.java.simpleName
        fun sndMSG(context: Context,path: String, lastUpdateTime: Long) {
            if (System.currentTimeMillis() - lastUpdateTime >= 5000) {
            SendMsgToPhone().SendThread(context,path).start()
            }
            else
                Log.e(TAG, "Too many updates")
        }
    }
}