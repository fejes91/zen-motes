package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.logic.GameStateHolder
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.service.AnalyticsService
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.service.SoundManager
import hu.adamfejes.zenmotes.service.SoundSample
import hu.adamfejes.zenmotes.ui.Constants.FAST_TICKING_THRESHOLD_MILLIS
import hu.adamfejes.zenmotes.ui.Constants.INITIAL_COUNTDOWN_TIME_MILLIS
import hu.adamfejes.zenmotes.ui.Constants.SLOW_TICKING_THRESHOLD_MILLIS
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SandSimulationViewModel(
    private val gameStateHolder: GameStateHolder,
    private val scoreHolder: ScoreHolder,
    private val preferencesService: PreferencesService,
    private val soundManager: SoundManager,
    private val analyticsService: AnalyticsService
) : BaseViewModel(preferencesService) {

    var soundJob: Job? = null
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

    val isPaused: StateFlow<Boolean> = gameStateHolder.isPaused
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    val isDemoMode: StateFlow<Boolean> = gameStateHolder.isDemoMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    fun initialize() {
        // Sync SoundManager with stored sound preference
        soundEnabled.combine(isDemoMode) { enabled, demoMode ->
            soundManager.setSoundEnabled(enabled && !demoMode)
        }.launchIn(viewModelScope)

        countDownTimeMillis
            .map { it < FAST_TICKING_THRESHOLD_MILLIS && it > 0L }
            .distinctUntilChanged()
            .onEach { triggerFastTicking ->
                if (triggerFastTicking) {
                    soundManager.playAsync(SoundSample.CLOCK_FAST, loop = true)
                } else {
                    soundManager.stop(SoundSample.CLOCK_FAST)
                }
            }.launchIn(viewModelScope)

        countDownTimeMillis
            .map { it < SLOW_TICKING_THRESHOLD_MILLIS && it >= FAST_TICKING_THRESHOLD_MILLIS }
            .distinctUntilChanged()
            .onEach { triggerSlowTicking ->
                if (triggerSlowTicking) {
                    soundManager.playAsync(SoundSample.CLOCK_SLOW, loop = true)
                } else {
                    soundManager.stop(SoundSample.CLOCK_SLOW)
                }
            }.launchIn(viewModelScope)
    }

    fun updateScore(score: Int) {
        viewModelScope.launch {
            scoreHolder.updateScore(score)
        }
    }

    fun pauseSession() {
        gameStateHolder.onPause()
        analyticsService.trackGamePause()
    }

    fun playScoreSound(score: Int) {
        if (soundJob?.isActive == true) {
            return
        }

        soundJob = viewModelScope.launch {
            soundManager.play(if (score < 0) SoundSample.NEGATIVE else SoundSample.POSITIVE)
        }
    }
}