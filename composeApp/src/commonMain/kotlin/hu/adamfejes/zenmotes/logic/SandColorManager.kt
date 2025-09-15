package hu.adamfejes.zenmotes.logic

import hu.adamfejes.zenmotes.ui.Constants.COLOR_CHANGE_ANIMATION_DURATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SandColorManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _currentSandColor = MutableStateFlow(ColorType.OBSTACLE_COLOR_1)
    val currentSandColor: StateFlow<ColorType> = _currentSandColor.asStateFlow()

    private val _nextSandColor = MutableStateFlow<ColorType?>(null)
    val nextSandColor: StateFlow<ColorType?> = _nextSandColor.asStateFlow()

    private var isPaused = false

    init {
        startColorChangeLoop()
    }

    private fun startColorChangeLoop() {
        scope.launch {
            while (true) {
                val delayTime = (5000..10000).random().toLong()

                delay(delayTime - COLOR_CHANGE_ANIMATION_DURATION)

                if (!isPaused) {
                    _nextSandColor.value = ColorType.entries.random()
                }

                delay(COLOR_CHANGE_ANIMATION_DURATION)

                if (!isPaused) {
                    _nextSandColor.value?.let { nextColor ->
                        _currentSandColor.value = nextColor
                        _nextSandColor.value = null
                    }
                }
            }
        }
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }
}