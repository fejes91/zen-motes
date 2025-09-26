package hu.adamfejes.zenmotes.logic

import androidx.compose.ui.graphics.ImageBitmap
import hu.adamfejes.zenmotes.utils.TimeUtils
import hu.adamfejes.zenmotes.utils.UuidGenerator

class ListBasedObstacleGenerator(
    private val width: Int,
    private val height: Int,
) : IObstacleGenerator {
    val slidingObstacleTransitTimeSeconds = 7.5f
    val timeBetweenObstaclesMs = 2000L // 2 seconds between obstacles
    private val slidingSpeed = width / slidingObstacleTransitTimeSeconds // pixels per second
    private var lastObstacleTime = 0L
    private var currentObstacleIndex = 0

    data class ObstacleDefinition(
        val id: String = UuidGenerator.randomUUID(),
        val y: Int,
        val direction: Int, // 1 for left to right, -1 for right to left
        val colorType: ColorType
    )

    private fun getObstacleList(): List<ObstacleDefinition> {
        return listOf(
            ObstacleDefinition(
                id = "red",
                y = height / 3,
                direction = 1, // left to right
                colorType = ColorType.OBSTACLE_COLOR_1,
            ),
            ObstacleDefinition(
                id = "blue",
                y = height / 3 - 20,
                direction = -1, // right to left
                colorType = ColorType.OBSTACLE_COLOR_2,
            )
        )
    }

    private fun shouldGenerateObstacle(): Boolean {
        return TimeUtils.currentTimeMillis() - lastObstacleTime >= timeBetweenObstaclesMs
    }

    override fun generateSlidingObstacle(
        frameTime: Long,
        obstacleTypes: List<SlidingObstacleType>
    ): SlidingObstacle? {
        val obstacleList = getObstacleList()
        if (!shouldGenerateObstacle()) return null
        if (obstacleList.isEmpty()) return null
        if (obstacleTypes.isEmpty()) return null
        if (currentObstacleIndex >= obstacleList.size) return null // Done with all obstacles

        lastObstacleTime = TimeUtils.currentTimeMillis()
        val obstacleDefinition = obstacleList[currentObstacleIndex]
        currentObstacleIndex++

        val direction = obstacleDefinition.direction
        val obstacleType = obstacleTypes.random()
        
        val obstacleWidth = obstacleType.getWidth()
        val obstacleHeight = obstacleType.getHeight()

        return SlidingObstacle(
            id = obstacleDefinition.id,
            x = if (direction == 1) -obstacleWidth.toFloat() else width.toFloat() + obstacleWidth,
            y = obstacleDefinition.y,
            targetX = if (direction == 1) width.toFloat() + obstacleWidth else -obstacleWidth.toFloat(),
            speed = slidingSpeed * direction,
            width = obstacleWidth,
            height = obstacleHeight,
            colorType = obstacleDefinition.colorType,
            type = obstacleType,
            lastUpdateTime = frameTime
        )
    }

    override fun reset() {
        lastObstacleTime = 0L
        currentObstacleIndex = 0
    }

    override fun onPause() {
        // Nothing specific needed for pause
    }

    override fun onResume() {
        // Reset timing to restart obstacle generation immediately
        lastObstacleTime = 0L
    }

    override fun setDemoMode(isDemoMode: Boolean) {
        // This generator doesn't need special demo mode behavior
        // Could be extended if needed for different demo content
    }
}