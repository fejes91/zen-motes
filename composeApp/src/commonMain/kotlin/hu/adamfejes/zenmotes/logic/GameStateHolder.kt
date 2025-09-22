package hu.adamfejes.zenmotes.logic

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GameStateHolder(
    private val sandColorManager: SandColorManager,
    private val sandGridHolder: SandGridHolder,
    private val scoreHolder: ScoreHolder
) {
    private val isPausedState = MutableStateFlow(false)
    val isPaused: Flow<Boolean> = isPausedState

    fun onPause() {
        isPausedState.value = true

        scoreHolder.pauseTimer()
        sandColorManager.pause()
        sandGridHolder.sandGrid?.onPause()
    }

    fun onResume() {
        isPausedState.value = false

        scoreHolder.resumeTimer()
        sandColorManager.resume()
        sandGridHolder.sandGrid?.onResume()
    }

    fun restart() {
        sandGridHolder.sandGrid?.reset()
        scoreHolder.resetScore()
        scoreHolder.resumeTimer()
        isPausedState.value = false
        sandGridHolder.sandGrid?.onResume()
    }
}