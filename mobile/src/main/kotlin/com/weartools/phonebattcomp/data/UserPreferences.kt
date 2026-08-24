package com.weartools.phonebattcomp.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/** PREFERENCES **/
@Serializable
data class UserPreferences(

    // Phone Battery
    val backgroundServiceState: Boolean = false,
    val activeSync: Boolean = false,
    val calendarSync: Boolean = false,
    val notificationsSync: Boolean = true,

    // Crashlytics
    val crashlytics: Boolean = false,
    val crashlyticsNoticeAccepted: Boolean = false,

    // Calendar Events
    val syncedCalendarsIds: String = "",
    val syncedCalendars: List<CalendarInfo> = emptyList(),
)

/** REPOSITORY **/
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<UserPreferences>
){
    fun getPreferences() = dataStore.data
}

/** SERIALIZER **/
object UserPreferencesSerializer : Serializer<UserPreferences> {

    override val defaultValue = UserPreferences()

    override suspend fun readFrom(input: InputStream): UserPreferences {
        try {
            val json = Json { ignoreUnknownKeys = true } // Ignore unknown keys to prevent errors when removing some parameters
            return json.decodeFromString(
                UserPreferences.serializer(), input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read UserPrefs", serialization)
        }
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(UserPreferences.serializer(), t)
                    .encodeToByteArray()
            )
        }
    }
}
