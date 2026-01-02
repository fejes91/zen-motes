package hu.adamfejes.zenmotes.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.random.Random
import hu.adamfejes.zenmotes.utils.Logger
import hu.adamfejes.zenmotes.utils.TimeUtils
import kotlin.math.roundToLong

class ObstacleGenerator(
    private val width: Int,
    private val height: Int,
    private val nonObstacleZoneHeight: Int,
    slidingObstacleTransitTimeSeconds: Float,
    private val sandColorManager: SandColorManager
) : IObstacleGenerator {
    private val initialSlidingObstacleInterval = 1500L
    private val initialColorMatchProbability = 0.9f
    private val demoModeObstacleInterval = 500L
    private val minSlidingObstacleInterval = 350L
    private val minColorMatchProbability = 0.3f
    private val intervalReductionMultiplier = 0.95f
    private val probabilityReductionMultiplier = 0.98f
    private val difficultyIncreaseInterval = 10000L
    private var currentSlidingObstacleInterval = initialSlidingObstacleInterval
    private var currentColorMatchProbability = initialColorMatchProbability
    private var lastDifficultyIncreaseTime = 0L
    private val slidingSpeed = width / slidingObstacleTransitTimeSeconds // pixels per second
    private var lastSlidingObstacleTime = 0L
    private var isPaused = false
    private var isDemoMode = false

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
        if (isPaused) return false
        val interval = if (isDemoMode) demoModeObstacleInterval else currentSlidingObstacleInterval
        return frameTime - lastSlidingObstacleTime >= interval
    }

    private fun updateDifficulty() {
        // Skip difficulty increase in demo mode
        if (isDemoMode) return

        if (lastDifficultyIncreaseTime == 0L) {
            lastDifficultyIncreaseTime = TimeUtils.currentTimeMillis()
            return
        }

        if (TimeUtils.currentTimeMillis() - lastDifficultyIncreaseTime >= difficultyIncreaseInterval) {
            if (currentSlidingObstacleInterval > minSlidingObstacleInterval) {
                currentSlidingObstacleInterval = maxOf(
                    minSlidingObstacleInterval,
                    (currentSlidingObstacleInterval * intervalReductionMultiplier).roundToLong()
                )
                Logger.d("ObstacleGenerator","Increased difficulty: new interval = $currentSlidingObstacleInterval")
            }

            if(currentColorMatchProbability > minColorMatchProbability) {
                currentColorMatchProbability = maxOf(
                    minColorMatchProbability,
                    currentColorMatchProbability * probabilityReductionMultiplier
                )
                Logger.d("ObstacleGenerator","Increased difficulty: new color match probability = $currentColorMatchProbability")
            }

            lastDifficultyIncreaseTime = TimeUtils.currentTimeMillis()
        }
    }

    override fun generateSlidingObstacle(frameTime: Long, obstacleTypes: List<SlidingObstacleType>): SlidingObstacle? {
        updateDifficulty()

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
        val currentColor = sandColorManager.currentSandColor.value
        val obstacleColor = if (useCurrentSandColorForNext) {
            useCurrentSandColorForNext = false // Reset flag after using
            currentColor
        } else {
            if(Random.nextFloat() < currentColorMatchProbability) {
                currentColor
            } else {
                // Random selection excluding current color
                val filteredColors = colorTypes.filter { it != currentColor }
                filteredColors.random()
            }
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
        currentColorMatchProbability = initialColorMatchProbability
    }

    override fun onPause() {
        isPaused = true
    }

    override fun onResume() {
        isPaused = false
        // Reset timing to restart obstacle generation immediately
        lastSlidingObstacleTime = 0L
        lastDifficultyIncreaseTime = 0L
    }

    fun getCurrentSlidingObstacleInterval(): Long = currentSlidingObstacleInterval

    override fun setDemoMode(isDemoMode: Boolean) {
        this.isDemoMode = isDemoMode
        if (isDemoMode) {
            // Reset timing when entering demo mode
            lastSlidingObstacleTime = 0L
            lastDifficultyIncreaseTime = 0L
        }
    }
}