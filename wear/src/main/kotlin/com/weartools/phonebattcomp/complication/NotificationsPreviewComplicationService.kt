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
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.google.android.gms.wearable.DataClient
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsPreviewComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>

    @Inject
    lateinit var dataClient: DataClient

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "Hey, how are you?").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTitle(PlainComplicationText.Builder("Joseph").build())
                    .setMonochromaticImage(
                        MonochromaticImage.Builder(
                            Icon.createWithResource(this, R.drawable.ic_chat))
                            .build()
                    )
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

        val preferences = dataStore.data.first()
        val notificationsList = preferences.notificationsList

        return when (request.complicationType) {

            ComplicationType.LONG_TEXT -> {

                if (notificationsList.isEmpty()){
                    LongTextComplicationData.Builder(
                        text = PlainComplicationText.Builder(text = getString(R.string.no_notifications)).build(),
                        contentDescription = ComplicationText.EMPTY)
                        .setMonochromaticImage(null)
                        .build()
                }
                else {
                    val hasText = preferences.notificationText.isNotBlank()
                    val hasTitle = preferences.notificationTitle.isNotBlank()

                    LongTextComplicationData.Builder(
                        text = PlainComplicationText.Builder(
                            if (hasText) preferences.notificationText
                            else if (hasTitle) preferences.notificationTitle
                            else "- -"
                        ).build(),
                        contentDescription = ComplicationText.EMPTY)
                        .setTitle(
                            if (hasText && hasTitle) PlainComplicationText.Builder(text = preferences.notificationTitle).build()
                            else null
                        )
                        .setMonochromaticImage(
                            MonochromaticImage.Builder(
                                Icon.createWithBitmap(
                                    BitmapFactory.decodeByteArray(notificationsList[0], 0, notificationsList[0].size)
                                ))
                                .build()
                        )
                        .build()
                }
            }

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }
}


