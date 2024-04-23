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
        prefs[PREFS_VERSION]?: 1
    }

    val activeSync: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ACTIVE_SYNC] ?: false
    }
    val notificationsSync: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_SYNC] ?: true
    }
    suspend fun setActiveSyncState(state: Boolean) {
        dataStore.edit { prefs ->
            prefs[ACTIVE_SYNC] = state
        }
    }
    suspend fun setNotificationsSyncState(state: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_SYNC] = state
        }
    }
    companion object {
        private val ACTIVE_SYNC = booleanPreferencesKey("active_sync")
        private val NOTIFICATIONS_SYNC = booleanPreferencesKey("notifications_sync")
        val PREFS_VERSION = intPreferencesKey(name = "preferencesVersion")
    }
}
