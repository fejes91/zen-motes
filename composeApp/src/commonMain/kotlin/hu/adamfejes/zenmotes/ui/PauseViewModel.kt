package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.logic.GameStateHolder
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.service.AnalyticsService
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.service.SoundManager
import hu.adamfejes.zenmotes.ui.Constants.INITIAL_COUNTDOWN_TIME_MILLIS
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PauseViewModel(
    scoreHolder: ScoreHolder,
    private val analyticsService: AnalyticsService,
    private val preferencesService: PreferencesService,
    private val gameStateHolder: GameStateHolder,
    private val soundManager: SoundManager
) : BaseViewModel(preferencesService) {

    val score: StateFlow<Int> = scoreHolder
        .getScore()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = 0
        )

    val countDownTimeMillis: StateFlow<Long> = scoreHolder.getCountDownTimeMillis()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = INITIAL_COUNTDOWN_TIME_MILLIS
        )
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesService.saveTheme(theme)
            analyticsService.trackSettingsChanged("theme", theme.name)
        }
    }

    fun resumeSession() {
        gameStateHolder.onResume()
        analyticsService.trackGameResume()
    }


    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesService.saveSoundEnabled(enabled)
            soundManager.setSoundEnabled(enabled)
            analyticsService.trackSettingsChanged("sound_enabled", enabled)
        }
    }

    fun resetSession() {
        gameStateHolder.restart()
    }
}