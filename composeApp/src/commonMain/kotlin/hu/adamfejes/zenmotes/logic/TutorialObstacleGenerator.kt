package hu.adamfejes.zenmotes.logic

import kotlinx.coroutines.launch
import kotlin.random.Random

class TutorialObstacleGenerator(
    private val tutorialManager: TutorialManager,
    private val sandColorManager: SandColorManager
) : BaseObstacleGenerator() {
    private var lastSlidingObstacleTime = 0L
    private var currentTutorialStep: TutorialStep? = null

    init {
        scope.launch {
            tutorialManager.currentStepFlow
                .collect {
                    currentTutorialStep = it
                }
        }
    }

    override fun generateSlidingObstacle(
        frameTime: Long,
        obstacleTypes: List<SlidingObstacleType>
    ): SlidingObstacle? {
        if (!shouldGenerateObstacle(frameTime)) return null
        if (obstacleTypes.isEmpty()) return null

        lastSlidingObstacleTime = frameTime
        val obstacleType = obstacleTypes.maxBy { it.width }

        // Generate random Y position avoiding non-obstacle zone
        val minY = nonObstacleZoneHeight + 6 // Add margin
        val maxY = (height / 2f).toInt()

        if (minY >= maxY) return null // Not enough space to place obstacle

        val obstacleY = (minY..maxY).random()
            .apply {
                if (this > height / 2 && Random.nextInt(10) < 8) {
                    (minY..height / 2).random() // Bias towards upper half
                }
            }

        val direction = 1

        val obstacleWidth = obstacleType.getWidth()
        val obstacleHeight = obstacleType.getHeight()

        // Determine color: use current sand color if flag is set, otherwise random with bias
        val obstacleColor = if (currentTutorialStep == TutorialStep.SAME_COLOR_DESTRUCTION) {
            sandColorManager.currentSandColor.value
        } else {
            sandColorManager.currentSandColor.value // todo handle other tutorial steps
        }

        return SlidingObstacle(
            x = if (direction == 1) -obstacleWidth.toFloat() else width.toFloat() + obstacleWidth,
            y = obstacleY,
            targetX = if (direction == 1) width.toFloat() + obstacleWidth else -obstacleWidth.toFloat(),
            speed = slidingSpeed * direction,
            width = obstacleWidth,
            height = obstacleHeight,
            colorType = obstacleColor,
            type = obstacleType,
            lastUpdateTime = frameTime
        )
    }

    override fun reset() {
        // Nothing to reset
    }

    override fun setDemoMode(isDemoMode: Boolean) {
        // Tutorial mode doesn't care about demo mode
    }

    override fun shouldGenerateObstacle(frameTime: Long): Boolean {
        currentTutorialStep ?: return false

        return if (currentTutorialStep == TutorialStep.SAND_INTRO) {
            false
        } else {
            val interval = 3000L
            return frameTime - lastSlidingObstacleTime >= interval
        }
    }
}