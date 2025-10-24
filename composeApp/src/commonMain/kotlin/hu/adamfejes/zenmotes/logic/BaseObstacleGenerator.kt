package hu.adamfejes.zenmotes.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private const val slidingObstacleTransitTimeSeconds = 7.5f
abstract class BaseObstacleGenerator : IObstacleGenerator {
    var width: Int = 0
        set(value) {
            field = value
            slidingSpeed = value / slidingObstacleTransitTimeSeconds // pixels per second
        }
    var height: Int = 0
        set(value) {
            field = value
            nonObstacleZoneHeight = (value * 0.15f).toInt().coerceAtLeast(10)
        }

    protected var nonObstacleZoneHeight = 0
    protected var slidingSpeed = 0f

    protected var isPaused = false

    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onPause() {
        isPaused = true
    }

    override fun onResume() {
        isPaused = false
    }

    protected abstract fun shouldGenerateObstacle(frameTime: Long): Boolean
}