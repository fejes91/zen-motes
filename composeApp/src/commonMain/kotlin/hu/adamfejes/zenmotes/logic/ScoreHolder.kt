package hu.adamfejes.zenmotes.logic

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.atomicfu.atomic

interface ScoreHolder {
    fun getScore(): Flow<Int>

    fun increaseScore(score: Int)

    fun decreaseScore(score: Int)

    fun resetScore()
}

class ScoreHolderImpl : ScoreHolder {
    private val _score = atomic(0)
    private val _scoreFlow = MutableStateFlow(0)
    
    override fun getScore(): Flow<Int> = _scoreFlow.asStateFlow()
    
    override fun increaseScore(score: Int) {
        val newScore = _score.addAndGet(score)
        _scoreFlow.value = newScore
    }
    
    override fun decreaseScore(score: Int) {
        val newScore = _score.addAndGet(-score)
        _scoreFlow.value = newScore
    }
    
    override fun resetScore() {
        _score.value = 0
        _scoreFlow.value = 0
    }
}