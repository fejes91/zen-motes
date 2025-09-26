package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.getPlatform
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainMenuViewModel(
    private val preferencesService: PreferencesService
) : ViewModel() {

    val appTheme = preferencesService.getTheme.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null
    )
    val soundEnabled = preferencesService.getSoundEnabled.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = true
    )
    val highScore = preferencesService.getHighScore.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = 0
    )

    private val _appVersion = MutableStateFlow<String?>(getPlatform().appVersion)
    val appVersion: StateFlow<String?> = _appVersion.asStateFlow()

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesService.saveTheme(theme)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesService.saveSoundEnabled(enabled)
        }
    }
}