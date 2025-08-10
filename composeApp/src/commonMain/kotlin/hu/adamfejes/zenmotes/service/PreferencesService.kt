package hu.adamfejes.zenmotes.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesService(private val dataStore: DataStore<Preferences>) {

    val getTheme: Flow<AppTheme> = dataStore.data.map { preferences ->
        preferences[THEME_KEY]?.let { AppTheme.valueOf(it) } ?: AppTheme.DARK
    }

    suspend fun saveTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    companion object {
        private val THEME_KEY = stringPreferencesKey("selected_theme")
    }
}