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
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class SandSimulationViewModel(
    private val scoreHolder: ScoreHolder,
    private val preferencesService: PreferencesService,
    private val soundManager: SoundManager
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

    fun increaseScore(slidingObstacle: SlidingObstacle) {
        viewModelScope.launch {
            scoreHolder.increaseScore(
                ScoreEvent(
                    x = slidingObstacle.x.roundToInt(),
                    y = slidingObstacle.y,
                    score = slidingObstacle.getBallparkScore(),
                    obstacleId = slidingObstacle.id
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
                    score = (-slidingObstacle.getBallparkScore() / 4f).roundToInt(),
                    obstacleId = slidingObstacle.id
                )
            )
        }
    }

    fun resetScore() {
        scoreHolder.resetScore()
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