package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.logic.ScoreEvent
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.logic.SlidingObstacle
import hu.adamfejes.zenmotes.logic.getBallparkScore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class SandSimulationViewModel(
    private val scoreHolder: ScoreHolder
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
}