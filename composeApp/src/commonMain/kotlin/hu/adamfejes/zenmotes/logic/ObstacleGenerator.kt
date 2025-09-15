package hu.adamfejes.zenmotes.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.random.Random
import hu.adamfejes.zenmotes.utils.Logger

class ObstacleGenerator(
    private val width: Int,
    private val height: Int,
    private val nonObstacleZoneHeight: Int,
    slidingObstacleTransitTimeSeconds: Float,
    private val sandColorManager: SandColorManager
) : IObstacleGenerator {
    private val initialSlidingObstacleInterval = 2500L
    private val minSlidingObstacleInterval = 500L
    private val intervalReductionAmount = 50L
    private val difficultyIncreaseInterval = 5000L // 5 seconds in game time

    private var currentSlidingObstacleInterval = initialSlidingObstacleInterval
    private var lastDifficultyIncreaseTime = 0L
    private val slidingSpeed = width / slidingObstacleTransitTimeSeconds // pixels per second
    private var lastSlidingObstacleTime = 0L

    // Use domain-layer color types
    private val colorTypes = ColorType.entries.toTypedArray()

    // Track when to use the current sand color vs random
    private var useCurrentSandColorForNext = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        // Listen for sand color changes
        scope.launch {
            sandColorManager.currentSandColor
                .collect { newColor ->
                    useCurrentSandColorForNext = true
                }
        }
    }

    private fun shouldGenerateObstacle(frameTime: Long): Boolean {
        return frameTime - lastSlidingObstacleTime >= currentSlidingObstacleInterval
    }

    private fun updateDifficulty(frameTime: Long) {
        if (lastDifficultyIncreaseTime == 0L) {
            lastDifficultyIncreaseTime = frameTime
            return
        }

        if (frameTime - lastDifficultyIncreaseTime >= difficultyIncreaseInterval) {
            if (currentSlidingObstacleInterval > minSlidingObstacleInterval) {
                currentSlidingObstacleInterval = maxOf(
                    minSlidingObstacleInterval,
                    currentSlidingObstacleInterval - intervalReductionAmount
                )
                Logger.d("ObstacleGenerator","Increased difficulty: new interval = $currentSlidingObstacleInterval")
                lastDifficultyIncreaseTime = frameTime
            }
        }
    }

    override fun generateSlidingObstacle(frameTime: Long, obstacleTypes: List<SlidingObstacleType>): SlidingObstacle? {
        updateDifficulty(frameTime)

        if (!shouldGenerateObstacle(frameTime)) return null
        if (obstacleTypes.isEmpty()) return null

        lastSlidingObstacleTime = frameTime
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

        // Determine color: use current sand color if flag is set, otherwise random with bias
        val obstacleColor = if (useCurrentSandColorForNext) {
            useCurrentSandColorForNext = false // Reset flag after using
            sandColorManager.currentSandColor.value
        } else {
            // Random selection with double chance for current sand color
            val currentColor = sandColorManager.currentSandColor.value
            val colorPool = colorTypes.toMutableList().apply {
                add(currentColor)
                add(currentColor) // Add current color twice fro having 50% chance
            }
            colorPool.random()
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
        lastSlidingObstacleTime = 0L
        lastDifficultyIncreaseTime = 0L
        currentSlidingObstacleInterval = initialSlidingObstacleInterval
    }
}