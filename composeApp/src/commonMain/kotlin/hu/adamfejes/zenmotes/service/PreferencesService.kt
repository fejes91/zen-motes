package hu.adamfejes.zenmotes.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesService(private val dataStore: DataStore<Preferences>) {

    val getTheme: Flow<AppTheme> = dataStore.data.map { preferences ->
        preferences[THEME_KEY]?.let { AppTheme.valueOf(it) } ?: AppTheme.SYSTEM
    }

    val getSoundEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SOUND_ENABLED_KEY] ?: true
    }

    val getHighScore: Flow<Int?> = dataStore.data.map { preferences ->
        preferences[HIGH_SCORE_KEY]
    }

    suspend fun saveTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun saveSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SOUND_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveHighScore(score: Int) {
        dataStore.edit { preferences ->
            preferences[HIGH_SCORE_KEY] = score
        }
    }

    companion object {
        private val THEME_KEY = stringPreferencesKey("selected_theme")
        private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
        private val HIGH_SCORE_KEY = intPreferencesKey("high_score")
    }
}