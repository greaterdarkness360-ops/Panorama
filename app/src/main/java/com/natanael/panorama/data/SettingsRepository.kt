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
        private val HIDDEN_PACKAGES_KEY = stringSetPreferencesKey("hidden_packages")
        private val FAVORITE_PACKAGES_KEY = stringSetPreferencesKey("favorite_packages")
    }

    val hiddenPackagesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[HIDDEN_PACKAGES_KEY] ?: emptySet()
    }

    val favoritePackagesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[FAVORITE_PACKAGES_KEY] ?: emptySet()
    }

    suspend fun toggleAppVisibility(packageName: String, hide: Boolean) {
        context.dataStore.edit { preferences ->
            val current = preferences[HIDDEN_PACKAGES_KEY]?.toMutableSet() ?: mutableSetOf()
            if (hide) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            preferences[HIDDEN_PACKAGES_KEY] = current
        }
    }

    suspend fun toggleFavorite(packageName: String, favorite: Boolean) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAVORITE_PACKAGES_KEY]?.toMutableSet() ?: mutableSetOf()
            if (favorite) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            preferences[FAVORITE_PACKAGES_KEY] = current
        }
    }
}
