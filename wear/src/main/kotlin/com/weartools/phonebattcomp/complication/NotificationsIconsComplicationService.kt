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

import android.graphics.Color
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.DataStoreRepository
import com.weartools.phonebattcomp.utils.BitmapCreator
import com.weartools.phonebattcomp.utils.BitmapCreatorLine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Global variables **/
var notificationsByteArrayList: List<ByteArray> = emptyList()

@AndroidEntryPoint
class NotificationsIconsComplicationService : SuspendingComplicationDataSourceService() {

    @Inject lateinit var repository: DataStoreRepository

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        image = Icon.createWithResource(this, R.drawable.ic_notif),
                        type = SmallImageType.PHOTO).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "---").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setSmallImage(SmallImage.Builder(
                        image = Icon.createWithResource(this, R.drawable.ic_notif_none_line_preview),
                        type = SmallImageType.ICON)
                        .build())
                    .build()
            }

            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {

        val noNotification = notificationsByteArrayList.isEmpty()

        return when (request.complicationType) {

            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        image =
                        if (noNotification.not()) {
                            if (notificationsByteArrayList.size == 1) {
                                Icon.createWithBitmap(BitmapCreator.createSingleBitmap(notificationsByteArrayList[0]))
                                    .setTint(Color.WHITE)
                            }
                            else {
                                Icon.createWithBitmap(BitmapCreator.createCompositeBitmap(notificationsByteArrayList))
                                    .setTint(Color.WHITE)
                            }
                        }
                        else Icon.createWithResource(this, R.drawable.ic_notif_none),
                        type = if (repository.notificationsIconType.first() == 0) SmallImageType.ICON else SmallImageType.PHOTO)
                        .build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "---").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setSmallImage(SmallImage.Builder(
                        image =
                        if (noNotification.not()) {
                            Icon.createWithBitmap(BitmapCreatorLine.createLineCompositeBitmap(notificationsByteArrayList)).setTint(Color.WHITE)
                        }
                        else Icon.createWithBitmap(BitmapCreatorLine.createLineCompositeBitmapEmpty()),
                        type = SmallImageType.ICON)
                        .build())
                    .build()
            }

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }
}


