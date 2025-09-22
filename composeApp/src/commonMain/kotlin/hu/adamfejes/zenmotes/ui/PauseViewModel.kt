package hu.adamfejes.zenmotes.ui

import hu.adamfejes.zenmotes.logic.GameStateHolder
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.service.SoundManager

class PauseViewModel(
    gameStateHolder: GameStateHolder,
    scoreHolder: ScoreHolder,
    preferencesService: PreferencesService,
    soundManager: SoundManager
) : SandSimulationViewModel(
    gameStateHolder,
    scoreHolder,
    preferencesService,
    soundManager,
)