package com.weartools.phonebattcomp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.media.session.MediaSession
import android.graphics.ImageDecoder
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.content.ComponentName
import android.provider.MediaStore
import androidx.core.graphics.createBitmap
import androidx.datastore.core.DataStore
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.di.ServiceCommunication
import com.weartools.phonebattcomp.receiver.BatteryStatusBroadcastReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import androidx.core.graphics.scale
import androidx.core.net.toUri
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class NotificationListener : NotificationListenerService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>

    @Inject
    lateinit var dataClient: DataClient

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var syncEnabled = true
    private var mediaSyncEnabled = true

    private val mutex = Mutex()

    private var notificationsCount = 0

    // Shared Job to cancel previous task before starting a new one
    private var notificationJob: Job? = null

    // Store the previous bitmaps
    private var lastBitmapsSent = mutableListOf<Bitmap>()
    // Store the previous text+title
    private var lastTextTitleSent = ""

    private lateinit var mediaSessionManager: MediaSessionManager
    private val activeMediaControllers = mutableMapOf<MediaController, MediaController.Callback>()

    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        updateMediaControllers(controllers)
    }

    companion object {
        private const val ICON_SIZE = 48
        private const val URI = "/foobar"
        private const val NOTIFICATIONS_UPDATE_KEY = "ts"
        private const val NOTIFICATION_TEXT = "ntext"
        private const val NOTIFICATION_TITLE = "ntitle"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationListener", "onCreate")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationListener", "onListenerConnected")

        mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        try {
            mediaSessionManager.addOnActiveSessionsChangedListener(sessionListener, ComponentName(this, NotificationListener::class.java))
            val sessions = mediaSessionManager.getActiveSessions(ComponentName(this, NotificationListener::class.java))
            Log.d("NotificationListener", "Initial active sessions: ${sessions.size}")
            updateMediaControllers(sessions)
        } catch (e: SecurityException) {
            Log.e("NotificationListener", "SecurityException: Is Notification Listener permission granted?", e)
        }

        BatteryStatusBroadcastReceiver.subscribeToUpdates(this)

        serviceScope.launch {
            // Set background service state to true
            dataStore.updateData { it.copy(backgroundServiceState = true) }

            // Send notifications to watch when listener is connected
            val prefs = dataStore.data.first()
            syncEnabled = prefs.notificationsSync
            mediaSyncEnabled = prefs.mediaPlaybackSync
            
            if (syncEnabled) debounceSendToWatch(updateTime = System.currentTimeMillis())
            syncMediaToWatch()

            // Send notifications to watch when WearListener requests it using flow collection
            ServiceCommunication.sendToWatchFlow.collect {

                val currentPrefs = dataStore.data.first()
                syncEnabled = currentPrefs.notificationsSync
                mediaSyncEnabled = currentPrefs.mediaPlaybackSync

                if (syncEnabled){
                    debounceSendToWatch(
                        updateTime = System.currentTimeMillis(),
                        forceSend = true
                    )
                }
                syncMediaToWatch()
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (++notificationsCount % 10 == 0) {
            BatteryStatusBroadcastReceiver.subscribeToUpdates(this)
        }
        if (syncEnabled) debounceSendToWatch(updateTime = System.currentTimeMillis())
        syncMediaToWatch()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (syncEnabled) debounceSendToWatch(updateTime = System.currentTimeMillis())
        syncMediaToWatch()
    }

    private fun updateMediaControllers(controllers: List<MediaController>?) {
        Log.d("NotificationListener", "updateMediaControllers: ${controllers?.size} controllers found")
        // Remove old callbacks
        activeMediaControllers.forEach { (controller, callback) ->
            controller.unregisterCallback(callback)
        }
        activeMediaControllers.clear()

        controllers?.forEach { controller ->
            val callback = object : MediaController.Callback() {
                override fun onMetadataChanged(metadata: MediaMetadata?) {
                    syncMediaToWatch()
                }

                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    syncMediaToWatch()
                }
            }
            controller.registerCallback(callback)
            activeMediaControllers[controller] = callback
        }
        syncMediaToWatch()
    }

    private fun syncMediaToWatch() {
        Log.d("NotificationListener", "syncMediaToWatch enabled: $mediaSyncEnabled")
        if (!mediaSyncEnabled) return
        serviceScope.launch {
            val controllers = mediaSessionManager.getActiveSessions(ComponentName(this@NotificationListener, NotificationListener::class.java))

            val activeSession = controllers.firstOrNull { 
                it.playbackState?.state == PlaybackState.STATE_PLAYING
            }

            Log.d("NotificationListener", "Selected session: ${activeSession?.packageName}")

            val putDataMapReq = PutDataMapRequest.create("/now-playing")
            if (activeSession != null) {
                val metadata = activeSession.metadata
                val playbackState = activeSession.playbackState
                
                val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
                val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                val status = playbackState?.state == PlaybackState.STATE_PLAYING

                Log.d("NotificationListener", "Sending Now Playing: $title by $artist (playing: $status)")

                putDataMapReq.dataMap.apply {
                    putString("title", title)
                    putString("artist", artist)
                    putBoolean("status", status)

                    var bitmap = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                        ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                        ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
                        ?: metadata?.description?.iconBitmap
                    
                    if (bitmap != null) Log.d("NotificationListener", "Bitmap found in metadata: ${bitmap.width}x${bitmap.height}")
                    
                    // Check URIs if bitmap is still null
                    if (bitmap == null) {
                        val uriString = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
                            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI)
                            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI)
                            ?: metadata?.description?.iconUri?.toString()
                        
                        if (uriString != null) {
                            Log.d("NotificationListener", "Found artwork URI: $uriString")
                            try {
                                val uri = uriString.toUri()
                                if (uri.scheme == "content" || uri.scheme == "android.resource" || uri.scheme == "file") {
                                    bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        val source = ImageDecoder.createSource(contentResolver, uri)
                                        ImageDecoder.decodeBitmap(source)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        MediaStore.Images.Media.getBitmap(contentResolver, uri)
                                    }
                                    Log.d("NotificationListener", "Bitmap loaded from URI: ${bitmap?.width}x${bitmap?.height}")
                                }
                            } catch (e: Exception) {
                                Log.e("NotificationListener", "Failed to load bitmap from URI", e)
                            }
                        }
                    }

                    // Fallback to notification large icon if still null
                    if (bitmap == null) {
                        Log.d("NotificationListener", "Metadata bitmap/URI null, checking notification fallback...")
                        val notification = activeNotifications.find { it.packageName == activeSession.packageName }
                        
                        // Try android.largeIcon first
                        val largeIcon = notification?.notification?.getLargeIcon()
                        if (largeIcon != null) {
                            val drawable = largeIcon.loadDrawable(this@NotificationListener)
                            if (drawable != null) {
                                bitmap = drawableToBitmap(drawable, 256)
                                Log.d("NotificationListener", "Bitmap found in notification getLargeIcon()")
                            }
                        }
                        
                        // Extra deep dive into extras
                        if (bitmap == null && notification != null) {
                            val extras = notification.notification.extras
                            @Suppress("DEPRECATION")
                            val extraBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                extras.getParcelable("android.largeIcon.big", Bitmap::class.java)
                                    ?: extras.getParcelable("android.picture", Bitmap::class.java)
                            } else {
                                extras.getParcelable("android.largeIcon.big")
                                    ?: extras.getParcelable("android.picture")
                            }
                            
                            if (extraBitmap != null) {
                                bitmap = extraBitmap
                                Log.d("NotificationListener", "Bitmap found in notification extras deep dive")
                            }
                        }
                    }

                    if (bitmap != null) {
                        val scaledBitmap = if (bitmap.width > 256 || bitmap.height > 256) {
                            Log.d("NotificationListener", "Scaling bitmap down from ${bitmap.width}x${bitmap.height}")
                            bitmap.scale(256, 256)
                        } else bitmap
                        val byteArray = bitmapToByteArray(scaledBitmap, isArtwork = true)
                        Log.d("NotificationListener", "Sending artwork byte array: ${byteArray.size} bytes")
                        putByteArray("artwork", byteArray)
                    } else {
                        Log.d("NotificationListener", "No bitmap found anywhere")
                        remove("artwork")
                    }
                }
            } else {
                putDataMapReq.dataMap.apply {
                    putString("title", "")
                    putString("artist", "")
                    putBoolean("status", false)
                    remove("artwork")
                }
            }

            try {
                dataClient.putDataItem(putDataMapReq.asPutDataRequest().setUrgent()).await()
            } catch (e: Exception) {
                Log.e("NotificationListener", "syncMediaToWatch failed: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaSessionManager.isInitialized) {
            mediaSessionManager.removeOnActiveSessionsChangedListener(sessionListener)
        }
        activeMediaControllers.forEach { (controller, callback) ->
            controller.unregisterCallback(callback)
        }
        activeMediaControllers.clear()
        serviceScope.cancel()
    }

    private fun debounceSendToWatch(
        updateTime: Long,
        forceSend: Boolean = false
    ) {
        notificationJob?.cancel()
        notificationJob = serviceScope.launch {

            delay(300.milliseconds)  // Debounce delay

            if (isActive) {
                sendToWatch(updateTime, forceSend)
            }
        }
    }

    suspend fun sendToWatch(
        updateTime: Long,
        forceSend: Boolean
    ) {
        mutex.withLock {

            /** Catch exceptions when onListenerConnected is not called before accessing notifications **/
            val notifications = try {
                activeNotifications
            } catch (_: Exception) {
                return@withLock
            }

            // Initialize current bitmaps list.
            val currentBitmaps = mutableListOf<Bitmap>()
            var hasPreview = false
            var notificationText = ""
            var notificationTitle = ""

            // Get activeNotifications bitmaps and add unique ones to currentBitmaps.
            for (notification in notifications) {


                if (notification.isOngoing) { continue }
                if (currentBitmaps.size >= 9) { break } // Break the loop when we have 9 distinct icons (...)

                val text = notification.notification.extras.getCharSequence("android.text") ?: ""
                val title = notification.notification.extras.getCharSequence("android.title") ?: ""

                if (text.isBlank() && title.isBlank()) { continue }

                notification.notification.smallIcon?.let {
                    it.loadDrawable(this@NotificationListener)?.let { drawable ->
                        drawableToBitmap(drawable)
                    }
                }?.let { bitmap ->
                    if (currentBitmaps.none { it.sameAs(bitmap) }) {
                        currentBitmaps.add(bitmap)
                        if (hasPreview.not()){
                            hasPreview = true
                            notificationText = text.toString()
                            if (text != title) { notificationTitle = title.toString() }
                        }
                    }
                }
            }

            /** If update is not forced and list sizes are same, check bitmaps pairs
             * to see if the bitmaps on their positions matches. If bitmaps match, return early
             * so there is no need to send new complication update, else, continue with sending
            **/

            if (!forceSend && currentBitmaps.size == lastBitmapsSent.size) {
                var allIdentical = true
                for (i in 0 until minOf(8, currentBitmaps.size)) {
                    if (!currentBitmaps[i].sameAs(lastBitmapsSent[i])) {
                        // If any compared bitmaps are different, break the loop
                        allIdentical = false
                        break
                    }
                }
                if (lastTextTitleSent != (notificationText + notificationTitle)) allIdentical = false
                // If loop completes without breaking, all bitmaps are identical
                if (allIdentical) { return@withLock }
            }

            // Set currentBitmaps as lastBitmapsSent for next comparison
            lastBitmapsSent = currentBitmaps
            lastTextTitleSent = notificationText + notificationTitle

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
                putLong(NOTIFICATIONS_UPDATE_KEY, updateTime)
                putString(NOTIFICATION_TEXT, notificationText)
                putString(NOTIFICATION_TITLE, notificationTitle)
            }

            // Send DataMap to watch using DataClient
            try {
                dataClient
                    .putDataItem(putDataMapReq.asPutDataRequest().setUrgent())
                    .await()
            } catch (e: Exception) {
                Log.e("NotificationListener", "sendToWatch failed: ${e.message}")
            }
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap, isArtwork: Boolean = false): ByteArray {
        val stream = ByteArrayOutputStream()
        if (isArtwork) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        } else {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        return stream.toByteArray()
    }
    private fun drawableToBitmap(drawable: Drawable, size: Int = ICON_SIZE): Bitmap {
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        if (size == ICON_SIZE) drawable.setTint(Color.WHITE)
        drawable.draw(canvas)
        return bitmap
    }
}
