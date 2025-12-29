package hu.adamfejes.zenmotes.logic

class ObstacleAnimator(
    private val width: Int
) {
    fun updateObstaclePosition(obstacle: SlidingObstacle, frameTime: Long): SlidingObstacle {
        // Calculate time delta in seconds
        val deltaTimeMs = frameTime - obstacle.lastUpdateTime
        val deltaTimeSeconds = deltaTimeMs / 1000f

        // Calculate movement based on speed (pixels per second) and time delta
        val movement = obstacle.speed * deltaTimeSeconds

        // Keep float position for smooth movement, rounding only happens during grid placement
        return obstacle.copy(
            x = obstacle.x + movement,
            lastUpdateTime = frameTime
        )
    }

    fun isObstacleOffScreen(obstacle: SlidingObstacle): Boolean {
        return obstacle.x > width + obstacle.width || obstacle.x < -obstacle.width
    }
}