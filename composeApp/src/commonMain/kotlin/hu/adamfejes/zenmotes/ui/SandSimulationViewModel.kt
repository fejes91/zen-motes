package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.logic.ScoreEvent
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.logic.SlidingObstacle
import hu.adamfejes.zenmotes.logic.getBallparkScore
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.service.SoundManager
import hu.adamfejes.zenmotes.service.SoundSample
import hu.adamfejes.zenmotes.ui.Constants.INITIAL_COUNTDOWN_TIME_MILLIS
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class SandSimulationViewModel(
    private val scoreHolder: ScoreHolder,
    private val preferencesService: PreferencesService,
    private val soundManager: SoundManager
) : ViewModel() {

    var soundJob: Job? = null
    val score: StateFlow<Int> = scoreHolder
        .getScore()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = 0
        )

    val scoreEvent: StateFlow<ScoreEvent?> = scoreHolder.getScoreEvent().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null
    )

    val appTheme: StateFlow<AppTheme?> = preferencesService.getTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    val soundEnabled: StateFlow<Boolean> = preferencesService.getSoundEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = true
        )

    val countDownTimeMillis: StateFlow<Long> = scoreHolder.getCountDownTimeMillis()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = INITIAL_COUNTDOWN_TIME_MILLIS
        )

    init {
        // Sync SoundManager with stored sound preference
        soundEnabled
            .onEach { enabled ->
                soundManager.setSoundEnabled(enabled)
            }
            .launchIn(viewModelScope)
    }

    fun increaseScore(slidingObstacle: SlidingObstacle, isBonus: Boolean = false) {
        viewModelScope.launch {
            scoreHolder.increaseScore(
                ScoreEvent(
                    x = slidingObstacle.x.roundToInt(),
                    y = slidingObstacle.y,
                    score = slidingObstacle.getBallparkScore(),
                    obstacleId = slidingObstacle.id,
                    isBonus = isBonus
                )
            )
        }
    }

    fun decreaseScore(slidingObstacle: SlidingObstacle) {
        viewModelScope.launch {
            // do not decrease score
            scoreHolder.decreaseScore(
                ScoreEvent(
                    x = slidingObstacle.x.roundToInt(),
                    y = slidingObstacle.y,
                    score = (-slidingObstacle.getBallparkScore() / 2f).roundToInt(),
                    obstacleId = slidingObstacle.id
                )
            )
        }
    }

    fun resetSession() {
        scoreHolder.resetScore()
    }

    fun startSession() {
        scoreHolder.startTimer()
    }

    fun pauseSession() {
        scoreHolder.pauseTimer()
    }

    fun resumeSession() {
        scoreHolder.resumeTimer()
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesService.saveTheme(theme)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesService.saveSoundEnabled(enabled)
            soundManager.setSoundEnabled(enabled)
        }
    }

    fun playSound(score: Int) {
        if (soundJob?.isActive == true) {
            return
        }

        soundJob = viewModelScope.launch {
            soundManager.play(if (score < 0) SoundSample.NEGATIVE else SoundSample.POSITIVE)
        }
    }

    fun toggleAddingSand(isAdding: Boolean) {
        viewModelScope.launch {
//            if(isAdding) {
//                soundManager.play(SoundSample.SAND_BEGIN, loop = false)
//                soundManager.play(SoundSample.SAND_MIDDLE, loop = true)
//            } else {
//                soundManager.stopAll()
//            }
        }
    }
}