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

    private val isDemoModeState = MutableStateFlow(false)
    val isDemoMode: Flow<Boolean> = isDemoModeState

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
        isDemoModeState.value = false
        sandGridHolder.sandGrid?.onResume()
        sandColorManager.resume()
    }

    fun enableDemoMode() {
        isDemoModeState.value = true
        isPausedState.value = false
        scoreHolder.pauseTimer()
        scoreHolder.setDemoMode(true)
        sandColorManager.resume()
        sandGridHolder.sandGrid?.onResume()
        sandGridHolder.sandGrid?.setDemoMode(true)
    }

    fun disableDemoMode() {
        isDemoModeState.value = false
        scoreHolder.setDemoMode(false)
        sandGridHolder.sandGrid?.setDemoMode(false)
        sandGridHolder.sandGrid?.reset()
    }
}