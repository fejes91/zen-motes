package hu.adamfejes.zenmotes

enum class ObstacleColorType {
    OBSTACLE_COLOR_1,
    OBSTACLE_COLOR_2,
    OBSTACLE_COLOR_3,
    OBSTACLE_COLOR_4,
    OBSTACLE_COLOR_5,
    OBSTACLE_COLOR_6
}

data class SandParticle(
    val colorType: ObstacleColorType, // Domain color type (reusing obstacle colors)
    val isActive: Boolean = false,
    val velocityY: Float = 0f,
    val lastUpdateTime: Long = 0L,
    val noiseVariation: Float = 1f, // 1f = normal, < 1f = darker
    val isSettled: Boolean = false, // true if particle won't move anymore
    val obstacleId: String? = null, // ID of the obstacle this particle is settling on
    val used: Boolean = false // true if particle was from destroyed obstacle - never settles again
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
    val size: Int = 12, // Size of the obstacle (width and height)
    val colorType: ObstacleColorType, // Domain color type
    val id: String = java.util.UUID.randomUUID().toString(), // Unique identifier
    val lastUpdateTime: Long = 0L // Last time this obstacle was updated (for time-based movement)
)

data class Cell(
    val type: CellType = CellType.EMPTY,
    val particle: SandParticle? = null,
    val slidingObstacle: SlidingObstacle? = null
)

data class PerformanceData(
    val updateTime: Long,
    val avgUpdateTime: Long,
    val movingParticles: Int,
    val settledParticles: Int,
    val obstacles: Int
)