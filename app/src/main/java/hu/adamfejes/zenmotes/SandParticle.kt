package hu.adamfejes.zenmotes

import androidx.compose.ui.graphics.Color

data class SandParticle(
    val color: Color = Color.Yellow,
    val isActive: Boolean = false,
    val velocityY: Float = 0f,
    val lastUpdateTime: Long = 0L,
    val noiseVariation: Float = 1f, // 1f = normal, < 1f = darker
    val isSettled: Boolean = false // true if particle won't move anymore
)

enum class CellType {
    EMPTY,
    SAND,
    OBSTACLE,
    ROTATING_OBSTACLE,
    DESTROYABLE_OBSTACLE
}

data class DestroyableObstacle(
    val weightThreshold: Int = 5, // Maximum sand height before destruction
    val x: Int,
    val y: Int,
    val size: Int = 18, // Size of the obstacle (width and height)
    val color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFFD2691E) // Color for rendering
)

data class Cell(
    val type: CellType = CellType.EMPTY,
    val particle: SandParticle? = null,
    val destroyableObstacle: DestroyableObstacle? = null
)