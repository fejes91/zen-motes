package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.logic.ScoreEvent
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.logic.SessionTimer
import hu.adamfejes.zenmotes.logic.SlidingObstacle
import hu.adamfejes.zenmotes.logic.getBallparkScore
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.service.SoundManager
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class SandSimulationViewModel(
    private val scoreHolder: ScoreHolder,
    private val preferencesService: PreferencesService,
    private val soundManager: SoundManager,
    private val sessionTimer: SessionTimer
) : ViewModel() {

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

    val sessionTimeMillis: StateFlow<Long> = sessionTimer.sessionTimeMillis
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = 0L
        )

    fun increaseScore(slidingObstacle: SlidingObstacle, isBonus: Boolean = false) {
        viewModelScope.launch {
            val baseScore = slidingObstacle.getBallparkScore()
            val finalScore = if (isBonus) baseScore * 2 else baseScore
            scoreHolder.increaseScore(
                ScoreEvent(
                    x = slidingObstacle.x.roundToInt(),
                    y = slidingObstacle.y,
                    score = finalScore,
                    obstacleId = slidingObstacle.id,
                    isBonus = isBonus
                )
            )
        }
    }

    fun decreaseScore(slidingObstacle: SlidingObstacle) {
        viewModelScope.launch {
            scoreHolder.decreaseScore(
                ScoreEvent(
                    x = slidingObstacle.x.roundToInt(),
                    y = slidingObstacle.y,
                    score = -slidingObstacle.getBallparkScore(),
                    obstacleId = slidingObstacle.id
                )
            )
        }
    }

    fun resetSession() {
        scoreHolder.resetScore()
        sessionTimer.reset()
    }

    fun startSession() {
        sessionTimer.start()
    }

    fun pauseSession() {
        sessionTimer.pause()
    }

    fun resumeSession() {
        sessionTimer.resume()
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesService.saveTheme(theme)
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