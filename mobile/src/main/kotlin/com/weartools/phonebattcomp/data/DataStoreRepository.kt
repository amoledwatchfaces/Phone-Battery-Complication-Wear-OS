package com.weartools.phonebattcomp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
): Repository {

    val preferencesVersion: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PREFS_VERSION]?: 2
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
    companion object {
        private val ACTIVE_SYNC = booleanPreferencesKey("active_sync")
        private val CALENDAR_SYNC = booleanPreferencesKey("calendar_sync")
        private val BACKGROUND_SERVICE = booleanPreferencesKey("background_service")
        private val NOTIFICATIONS_SYNC = booleanPreferencesKey("notifications_sync")
        val PREFS_VERSION = intPreferencesKey(name = "preferencesVersion")
    }
}
