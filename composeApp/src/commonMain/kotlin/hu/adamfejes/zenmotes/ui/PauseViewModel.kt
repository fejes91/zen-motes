package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.logic.GameStateHolder
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.service.SoundManager
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import kotlinx.coroutines.launch

class PauseViewModel(
    gameStateHolder: GameStateHolder,
    scoreHolder: ScoreHolder,
    private val preferencesService: PreferencesService,
    soundManager: SoundManager
) : SandSimulationViewModel(
    gameStateHolder,
    scoreHolder,
    preferencesService,
    soundManager,
) {

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesService.saveTheme(theme)
        }
    }
}