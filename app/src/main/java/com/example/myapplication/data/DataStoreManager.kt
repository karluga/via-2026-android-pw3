package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "repository_g2_settings")

object ThemeOptions {
    const val DEFAULT = "Default"
    const val DARK = "Dark theme"
    const val LIGHT = "Light theme"
}

class DataStoreManager(private val context: Context) {

    companion object {
        val SAVED_TEXT_KEY = stringPreferencesKey("saved_user_text")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }

    val savedTextFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SAVED_TEXT_KEY]
        }

    val themeModeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_MODE_KEY] ?: ThemeOptions.DEFAULT
        }

    suspend fun saveText(text: String) {
        context.dataStore.edit { preferences ->
            preferences[SAVED_TEXT_KEY] = text
        }
    }

    suspend fun saveThemeMode(themeMode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode
        }
    }
}
