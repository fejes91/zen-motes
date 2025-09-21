package hu.adamfejes.zenmotes.logic

import hu.adamfejes.zenmotes.ui.Constants.INITIAL_COUNTDOWN_TIME_MILLIS
import hu.adamfejes.zenmotes.utils.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface ScoreHolder {
    fun getScore(): Flow<Int>

    fun getScoreEvent(): Flow<ScoreEvent>

    fun getCountDownTimeMillis(): StateFlow<Long>

    suspend fun increaseScore(scoreEvent: ScoreEvent)

    suspend fun decreaseScore(scoreEvent: ScoreEvent)

    fun resetScore()

    fun startTimer()

    fun pauseTimer()

    fun resumeTimer()

    fun resetTimer()
}

class ScoreHolderImpl : ScoreHolder {
    private val _score = atomic(0)
    private val _scoreFlow = MutableStateFlow(0)

    private val _scoreEventFlow = MutableSharedFlow<ScoreEvent?>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val initialCountDown = INITIAL_COUNTDOWN_TIME_MILLIS

    private val _countDownTimeMillis = MutableStateFlow(initialCountDown)
    override fun getCountDownTimeMillis(): StateFlow<Long> = _countDownTimeMillis.asStateFlow()

    private var timerJob: Job? = null
    private var startTime: Long = 0L
    private var countDownAccumulatedTime: Long = 0L
    private var isRunning: Boolean = false

    override fun getScore(): Flow<Int> = _scoreFlow.asStateFlow()

    override fun getScoreEvent(): Flow<ScoreEvent> = _scoreEventFlow.filterNotNull()

    override suspend fun increaseScore(scoreEvent: ScoreEvent) = withContext(Dispatchers.Default){
        _scoreEventFlow.emit(scoreEvent)
        val newScore = _score.addAndGet(scoreEvent.score / 10)
        _scoreFlow.value = newScore
        updateCountDownTime(scoreEvent.score.toLong())
    }

    override suspend fun decreaseScore(scoreEvent: ScoreEvent) = withContext(Dispatchers.Default) {
        _scoreEventFlow.emit(scoreEvent)
        updateCountDownTime(scoreEvent.score.toLong())
    }

    override fun resetScore() {
        _score.value = 0
        _scoreFlow.value = 0
        resetTimer()
    }

    override fun startTimer() {
        if (!isRunning) {
            startTime = TimeUtils.currentTimeMillis()
            isRunning = true
            startTimerLoop()
        }
    }

    override fun pauseTimer() {
        if (isRunning) {
            timerJob?.cancel()
            val elapsed = TimeUtils.currentTimeMillis() - startTime
            countDownAccumulatedTime += elapsed
            isRunning = false
        }
    }

    override fun resumeTimer() {
        if (!isRunning) {
            startTime = TimeUtils.currentTimeMillis()
            isRunning = true
            startTimerLoop()
        }
    }

    override fun resetTimer() {
        timerJob?.cancel()
        countDownAccumulatedTime = 0L
        startTime = TimeUtils.currentTimeMillis()
        isRunning = false
        _countDownTimeMillis.value = initialCountDown
    }

    private fun updateCountDownTime(millis: Long) {
        countDownAccumulatedTime -= millis
        if (!isRunning) {
            val remainingCountDown = (initialCountDown - countDownAccumulatedTime).coerceAtLeast(0L)
            _countDownTimeMillis.value = remainingCountDown
        }
    }

    private fun startTimerLoop() {
        timerJob = scope.launch {
            while (isRunning) {
                val elapsed = TimeUtils.currentTimeMillis() - startTime
                val currentCountDownElapsed = countDownAccumulatedTime + elapsed
                val remainingCountDown = (initialCountDown - currentCountDownElapsed).coerceAtLeast(0L)

                _countDownTimeMillis.value = remainingCountDown
                delay(100)
            }
        }
    }
}