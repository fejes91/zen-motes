package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.logic.GameStateHolder
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class GameOverViewModel(
    private val scoreHolder: ScoreHolder,
    private val gameStateHolder: GameStateHolder,
    private val preferencesService: PreferencesService
) : ViewModel() {

    val score: Flow<Int> = scoreHolder.getScore()
    val appTheme: Flow<AppTheme> = preferencesService.getTheme

    val scoreComparison: StateFlow<ScoreComparison> = combine(
        scoreHolder.getScore(),
        preferencesService.getHighScore.take(1)
    ) { currentScore, savedHighScore ->
        val isNewHighScore = currentScore > (savedHighScore ?: 0)
        if (isNewHighScore) {
            // Save the new high score
            viewModelScope.launch {
                preferencesService.saveHighScore(currentScore)
            }
            ScoreComparison.NewHighScore(currentScore)
        } else {
            ScoreComparison.RegularScore(currentScore, savedHighScore)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScoreComparison.RegularScore(0, 0)
    )

    fun resetSession() {
        gameStateHolder.restart()
    }
}

sealed class ScoreComparison {
    data class NewHighScore(val score: Int) : ScoreComparison()
    data class RegularScore(val score: Int, val highScore: Int?) : ScoreComparison()
}