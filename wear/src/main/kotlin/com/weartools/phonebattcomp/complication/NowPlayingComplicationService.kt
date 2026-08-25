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

import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
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

@AndroidEntryPoint
class NowPlayingComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "Song Title").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTitle(PlainComplicationText.Builder(text = "Artist Name").build())
                    .setSmallImage(SmallImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_notif), type = SmallImageType.PHOTO).build())
                    .build()
            }
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_notif), type = SmallImageType.PHOTO).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }
            ComplicationType.PHOTO_IMAGE -> {
                PhotoImageComplicationData.Builder(
                    photoImage = Icon.createWithResource(this, R.drawable.ic_notif),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }
            else -> null
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val prefs = dataStore.data.first()
        val title = prefs.nowPlayingTitle
        val artist = prefs.nowPlayingArtist
        val artworkBytes = prefs.nowPlayingArtwork

        val hasMedia = title.isNotEmpty()
        
        val tapIntent = MediaTapReceiver.getToggleIntent(this)
        
        val icon = if (artworkBytes != null) {
            try {
                val array = artworkBytes.toByteArray()
                val bitmap = BitmapFactory.decodeByteArray(array, 0, array.size)
                if (bitmap != null) Icon.createWithBitmap(bitmap)
                else Icon.createWithResource(this, R.drawable.ic_notif)
            } catch (_: Exception) {
                Icon.createWithResource(this, R.drawable.ic_notif)
            }
        } else {
            Icon.createWithResource(this, R.drawable.ic_notif)
        }

        return when (request.complicationType) {
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = if (hasMedia) title else getString(R.string.no_media)).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .apply {
                        if (hasMedia) {
                            setTitle(PlainComplicationText.Builder(text = artist).build())
                            setSmallImage(SmallImage.Builder(image = icon, type = SmallImageType.PHOTO).build())
                        }
                    }
                    .setTapAction(tapIntent)
                    .build()
            }
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(image = icon, type = SmallImageType.PHOTO).build(),
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
}
