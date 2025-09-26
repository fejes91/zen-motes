package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.logic.GameStateHolder
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.service.AnalyticsService
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.service.SoundManager
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import kotlinx.coroutines.launch

class PauseViewModel(
    gameStateHolder: GameStateHolder,
    scoreHolder: ScoreHolder,
    private val analyticsService: AnalyticsService,
    private val preferencesService: PreferencesService,
    soundManager: SoundManager
) : SandSimulationViewModel( // todo do we need to inherit from SandSimulationViewModel?
    gameStateHolder,
    scoreHolder,
    preferencesService,
    soundManager,
    analyticsService
) {

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesService.saveTheme(theme)
            analyticsService.trackSettingsChanged("theme", theme.name)
        }
    }
}