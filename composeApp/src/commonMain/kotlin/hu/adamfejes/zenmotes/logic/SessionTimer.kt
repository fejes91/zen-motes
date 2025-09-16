package hu.adamfejes.zenmotes.logic

import hu.adamfejes.zenmotes.utils.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionTimer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _sessionTimeMillis = MutableStateFlow(0L)
    val sessionTimeMillis: StateFlow<Long> = _sessionTimeMillis.asStateFlow()

    private var timerJob: Job? = null
    private var startTime: Long = 0L
    private var accumulatedTime: Long = 0L
    private var isRunning: Boolean = false

    fun start() {
        if (!isRunning) {
            startTime = TimeUtils.currentTimeMillis()
            isRunning = true
            startTimer()
        }
    }

    fun pause() {
        if (isRunning) {
            timerJob?.cancel()
            accumulatedTime += TimeUtils.currentTimeMillis() - startTime
            isRunning = false
        }
    }

    fun resume() {
        if (!isRunning) {
            startTime = TimeUtils.currentTimeMillis()
            isRunning = true
            startTimer()
        }
    }

    fun reset() {
        timerJob?.cancel()
        accumulatedTime = 0L
        startTime = TimeUtils.currentTimeMillis()
        isRunning = false
        _sessionTimeMillis.value = 0L
    }

    private fun startTimer() {
        timerJob = scope.launch {
            while (isRunning) {
                val currentTime = if (isRunning) {
                    accumulatedTime + (TimeUtils.currentTimeMillis() - startTime)
                } else {
                    accumulatedTime
                }
                _sessionTimeMillis.value = currentTime
                delay(100)
            }
        }
    }
}