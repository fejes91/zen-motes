package hu.adamfejes.zenmotes.ui

import hu.adamfejes.zenmotes.logic.SandGridHolder
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.service.SoundManager

class PauseViewModel(
    sandGridHolder: SandGridHolder,
    scoreHolder: ScoreHolder,
    preferencesService: PreferencesService,
    soundManager: SoundManager
) : SandSimulationViewModel(
    sandGridHolder,
    scoreHolder,
    preferencesService,
    soundManager,
) {

}