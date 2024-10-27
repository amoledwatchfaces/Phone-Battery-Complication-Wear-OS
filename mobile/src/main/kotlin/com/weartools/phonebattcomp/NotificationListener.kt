package com.weartools.phonebattcomp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.weartools.phonebattcomp.data.DataStoreRepository
import com.weartools.phonebattcomp.di.ServiceCommunication
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListener : NotificationListenerService() {

    @Inject
    lateinit var dataRepository: DataStoreRepository
    @Inject
    lateinit var dataClient: DataClient

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Shared Job to cancel previous task before starting a new one
    private var notificationJob: Job? = null

    // Store the previous icons
    private var lastBitmapsSent: List<Bitmap>? = null

    companion object {
        private const val ICON_SIZE = 48
        private const val URI = "/foobar"
        private const val NOTIFICATIONS_UPDATE_KEY = "update-time"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        serviceScope.launch {
            // Set background service state to true
            dataRepository.setBackgroundServiceState(true)

            // Send notifications to watch when listener is connected
            sendToWatch()

            // Send notifications to watch when WearListener requests it using flow collection
            ServiceCommunication.sendToWatchFlow.collect { sendToWatch(forceSend = true) }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        //Log.w(ContentValues.TAG, "Notification Posted!")
        sendToWatch()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        //Log.w(ContentValues.TAG, "Notification Removed!")
        sendToWatch()
    }
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    fun sendToWatch(
        forceSend: Boolean = false
    ) {
        // Cancel the previous job and start a new one
        notificationJob?.cancel()
        notificationJob = serviceScope.launch {
            // Check if notifications should be synced
            if (!dataRepository.notificationsSync.first()) return@launch

            /** Catch exceptions when onListenerConnected is not called before accessing notifications **/
            val notifications = try { activeNotifications } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                return@launch
            }

            val currentBitmaps = mutableListOf<Bitmap>()

            for (notification in notifications) {

                if (notification.isOngoing) { continue }

                notification.notification.smallIcon?.let {
                    it.loadDrawable(this@NotificationListener)?.let {
                            drawable -> drawableToBitmap(drawable)
                    }
                }?.let { bitmap ->
                    if (currentBitmaps.none { it.sameAs(bitmap) }) {
                        currentBitmaps.add(bitmap)
                    }
                }
            }

            // Compare current icons with last sent icons
            if (currentBitmaps == lastBitmapsSent && !forceSend) { return@launch }

            // Save current icons as last sent icons
            lastBitmapsSent = currentBitmaps

            Log.i("NotificationListener", "Notifications size: ${currentBitmaps.size}")

            // Create DataMap Request
            val putDataMapReq = PutDataMapRequest.create(URI)
            putDataMapReq.dataMap.apply {
                currentBitmaps.forEachIndexed { i, bitmap ->
                    if (i < 8) {
                        // send only first 8 drawables
                        putByteArray("icon$i", bitmapToByteArray(bitmap))
                    } else {
                        // Add an empty byte array for each drawable above the first 8
                        putByteArray("icon$i", ByteArray(0))
                    }
                }
                putLong(NOTIFICATIONS_UPDATE_KEY, System.currentTimeMillis())
            }

            try {
                dataClient.putDataItem(putDataMapReq.asPutDataRequest().setUrgent()).await()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("NotificationListener", "Notification sending failed: ${e.message}")
            }
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.setTint(Color.WHITE)
        drawable.draw(canvas)
        return bitmap
    }
}
