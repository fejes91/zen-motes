package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.ViewModel
import hu.adamfejes.zenmotes.logic.ScoreHolder
import kotlinx.coroutines.flow.StateFlow

class SandSimulationViewModel(
    private val scoreHolder: ScoreHolder
) : ViewModel() {
    
    val score: StateFlow<Int> = scoreHolder.getScore() as StateFlow<Int>
    
    fun resetScore() {
        scoreHolder.resetScore()
    }
}