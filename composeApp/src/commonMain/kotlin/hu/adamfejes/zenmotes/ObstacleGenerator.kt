package hu.adamfejes.zenmotes

import kotlin.random.Random

class ObstacleGenerator(
    private val width: Int,
    private val height: Int,
    private val nonObstacleZoneHeight: Int,
    private val slidingObstacleTransitTimeSeconds: Float
) {
    private val slidingObstacleInterval = 500L // 3 seconds between obstacles
    private val slidingSpeed = width / slidingObstacleTransitTimeSeconds // pixels per second
    private var lastSlidingObstacleTime = 0L
    
    // Use domain-layer color types
    private val colorTypes = ColorType.entries.toTypedArray()
    
    private fun shouldGenerateObstacle(currentTime: Long): Boolean {
        return currentTime - lastSlidingObstacleTime >= slidingObstacleInterval
    }
    
    fun generateSlidingObstacle(currentTime: Long): SlidingObstacle? {
        if (!shouldGenerateObstacle(currentTime)) return null
        
        lastSlidingObstacleTime = currentTime
        
        // Generate random Y position avoiding non-obstacle zone
        val minY = nonObstacleZoneHeight + 6 // Add margin
        val maxY = height - 20 // Add margin from bottom
        
        if (minY >= maxY) return null // Not enough space to place obstacle
        
        val obstacleY = (minY..maxY).random()
        val obstacleSize = listOf(14, 16, 20, 26).random()
        
        val direction = if (Random.nextBoolean()) 1 else -1
        
        return SlidingObstacle(
            x = if (direction == 1) -obstacleSize.toFloat() else width.toFloat() + obstacleSize,
            y = obstacleY,
            targetX = if (direction == 1) width.toFloat() + obstacleSize else -obstacleSize.toFloat(),
            speed = slidingSpeed * direction,
            size = obstacleSize,
            colorType = colorTypes.random(),
            lastUpdateTime = currentTime
        )
    }
    
    fun updateObstaclePosition(obstacle: SlidingObstacle, currentTime: Long): SlidingObstacle {
        // Calculate time delta in seconds
        val deltaTimeMs = currentTime - obstacle.lastUpdateTime
        val deltaTimeSeconds = deltaTimeMs / 1000f
        
        // Calculate movement based on speed (pixels per second) and time delta
        val movement = obstacle.speed * deltaTimeSeconds
        
        // Keep float position for smooth movement, rounding only happens during grid placement
        return obstacle.copy(
            x = obstacle.x + movement,
            lastUpdateTime = currentTime
        )
    }
    
    fun isObstacleOffScreen(obstacle: SlidingObstacle): Boolean {
        return obstacle.x > width + obstacle.size || obstacle.x < -obstacle.size
    }
    
    fun reset() {
        lastSlidingObstacleTime = 0L
    }
}