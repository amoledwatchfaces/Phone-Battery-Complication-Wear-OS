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

import android.graphics.drawable.Icon
import androidx.datastore.core.DataStore
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
import com.google.android.gms.wearable.DataClient
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.utils.BitmapCreator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsIconsComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>

    @Inject
    lateinit var dataClient: DataClient

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        image = Icon.createWithResource(this, R.drawable.ic_notif),
                        type = SmallImageType.ICON).build(),
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

    override fun onComplicationActivated(complicationInstanceId: Int, type: ComplicationType) {
        super.onComplicationActivated(complicationInstanceId, type)
        CoroutineScope(Dispatchers.IO).launch {
            MobileListener.sendNotificationsRequest(dataClient)
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {

        val notificationsList = dataStore.data.first().notificationsList

        return when (request.complicationType) {

            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        image =
                        if (notificationsList.isEmpty()){
                            Icon.createWithResource(this, R.drawable.ic_notif_none)
                        }
                        else {
                            if (notificationsList.size == 1) {
                                Icon.createWithBitmap(BitmapCreator.createSingleBitmap(notificationsList[0]))
                                    //.setTint(Color.WHITE)
                            }
                            else {
                                Icon.createWithBitmap(BitmapCreator.createCompositeBitmap(notificationsList,this))
                                    //.setTint(Color.WHITE)
                            }
                        },
                        type = if (dataStore.data.first().notificationsIconType == 1) SmallImageType.PHOTO else SmallImageType.ICON)
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
                        if (notificationsList.isEmpty()){
                            Icon.createWithBitmap(BitmapCreator.createLineBitmapEmpty())
                        }
                        else {
                            Icon.createWithBitmap(BitmapCreator.createLineCompositeBitmap(notificationsList, this))
                                //.setTint(Color.WHITE)
                        },
                        type = SmallImageType.ICON)
                        .build())
                    .build()
            }

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }
}


