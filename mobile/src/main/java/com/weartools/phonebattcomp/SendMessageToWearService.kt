package com.weartools.phonebattcomp

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import java.util.concurrent.ExecutionException

class SendMessageToWearService {
    class SendMsgToWear : Thread() {
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
        private val TAG = SendMsgToWear::class.java.simpleName
        fun sndMSGWear(context: Context,path: String) {
            SendMsgToWear().SendThread(context,path).start()
        }
    }
}