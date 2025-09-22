package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.ViewModel
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.service.SoundManager

class PauseViewModel(
    scoreHolder: ScoreHolder, preferencesService: PreferencesService, soundManager: SoundManager
) : SandSimulationViewModel(
    scoreHolder,
    preferencesService,
    soundManager,
) {

}