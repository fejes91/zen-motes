package hu.adamfejes.zenmotes.ui

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.logic.ScoreEvent
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.logic.SlidingObstacle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SandSimulationViewModel(
    private val scoreHolder: ScoreHolder
) : ViewModel() {
    
    val score: StateFlow<Int> = scoreHolder.getScore() as StateFlow<Int>

    val scoreEvent: StateFlow<ScoreEvent?> = scoreHolder.getScoreEvent().stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun increaseScore(slidingObstacle: SlidingObstacle) {
        viewModelScope.launch {
            scoreHolder.increaseScore(slidingObstacle)
        }
    }

    fun decreaseScore(slidingObstacle: SlidingObstacle) {
        viewModelScope.launch {
            scoreHolder.decreaseScore(slidingObstacle)
        }
    }
    
    fun resetScore() {
        scoreHolder.resetScore()
    }
}