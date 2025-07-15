package hu.adamfejes.zenmotes

import androidx.compose.ui.graphics.Color

class ObstacleGenerator(
    private val width: Int,
    private val height: Int,
    private val nonObstacleZoneHeight: Int,
    private val slidingObstacleTransitTimeSeconds: Float = 5.0f
) {
    private val slidingObstacleInterval = 3000L // 3 seconds between obstacles
    private val slidingSpeed = width / (slidingObstacleTransitTimeSeconds * 60f)
    private var lastSlidingObstacleTime = 0L
    
    private val slidingColors = listOf(
        Color(0xFF00BCD4), // Cyan
        Color(0xFF4CAF50), // Green
        Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFFE91E63), // Pink
        Color(0xFF2196F3)  // Blue
    )
    
    fun shouldGenerateObstacle(currentTime: Long): Boolean {
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
        val obstacleSize = listOf(8, 10, 12, 14, 16).random()
        
        return SlidingObstacle(
            x = -obstacleSize.toFloat(), // Start just off screen to the left
            y = obstacleY,
            targetX = width.toFloat() + obstacleSize, // Target is off screen to the right
            speed = slidingSpeed,
            size = obstacleSize,
            color = slidingColors.random()
        )
    }
    
    fun updateObstaclePosition(obstacle: SlidingObstacle): SlidingObstacle {
        return obstacle.copy(x = obstacle.x + slidingSpeed)
    }
    
    fun isObstacleOffScreen(obstacle: SlidingObstacle): Boolean {
        return obstacle.x > width + obstacle.size
    }
}