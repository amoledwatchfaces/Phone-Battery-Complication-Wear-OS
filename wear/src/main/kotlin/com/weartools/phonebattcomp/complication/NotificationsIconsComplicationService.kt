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
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.DataRepository
import com.weartools.phonebattcomp.utils.BitmapCreator
import kotlinx.coroutines.flow.first
import java.util.Base64

class NotificationsIconsComplicationService : SuspendingComplicationDataSourceService() {

    private val repository by lazy { DataRepository(this) }

    /** DRAW NOTIFICATION ICONS **/
    /*
                for ((i, bitmap) in (bitmaps).withIndex()) {
                    canvas.drawBitmap(bitmap, null, Rect(x + i * (ICON_SIZE + padding), y,
                            x + i * (ICON_SIZE + padding) + ICON_SIZE, y + ICON_SIZE
                    ),
                            textPaint)
                }
     */

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
                smallImage = SmallImage.Builder(
                    image = Icon.createWithResource(this, R.drawable.ic_notif
                    ),
                    type = SmallImageType.ICON
                ).build(),
                contentDescription = PlainComplicationText.Builder(text = "SMALL_IMAGE.").build()
            )
                .setTapAction(null)
                .build()
            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        //Log.w(TAG, "Refreshing Notifications Complication!")

        val data = repository.byteArrayMutableListJsonString.first()
        val noNotification = data == ""


        /*
            private fun loadBitmapFromByteArray(byteArray: ByteArray): Bitmap {
                return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            }
         */

        return when (request.complicationType) {

            ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
                smallImage = SmallImage.Builder(
                    image =
                    if (noNotification.not()) {
                        val parts = data.split("|")
                        val byteArrayList = parts.map { Base64.getDecoder().decode(it) }.toMutableList()
                        //Log.w(ContentValues.TAG, "Icon List size: ${byteArrayList.size}")
                        if (byteArrayList.size == 1) {
                            Icon.createWithBitmap(BitmapCreator.createSingleBitmap(byteArrayList[0]))
                                .setTint(Color.WHITE)
                        }
                        else {
                            Icon.createWithBitmap(BitmapCreator.createCompositeBitmap(byteArrayList))
                                .setTint(Color.WHITE)
                        }
                    }
                    else Icon.createWithResource(this, R.drawable.ic_notif_none),
                    type = SmallImageType.ICON)
                    .build(),
                contentDescription = PlainComplicationText.Builder(text = "Notification Icons").build()
            )
                .setTapAction(null)
                .build()

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }
}


