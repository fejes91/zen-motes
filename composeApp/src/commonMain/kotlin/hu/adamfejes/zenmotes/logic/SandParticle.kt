package hu.adamfejes.zenmotes.logic

import androidx.compose.ui.graphics.ImageBitmap
import hu.adamfejes.zenmotes.utils.UuidGenerator

enum class ColorType {
    OBSTACLE_COLOR_1,
    OBSTACLE_COLOR_2,
    OBSTACLE_COLOR_3,
     OBSTACLE_COLOR_4,
    // OBSTACLE_COLOR_5,
    // OBSTACLE_COLOR_6
}

sealed class SlidingObstacleType(
    val imageBitmap: ImageBitmap,
    val value: Int
) {
    class Small(imageBitmap: ImageBitmap) : SlidingObstacleType(imageBitmap, 100)
    class Big(imageBitmap: ImageBitmap) : SlidingObstacleType(imageBitmap, 1000)
}

data class SandParticle(
    val colorType: ColorType, // Domain color type (reusing obstacle colors)
    val isActive: Boolean = false,
    val velocityY: Float = 0f,
    val lastUpdateTime: Long = 0L,
    val noiseVariation: Float = 1f, // 1f = normal, < 1f = darker
    val isSettled: Boolean = false, // true if particle won't move anymore
    val obstacleId: String? = null, // ID of the obstacle this particle is settling on
    val unsettlingUntil: Long = 0L // Timestamp until when particle cannot settle (for post-destruction falling)
)

data class MovingParticle(
    val x: Int,
    val y: Int,
    val particle: SandParticle
)

data class ParticlePosition(
    val x: Int,
    val y: Int,
    val obstacleId: String? = null
)

enum class CellType {
    EMPTY,
    SAND,
    OBSTACLE,
    SLIDING_OBSTACLE
}


data class SlidingObstacle(
    val x: Float, // Current x position (can be fractional for smooth movement)
    val y: Int, // Y position (fixed during slide)
    val targetX: Float, // Target x position to slide to
    val speed: Float, // Pixels per second
    val width: Int, // Width of the obstacle
    val height: Int, // Height of the obstacle
    val colorType: ColorType, // Domain color type
    val type: SlidingObstacleType, // Obstacle type with bitmap and value
    val id: String = UuidGenerator.randomUUID(), // Unique identifier
    val lastUpdateTime: Long = 0L // Last time this obstacle was updated (for time-based movement)
)

data class Cell(
    val type: CellType = CellType.EMPTY,
    val particle: SandParticle? = null,
    val slidingObstacles: List<SlidingObstacle> = emptyList()
)

data class PerformanceData(
    val updateTime: Long,
    val avgUpdateTime: Long,
    val movingParticles: Int,
    val settledParticles: Int,
    val obstacles: Int
)

fun SlidingObstacle.getArea() = width * height

fun SlidingObstacle.getBallparkScore(): Int {
    return type.value
}

fun SlidingObstacleType.getWidth(): Int {
    return imageBitmap.width
}

fun SlidingObstacleType.getHeight(): Int {
    return imageBitmap.height
}