package com.weartools.phonebattcomp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject


class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
): Repository {

    val preferencesVersion: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PREFS_VERSION]?: 3
    }

    val activeSync: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ACTIVE_SYNC] ?: false
    }
    val calendarSync: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[CALENDAR_SYNC] ?: false
    }
    val notificationsSync: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_SYNC] ?: true
    }

    val backgroundServiceState: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[BACKGROUND_SERVICE] ?: false
    }

    suspend fun setActiveSyncState(state: Boolean) {
        dataStore.edit { prefs ->
            prefs[ACTIVE_SYNC] = state
        }
    }
    suspend fun setCalendarSyncState(state: Boolean) {
        dataStore.edit { prefs ->
            prefs[CALENDAR_SYNC] = state
        }
    }
    suspend fun setNotificationsSyncState(state: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_SYNC] = state
        }
    }
    suspend fun setBackgroundServiceState(state: Boolean) {
        dataStore.edit { prefs ->
            prefs[BACKGROUND_SERVICE] = state
        }
    }

    // Function to save a list of CalendarEvent to DataStore
    suspend fun saveCalendars(calendars: List<CalendarInfo>) {
        val calendarsJson = Json.encodeToString(calendars)
        dataStore.edit { prefs ->
            prefs[CALENDARS_KEY] = calendarsJson
            prefs[SYNCED_CALENDAR_IDS] = calendars.map { it.calendarId }.joinToString(",")
        }
    }
    // Function to retrieve the list of CalendarEvent from DataStore
    fun getCalendars(): Flow<List<CalendarInfo>> {
        return dataStore.data
            .map { preferences ->
                // Retrieve the JSON string from DataStore
                val calendarsJson = preferences[CALENDARS_KEY] ?: "[]"
                // Convert the JSON string back to a list of CalendarEvent
                Json.decodeFromString(calendarsJson)
            }
    }
    val syncedCalendarsIdsString: Flow<String> = dataStore.data.map { prefs ->
        prefs[SYNCED_CALENDAR_IDS] ?: ""
    }

    companion object {
        private val ACTIVE_SYNC = booleanPreferencesKey("active_sync")
        private val CALENDAR_SYNC = booleanPreferencesKey("calendar_sync")
        private val BACKGROUND_SERVICE = booleanPreferencesKey("background_service")
        private val NOTIFICATIONS_SYNC = booleanPreferencesKey("notifications_sync")
        private val CALENDARS_KEY = stringPreferencesKey("calendars")
        private val SYNCED_CALENDAR_IDS = stringPreferencesKey("synced_calendar_ids")
        val PREFS_VERSION = intPreferencesKey(name = "preferencesVersion")
    }
}
