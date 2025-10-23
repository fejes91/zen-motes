package hu.adamfejes.zenmotes.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class BaseObstacleGenerator : IObstacleGenerator{
    protected abstract fun shouldGenerateObstacle(frameTime: Long): Boolean

    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}