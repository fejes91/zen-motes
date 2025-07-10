package hu.adamfejes.zenmotes

import androidx.compose.ui.graphics.Color

data class SandParticle(
    val color: Color = Color.Yellow,
    val isActive: Boolean = false,
    val velocityY: Float = 0f,
    val lastUpdateTime: Long = 0L,
    val noiseVariation: Float = 1f // 1f = normal, < 1f = darker
)

enum class CellType {
    EMPTY,
    SAND,
    OBSTACLE,
    ROTATING_OBSTACLE
}

data class Cell(
    val type: CellType = CellType.EMPTY,
    val particle: SandParticle? = null
)