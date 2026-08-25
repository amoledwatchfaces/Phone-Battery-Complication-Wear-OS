package com.weartools.phonebattcomp.complication

import android.graphics.*
import android.graphics.drawable.Icon
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.receiver.MediaTapReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import androidx.core.graphics.createBitmap

@AndroidEntryPoint
class NowPlayingComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(image = Icon.createWithResource(this, R.drawable.music_note_24px), type = SmallImageType.ICON).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }
            ComplicationType.PHOTO_IMAGE -> {
                PhotoImageComplicationData.Builder(
                    photoImage = Icon.createWithResource(this, R.drawable.music_note_24px),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }
            else -> null
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val prefs = dataStore.data.first()
        val artworkBytes = prefs.nowPlayingArtwork
        val playStatus = prefs.nowPlayingStatus
        
        val tapIntent = MediaTapReceiver.getToggleIntent(this)
        val icon = createArtworkIcon(artworkBytes, playStatus)

        return when (request.complicationType) {
            ComplicationType.SMALL_IMAGE -> {
                val imageType = if (artworkBytes.isNullOrEmpty()) SmallImageType.ICON else SmallImageType.PHOTO
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(image = icon, type = imageType).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTapAction(tapIntent)
                    .build()
            }
            ComplicationType.PHOTO_IMAGE -> {
                PhotoImageComplicationData.Builder(
                    photoImage = icon,
                    contentDescription = ComplicationText.EMPTY)
                    .setTapAction(tapIntent)
                    .build()
            }
            else -> null
        }
    }

    private fun createArtworkIcon(artworkBytes: List<Byte>?, playStatus: Boolean): Icon {
        if (artworkBytes == null) return Icon.createWithResource(this, R.drawable.music_note_24px)

        return try {
            val array = artworkBytes.toByteArray()
            var bitmap = BitmapFactory.decodeByteArray(array, 0, array.size)
            if (bitmap != null) {
                if (!playStatus) {
                    val mutableBitmap = createBitmap(bitmap.width, bitmap.height)
                    val canvas = Canvas(mutableBitmap)
                    val paint = Paint().apply {
                        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
                            setScale(0.5f, 0.5f, 0.5f, 1f)
                        })
                    }
                    canvas.drawBitmap(bitmap, 0f, 0f, paint)

                    val playIcon = ContextCompat.getDrawable(this, R.drawable.play_arrow_24px)
                    playIcon?.let {
                        val size = (mutableBitmap.width * 0.5f).toInt()
                        val left = (mutableBitmap.width - size) / 2
                        val top = (mutableBitmap.height - size) / 2
                        it.setBounds(left, top, left + size, top + size)
                        it.setTint(Color.WHITE)
                        it.draw(canvas)
                    }
                    bitmap = mutableBitmap
                }
                Icon.createWithBitmap(bitmap)
            } else {
                Icon.createWithResource(this, R.drawable.music_note_24px)
            }
        } catch (_: Exception) {
            Icon.createWithResource(this, R.drawable.music_note_24px)
        }
    }
}
