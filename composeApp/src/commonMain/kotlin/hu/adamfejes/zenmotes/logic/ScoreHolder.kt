package hu.adamfejes.zenmotes.logic

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

interface ScoreHolder {
    fun getScore(): Flow<Int>

    fun getScoreEvent(): Flow<ScoreEvent>

    suspend fun increaseScore(slidingObstacle: SlidingObstacle)

    suspend fun decreaseScore(slidingObstacle: SlidingObstacle)

    fun resetScore()
}

class ScoreHolderImpl : ScoreHolder {
    private val _score = atomic(0)
    private val _scoreFlow = MutableStateFlow(0)

    private val _scoreEventFlow = MutableSharedFlow<ScoreEvent?>()
    
    override fun getScore(): Flow<Int> = _scoreFlow.asStateFlow()

    override fun getScoreEvent(): Flow<ScoreEvent> = _scoreEventFlow.filterNotNull()

    override suspend fun increaseScore(slidingObstacle: SlidingObstacle) = withContext(Dispatchers.Default){
        _scoreEventFlow.emit(
            ScoreEvent(
                x = slidingObstacle.x.roundToInt(),
                y = slidingObstacle.y,
                score = slidingObstacle.getBallparkScore(),
                obstacleId = slidingObstacle.id
            )
        )
        val newScore = _score.addAndGet(slidingObstacle.getBallparkScore())
        _scoreFlow.value = newScore
    }
    
    override suspend fun decreaseScore(slidingObstacle: SlidingObstacle) = withContext(Dispatchers.Default) {
        _scoreEventFlow.emit(
            ScoreEvent(
                x = slidingObstacle.x.roundToInt(),
                y = slidingObstacle.y,
                score = (-slidingObstacle.getBallparkScore() / 4f).roundToInt(),
                obstacleId = slidingObstacle.id
            )
        )
        val newScore = _score.addAndGet((-slidingObstacle.getBallparkScore() /4f).roundToInt())
        _scoreFlow.value = newScore
    }
    
    override fun resetScore() {
        _score.value = 0
        _scoreFlow.value = 0
    }
}