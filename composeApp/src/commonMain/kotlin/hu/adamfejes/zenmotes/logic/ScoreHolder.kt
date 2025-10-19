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

    fun getCountDownTimeMillis(): StateFlow<Long>

    suspend fun updateScore(score: Int)

    fun resetScore()

    fun pauseTimer()

    fun resumeTimer()

    fun resetTimer()

    fun setDemoMode(isDemoMode: Boolean)
}

class ScoreHolderImpl : ScoreHolder {
    private val _score = atomic(0)
    private val _scoreFlow = MutableStateFlow(0)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val initialCountDown = INITIAL_COUNTDOWN_TIME_MILLIS

    private val _countDownTimeMillis = MutableStateFlow(initialCountDown)
    override fun getCountDownTimeMillis(): StateFlow<Long> = _countDownTimeMillis.asStateFlow()

    private var timerJob: Job? = null
    private var startTime: Long = 0L
    private var countDownAccumulatedTime: Long = 0L
    private var isRunning: Boolean = false
    private var isDemoMode: Boolean = false

    override fun getScore(): Flow<Int> = _scoreFlow.asStateFlow()

    override suspend fun updateScore(score: Int) = withContext(Dispatchers.Default) {
        if (score > 0) {
            val newScore = _score.addAndGet(score / 10)
            _scoreFlow.value = newScore
        }
        updateCountDownTime(score.toLong())
    }

    override fun resetScore() {
        _score.value = 0
        _scoreFlow.value = 0
        resetTimer()
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
        // Don't update countdown in demo mode
        if (isDemoMode) return

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
                val remainingCountDown =
                    (initialCountDown - currentCountDownElapsed).coerceAtLeast(0L)

                _countDownTimeMillis.value = remainingCountDown
                delay(100)
            }
        }
    }

    override fun setDemoMode(isDemoMode: Boolean) {
        this.isDemoMode = isDemoMode
    }
}