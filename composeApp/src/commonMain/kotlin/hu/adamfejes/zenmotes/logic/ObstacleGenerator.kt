package hu.adamfejes.zenmotes.logic

import androidx.compose.ui.graphics.ImageBitmap
import hu.adamfejes.zenmotes.utils.TimeUtils
import kotlin.random.Random

class ObstacleGenerator(
    private val width: Int,
    private val height: Int,
    private val nonObstacleZoneHeight: Int,
    private val slidingObstacleTransitTimeSeconds: Float
) : IObstacleGenerator {
    private val slidingObstacleInterval = 2500L // 3 seconds between obstacles
    private val slidingSpeed = width / slidingObstacleTransitTimeSeconds // pixels per second
    private var lastSlidingObstacleTime = 0L

    // Use domain-layer color types
    private val colorTypes = ColorType.entries.toTypedArray()

    private fun shouldGenerateObstacle(): Boolean {
        return TimeUtils.currentTimeMillis() - lastSlidingObstacleTime >= slidingObstacleInterval
    }

    override fun generateSlidingObstacle(frameTime: Long, images: List<ImageBitmap>): SlidingObstacle? {
        if (!shouldGenerateObstacle()) return null

        lastSlidingObstacleTime = TimeUtils.currentTimeMillis()

        // Generate random Y position avoiding non-obstacle zone
        val minY = nonObstacleZoneHeight + 6 // Add margin
        val maxY = height - 20 // Add margin from bottom

        if (minY >= maxY) return null // Not enough space to place obstacle

        val obstacleY = (minY..maxY).random()
        val bitmap = images.randomOrNull() ?: return null

        // Use bitmap dimensions directly
        val obstacleWidth = bitmap.width
        val obstacleHeight = bitmap.height

        val direction = if (Random.Default.nextBoolean()) 1 else -1

        return SlidingObstacle(
            x = if (direction == 1) -obstacleWidth.toFloat() else width.toFloat() + obstacleWidth,
            y = obstacleY,
            targetX = if (direction == 1) width.toFloat() + obstacleWidth else -obstacleWidth.toFloat(),
            speed = slidingSpeed * direction,
            width = obstacleWidth,
            height = obstacleHeight,
            colorType = colorTypes.random(),
            lastUpdateTime = frameTime,
            bitmapShape = bitmap
        )
    }


    override fun reset() {
        lastSlidingObstacleTime = 0L
    }
}