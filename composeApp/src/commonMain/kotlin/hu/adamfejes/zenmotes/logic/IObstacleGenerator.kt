package hu.adamfejes.zenmotes.logic

import androidx.compose.ui.graphics.ImageBitmap

interface IObstacleGenerator {
    fun generateSlidingObstacle(frameTime: Long, images: List<ImageBitmap>): SlidingObstacle?
    fun reset()
}