package com.natanael.panorama.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "panorama_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val HIDDEN_KEYS_KEY = stringSetPreferencesKey("hidden_app_keys")
        private val FAVORITE_KEYS_KEY = stringSetPreferencesKey("favorite_app_keys")
    }

    val hiddenKeysFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[HIDDEN_KEYS_KEY] ?: emptySet()
    }

    val favoriteKeysFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[FAVORITE_KEYS_KEY] ?: emptySet()
    }

    suspend fun toggleAppVisibility(uniqueKey: String, hide: Boolean) {
        context.dataStore.edit { preferences ->
            val current = preferences[HIDDEN_KEYS_KEY]?.toMutableSet() ?: mutableSetOf()
            if (hide) {
                current.add(uniqueKey)
            } else {
                current.remove(uniqueKey)
            }
            preferences[HIDDEN_KEYS_KEY] = current
        }
    }

    suspend fun toggleFavorite(uniqueKey: String, favorite: Boolean) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAVORITE_KEYS_KEY]?.toMutableSet() ?: mutableSetOf()
            if (favorite) {
                current.add(uniqueKey)
            } else {
                current.remove(uniqueKey)
            }
            preferences[FAVORITE_KEYS_KEY] = current
        }
    }
}
