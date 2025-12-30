package hu.adamfejes.zenmotes.logic

import androidx.compose.ui.graphics.ImageBitmap
import hu.adamfejes.zenmotes.service.SoundManager
import hu.adamfejes.zenmotes.ui.SandGrid

class SandGridHolder {
    var sandGrid: SandGrid? = null

    fun initializeGridIfNeeded(
        soundManager: SoundManager,
        dimensions: Pair<Int, Int>,
        images: List<ImageBitmap>,
        sandColorManager: SandColorManager
    ) {
        val (width, height) = dimensions
        val obstacleTypes = createObstacleTypes(images)

        sandGrid = if (sandGrid == null || sandGrid?.getWidth() != width || sandGrid?.getHeight() != height) {
            val newGrid = SandGrid(width, height, soundManager, 1000, sandColorManager)
            newGrid.setObstacleTypes(obstacleTypes)
            newGrid
        } else {
            // Update obstacle types if images changed
            sandGrid?.setObstacleTypes(obstacleTypes)
            sandGrid
        }
    }

    private fun createObstacleTypes(images: List<ImageBitmap>): List<SlidingObstacleType> {
        return listOf(
            SlidingObstacleType.Small(images[0]), // tower
            SlidingObstacleType.Big(images[1])    // wider_tower
        )
    }
}