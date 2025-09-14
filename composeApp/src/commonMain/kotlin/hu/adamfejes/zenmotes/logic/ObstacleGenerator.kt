package hu.adamfejes.zenmotes.logic

import androidx.compose.ui.graphics.ImageBitmap
import hu.adamfejes.zenmotes.utils.TimeUtils
import kotlin.random.Random

class ObstacleGenerator(
    private val width: Int,
    private val height: Int,
    private val nonObstacleZoneHeight: Int,
    slidingObstacleTransitTimeSeconds: Float
) : IObstacleGenerator {
    private val slidingObstacleInterval = 2500L
    private val slidingSpeed = width / slidingObstacleTransitTimeSeconds // pixels per second
    private var lastSlidingObstacleTime = 0L

    // Use domain-layer color types
    private val colorTypes = ColorType.entries.toTypedArray()

    private fun shouldGenerateObstacle(): Boolean {
        return TimeUtils.currentTimeMillis() - lastSlidingObstacleTime >= slidingObstacleInterval
    }

    override fun generateSlidingObstacle(frameTime: Long, obstacleTypes: List<SlidingObstacleType>): SlidingObstacle? {
        if (!shouldGenerateObstacle()) return null
        if (obstacleTypes.isEmpty()) return null

        lastSlidingObstacleTime = TimeUtils.currentTimeMillis()
        val obstacleType = obstacleTypes.random()

        // Generate random Y position avoiding non-obstacle zone
        val minY = nonObstacleZoneHeight + 6 // Add margin
        val maxY = height - obstacleType.getHeight()

        if (minY >= maxY) return null // Not enough space to place obstacle

        val obstacleY = (minY..maxY).random()
            .apply {
                if (this > height / 2 && Random.nextInt(10) < 8) {
                    (minY..height / 2).random() // Bias towards upper half
                }
            }

        val direction = if (Random.Default.nextBoolean()) 1 else -1

        val obstacleWidth = obstacleType.getWidth()
        val obstacleHeight = obstacleType.getHeight()

        return SlidingObstacle(
            x = if (direction == 1) -obstacleWidth.toFloat() else width.toFloat() + obstacleWidth,
            y = obstacleY,
            targetX = if (direction == 1) width.toFloat() + obstacleWidth else -obstacleWidth.toFloat(),
            speed = slidingSpeed * direction,
            width = obstacleWidth,
            height = obstacleHeight,
            colorType = colorTypes.random(),
            type = obstacleType,
            lastUpdateTime = frameTime
        )
    }


    override fun reset() {
        lastSlidingObstacleTime = 0L
    }
}