package hu.adamfejes.zenmotes.logic

import hu.adamfejes.zenmotes.service.SoundManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GameStateHolder(
    private val sandColorManager: SandColorManager,
    private val sandGridHolder: SandGridHolder,
    private val scoreHolder: ScoreHolder,
    private val soundManager: SoundManager
) {
    private val isPausedState = MutableStateFlow(false)
    val isPaused: Flow<Boolean> = isPausedState

    private val isDemoModeState = MutableStateFlow(false)
    val isDemoMode: Flow<Boolean> = isDemoModeState

    fun onPause() {
        scoreHolder.pauseTimer()
        sandColorManager.pause()
        sandGridHolder.sandGrid?.onPause()
        soundManager.onPause()

        isPausedState.value = true
    }

    fun onResume() {
        scoreHolder.resumeTimer()
        sandColorManager.resume()
        sandGridHolder.sandGrid?.onResume()
        soundManager.onResume()

        isPausedState.value = false
    }

    fun onFinish() {
        scoreHolder.pauseTimer()
        sandColorManager.pause()
        sandGridHolder.sandGrid?.onPause()
        soundManager.stopGameSceneSounds()

        isPausedState.value = true
    }

    fun restart() {
        sandGridHolder.sandGrid?.reset()
        scoreHolder.resetScore()
        scoreHolder.resumeTimer()
        isDemoModeState.value = false
        sandGridHolder.sandGrid?.onResume()
        sandColorManager.resume()
        soundManager.onResume()

        isPausedState.value = false
    }

    fun enableDemoMode() {
        isDemoModeState.value = true
        scoreHolder.pauseTimer()
        scoreHolder.setDemoMode(true)
        sandColorManager.resume()
        sandGridHolder.sandGrid?.onResume()
        sandGridHolder.sandGrid?.setDemoMode(true)

        isPausedState.value = false
    }

    fun disableDemoMode() {
        scoreHolder.setDemoMode(false)
        sandGridHolder.sandGrid?.setDemoMode(false)
        sandGridHolder.sandGrid?.reset()

        isDemoModeState.value = false
    }
}