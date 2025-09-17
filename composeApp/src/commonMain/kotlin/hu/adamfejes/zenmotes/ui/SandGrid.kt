package hu.adamfejes.zenmotes.ui

import androidx.compose.ui.graphics.ImageBitmap
import hu.adamfejes.zenmotes.logic.Cell
import hu.adamfejes.zenmotes.logic.CellType
import hu.adamfejes.zenmotes.logic.ColorType
import hu.adamfejes.zenmotes.logic.GridState
import hu.adamfejes.zenmotes.logic.MovingParticle
import hu.adamfejes.zenmotes.logic.ObstacleAnimator
import hu.adamfejes.zenmotes.logic.ObstacleGenerator
import hu.adamfejes.zenmotes.logic.ParticlePhysics
import hu.adamfejes.zenmotes.logic.SandColorManager
import hu.adamfejes.zenmotes.logic.ParticlePosition
import hu.adamfejes.zenmotes.logic.PerformanceData
import hu.adamfejes.zenmotes.logic.SlidingObstacle
import hu.adamfejes.zenmotes.logic.SlidingObstacleType
import hu.adamfejes.zenmotes.logic.setCell
import hu.adamfejes.zenmotes.service.SoundManager
import hu.adamfejes.zenmotes.service.SoundSample
import hu.adamfejes.zenmotes.utils.Logger
import hu.adamfejes.zenmotes.utils.TimeUtils
import kotlin.math.roundToInt
import kotlin.time.measureTime

private const val slidingObstacleTransitTimeSeconds = 7.5f

class SandGrid(
    private val width: Int,
    private val height: Int,
    private val soundManager: SoundManager,
    private val maxMovingParticles: Int, // Parameterized limit for moving particles
    private val sandColorManager: SandColorManager
) {
    // Sand generation control
    private var isSandGenerationActive = false
    private var sandGenerationSourceX = 0f
    private var sandGenerationColorType = ColorType.OBSTACLE_COLOR_1
    private var sandGenerationAmount = 1
    private var lastSandGenerationTime = 0L
    private val sandGenerationIntervalMs = 16L // ~60 FPS for sand generation
    // Non-settle zone at top 5% of screen to prevent stuck particles
    private val nonSettleZoneHeight = (height * 0.05f).toInt().coerceAtLeast(3)

    // Non-obstacle zone at top 15% of screen to prevent obstacles from sitting on top
    private val nonObstacleZoneHeight = (height * 0.15f).toInt().coerceAtLeast(10)

    // Cleanup routine timing - run every 2 seconds
    private var lastCleanupTime = 0L
    private val cleanupIntervalMs = 100L

    private var previousNumberOfMovingParticles = 0

    // Performance tracking
    private var lastFrameTime = 0L
    private var frameCount = 0
    private var lastUpdateDuration = 0L
    private var avgUpdateDuration = 0L

    // Pause tracking
    private var pauseStartTime: Long? = null
    private var totalPausedTime = 0L

    // Separate components for different responsibilities
    private val gridState = GridState(width, height)
    private val obstacleGenerator =
        ObstacleGenerator(width, height, nonObstacleZoneHeight, slidingObstacleTransitTimeSeconds, sandColorManager)

    //ListBasedObstacleGenerator(width = width, height = height)
    private val obstacleAnimator = ObstacleAnimator(width)
    private val particlePhysics = ParticlePhysics(width, height, nonSettleZoneHeight)

    // Obstacle types with loaded bitmaps
    private lateinit var obstacleTypes: List<SlidingObstacleType>

    fun setObstacleTypes(types: List<SlidingObstacleType>) {
        obstacleTypes = types
    }

    fun setSandGeneration(
        active: Boolean,
        sourceX: Float = 0f,
        colorType: ColorType = ColorType.OBSTACLE_COLOR_1,
        amount: Int = 1
    ) {
        isSandGenerationActive = active
        if (active) {
            sandGenerationSourceX = sourceX
            sandGenerationColorType = colorType
            sandGenerationAmount = amount
        }
    }

    fun getCell(x: Int, y: Int): Cell? = gridState.getCell(x, y)

    fun setCell(x: Int, y: Int, cell: Cell) = gridState.setCell(x, y, cell)

    fun addSand(
        x: Int,
        y: Int,
        colorType: ColorType,
        currentTime: Long = TimeUtils.currentTimeMillis()
    ) {
        if (x in 0 until width && y in 0 until height && gridState.getCell(
                x,
                y
            )?.type == CellType.EMPTY
        ) {
            val particle = particlePhysics.createSandParticle(colorType, currentTime)
            setCell(x, y, Cell(CellType.SAND, particle))
            gridState.addMovingParticle(MovingParticle(x, y, particle))
        }
    }

    fun onPause() {
        pauseStartTime = TimeUtils.currentTimeMillis()
        obstacleGenerator.onPause()
    }

    fun onResume() {
        pauseStartTime?.let { startTime ->
            val pauseDuration = TimeUtils.currentTimeMillis() - startTime
            totalPausedTime += pauseDuration
            pauseStartTime = null
        }
        obstacleGenerator.onResume()
    }

    fun update(
        frameTime: Long,
        increaseScore: (slidingObstacle: SlidingObstacle, isBonus: Boolean) -> Unit,
        decreaseScore: (slidingObstacle: SlidingObstacle) -> Unit
    ) {
        // 1. Grid creation
        var initialGrid: Array<Array<Cell>>
        val gridCreateTime = measureTime {
            initialGrid = gridState.getGrid()//createNewGrid()
        }.inWholeMilliseconds

        // 2. Time-based sand generation
        var gridAfterSandGeneration: Array<Array<Cell>>
        val sandGenerationTime = measureTime {
            gridAfterSandGeneration = processTimedSandGeneration(initialGrid, frameTime)
        }.inWholeMilliseconds

        // 3. Obstacle updates
        var gridAfterObstacles: Array<Array<Cell>>
        val obstacleTime = measureTime {
            gridAfterObstacles =
                updateSlidingObstacles(gridAfterSandGeneration, frameTime, increaseScore, decreaseScore)
        }.inWholeMilliseconds

        // 3. Particle physics
        var gridAfterParticles: Array<Array<Cell>>
        val particleTime = measureTime {
            gridAfterParticles = processMovingParticles(gridAfterObstacles, frameTime)
        }.inWholeMilliseconds

        // 4. Grid state update
        val updateGridTime = measureTime {
            gridState.updateGrid(gridAfterParticles)
        }.inWholeMilliseconds

        manageSounds(previousNumberOfMovingParticles, gridState.getMovingParticles().size)
        previousNumberOfMovingParticles = gridState.getMovingParticles().size

        // 5. Cleanup routine (periodic)
        val cleanupTime = if (frameTime - lastCleanupTime >= cleanupIntervalMs) {
            measureTime {
                cleanupInconsistentSettledParticles()
                lastCleanupTime = frameTime
            }.inWholeMilliseconds
        } else 0L

        val totalTime = gridCreateTime + sandGenerationTime + obstacleTime + particleTime + updateGridTime + cleanupTime

        // Update performance tracking
        frameCount++
        avgUpdateDuration = (avgUpdateDuration * 0.8 + totalTime * 0.2).toLong()

        // Calculate FPS every 20 frames
        if (frameCount % 10 == 0) {
            lastUpdateDuration = totalTime
        }

        Logger.d(
            "SandPerf",
            "Frame time: ${frameTime}ms, time since last frame: ${frameTime - lastFrameTime}ms | Frame count: $frameCount | Last update: ${lastUpdateDuration}ms | Avg update: ${avgUpdateDuration}ms"
        )

        Logger.d(
            "SandPerf",
            "BREAKDOWN: Grid: ${gridCreateTime}ms | SandGen: ${sandGenerationTime}ms | Obstacles: ${obstacleTime}ms | Particles: ${particleTime}ms | Update: ${updateGridTime}ms | Cleanup: ${cleanupTime}ms"
        )
        Logger.d(
            "SandPerf",
            "TOTAL: ${totalTime}ms | Moving: ${gridState.getMovingParticles().size} | Settled: ${gridState.getSettledParticles().size}"
        )

        lastFrameTime = frameTime
    }

    private fun manageSounds(previousNumberOfMovingParticles: Int, currentNumberOfMovingParticles: Int) {
        if(currentNumberOfMovingParticles > 0) {
            if (previousNumberOfMovingParticles == 0) {
                // More particles are moving, play sound
                soundManager.play(SoundSample.SAND_BEGIN)
                soundManager.play(SoundSample.SAND_MIDDLE, loop = true)
            }
        } else {
            soundManager.stopAll()
        }

        soundManager.setVolume(currentNumberOfMovingParticles * 0.7f / maxMovingParticles.toFloat())
    }

    private fun processTimedSandGeneration(
        grid: Array<Array<Cell>>,
        frameTime: Long
    ): Array<Array<Cell>> {
        // Only generate sand if active and enough time has passed
        if (!isSandGenerationActive || frameTime - lastSandGenerationTime < sandGenerationIntervalMs) {
            return grid
        }

        val centerX = sandGenerationSourceX.roundToInt().coerceIn(0, width - 1)

        // Generate multiple sand particles in a sprinkle pattern at the top of the screen
        var particlesAdded = 0
        repeat(sandGenerationAmount) {
            val spreadX = centerX + (-3..3).random()
            val spreadY = 0 // Always spawn at the top of the screen

            if (spreadX in 0 until width && grid[spreadY][spreadX].type == CellType.EMPTY) {
                val particle = particlePhysics.createSandParticle(sandGenerationColorType, frameTime)
                grid[spreadY][spreadX] = Cell(CellType.SAND, particle)
                gridState.addMovingParticle(MovingParticle(spreadX, spreadY, particle))
                particlesAdded++
            }
        }

        // Update last generation time only if we actually added particles
        if (particlesAdded > 0) {
            lastSandGenerationTime = frameTime
        }

        return grid
    }

    private fun updateSlidingObstacles(
        grid: Array<Array<Cell>>,
        frameTime: Long,
        increaseScore: (SlidingObstacle, Boolean) -> Unit,
        decreaseScore: (SlidingObstacle) -> Unit
    ): Array<Array<Cell>> {
        // Generate new sliding obstacles if needed (using adjusted time)
        val adjustedTime = frameTime - totalPausedTime
        val generationTime = measureTime {
            obstacleGenerator.generateSlidingObstacle(adjustedTime, obstacleTypes)?.let { newObstacle ->
                gridState.addSlidingObstacle(newObstacle)
                Logger.d(
                    "SlidingObstacle",
                    "🎯 Generated sliding obstacle: ${newObstacle.width}x${newObstacle.height} at y=${newObstacle.y}"
                )
            }
        }.inWholeMilliseconds

        // Update existing sliding obstacles
        val currentObstacles = gridState.getSlidingObstacles()
        val updatedObstacles = mutableListOf<SlidingObstacle>()

        // Start with the passed grid
        var workingGrid = grid

        var clearTime = 0L
        var positionUpdateTime = 0L
        var particleMoveTime = 0L
        var gridPlacementTime = 0L

        for (obstacle in currentObstacles) {
            // Clear old obstacle position from working grid
            val clearStartTime = TimeUtils.nanoTime()
            workingGrid = clearSlidingObstacleFromGrid(workingGrid, obstacle)
            clearTime += (TimeUtils.nanoTime() - clearStartTime)

            // Check if obstacle should be destroyed by sand weight
            val (sandHeight, isBonus) = calculateSandHeightAboveSlidingObstacle(workingGrid, obstacle)
            val weightThreshold =
                obstacle.width * obstacle.height / 2f * 0.9f // Threshold based on obstacle area

            if (sandHeight >= weightThreshold) {
                Logger.d(
                    "SlidingObstacle",
                    "💥 Destroying sliding obstacle due to sand weight: $sandHeight >= $weightThreshold"
                )
                // Add score for destroying obstacle (bonus already calculated)
                increaseScore(obstacle, isBonus)
                // Convert obstacle to sand particles instead of updating position
                workingGrid = destroySlidingObstacle(workingGrid, obstacle)
                continue
            }

            // Update obstacle position with adjusted time (excluding paused time)
            val posUpdateStartTime = TimeUtils.nanoTime()
            val adjustedTime = frameTime - totalPausedTime
            val updatedObstacle = obstacleAnimator.updateObstaclePosition(obstacle, adjustedTime)
            positionUpdateTime += (TimeUtils.nanoTime() - posUpdateStartTime)

            // Move settled particles to new position to follow the obstacle
            val particleMoveStartTime = TimeUtils.nanoTime()
            workingGrid = updateSettledParticles(workingGrid, obstacle, updatedObstacle)
            particleMoveTime += (TimeUtils.nanoTime() - particleMoveStartTime)

            // Check if obstacle has moved off screen
            if (!obstacleAnimator.isObstacleOffScreen(updatedObstacle)) {
                updatedObstacles.add(updatedObstacle)
                // Place updated obstacle in working grid
                val placementStartTime = TimeUtils.nanoTime()
                workingGrid = placeSlidingObstacleInGrid(workingGrid, updatedObstacle)
                gridPlacementTime += (TimeUtils.nanoTime() - placementStartTime)
            } else {
                Logger.d(
                    "SlidingObstacle",
                    "🚫 Obstacle off screen, removing: ${updatedObstacle.id} at x=${updatedObstacle.x}, y=${updatedObstacle.y}"
                )
                // Add score for obstacle removal
                decreaseScore(obstacle)
            }
        }

        // Update the sliding obstacles list
        gridState.setSlidingObstacles(updatedObstacles)

        val clearTimeMs = clearTime / 1_000_000.0
        val positionUpdateTimeMs = positionUpdateTime / 1_000_000.0
        val particleMoveTimeMs = particleMoveTime / 1_000_000.0
        val gridPlacementTimeMs = gridPlacementTime / 1_000_000.0

        Logger.d(
            "ObstaclePerf",
            "SLIDING: Gen=${generationTime}ms | Clear=${clearTimeMs}ms | PosUpdate=${positionUpdateTimeMs}ms | ParticleMove=${particleMoveTimeMs}ms | Placement=${gridPlacementTimeMs}ms"
        )
        Logger.d("SlidingObstacle", "🚀 Active sliding obstacles: ${updatedObstacles.size}")

        return workingGrid
    }

    private fun processMovingParticles(
        grid: Array<Array<Cell>>,
        frameTime: Long
    ): Array<Array<Cell>> {
        val physicsStartTime = TimeUtils.nanoTime()
        val newMovingParticles = mutableListOf<MovingParticle>()

        // Process moving particles in random order to avoid asymmetry
        val shuffleStartTime = TimeUtils.nanoTime()
        val shuffledMoving = gridState.getMovingParticles().shuffled()
        val shuffleTime = (TimeUtils.nanoTime() - shuffleStartTime) / 1_000_000.0

        var collisionTime = 0.0
        var movementTime = 0.0
        var particleCount = 0

        for (movingParticle in shuffledMoving) {
            val (x, y, particle) = movingParticle

            // Skip if position has been overwritten by obstacle update
            val currentCell = grid[y][x]
            if (currentCell.type != CellType.SAND || currentCell.particle != particle) {
                continue
            }

            val particleStartTime = TimeUtils.nanoTime()
            val result = particlePhysics.tryMoveSandWithGravity(x, y, particle, grid, frameTime)
            val particleTime = (TimeUtils.nanoTime() - particleStartTime) / 1_000_000.0

            if (result.moved) {
                movementTime += particleTime

                // Add to moving particles if not removed from screen
                result.newParticle?.let { newMovingParticles.add(it) }
            } else {
                collisionTime += particleTime

                // Handle settled particles
                if (result.isSettled) {
                    result.settledPosition?.let { gridState.addSettledParticle(it) }
                } else {
                    result.newParticle?.let { newMovingParticles.add(it) }
                }
            }
            particleCount++
        }

        // Apply particle limit and clean up excess particles from grid
        val limitedParticles = if (newMovingParticles.size > maxMovingParticles) {
            val shuffledMovingParticles = newMovingParticles.shuffled()
            val particlesToRemove = shuffledMovingParticles.drop(maxMovingParticles)
            val cleanupStartTime = TimeUtils.nanoTime()

            // Remove excess particles from the grid
            for (particle in particlesToRemove) {
                if (particle.x in 0 until width && particle.y in 0 until height) {
                    val cell = grid[particle.y][particle.x]
                    if (cell.type == CellType.SAND && cell.particle == particle.particle) {
                        grid[particle.y][particle.x] = Cell(CellType.EMPTY)
                    }
                }
            }

            val cleanupTime = (TimeUtils.nanoTime() - cleanupStartTime) / 1_000_000.0
            Logger.d(
                "ParticleLimit",
                "⚡ Removed ${particlesToRemove.size} excess particles (${newMovingParticles.size} → $maxMovingParticles) in ${cleanupTime}ms"
            )

            shuffledMovingParticles.take(maxMovingParticles)
        } else {
            newMovingParticles
        }

        // Update moving particles list with limited particles
        gridState.setMovingParticles(limitedParticles)

        val physicsTime = (TimeUtils.nanoTime() - physicsStartTime) / 1_000_000.0
        Logger.d(
            "SandPerf",
            "Shuffle: ${shuffleTime}ms, Movement: ${movementTime}ms, Collision: ${collisionTime}ms, Total Physics: ${physicsTime}ms, Particles: $particleCount"
        )

        return grid
    }

    fun getAllCells(): List<Triple<Int, Int, Cell>> = gridState.getAllCells()

    fun getWidth() = width
    fun getHeight() = height
    fun getSlidingObstacles() = gridState.getSlidingObstacles()

    // Performance data access
    fun getPerformanceData(): PerformanceData {
        return PerformanceData(
            updateTime = lastUpdateDuration,
            avgUpdateTime = avgUpdateDuration,
            movingParticles = gridState.getMovingParticles().size,
            settledParticles = gridState.getSettledParticles().size,
            obstacles = gridState.getSlidingObstacles().size,
            currentSlidingObstacleInterval = obstacleGenerator.getCurrentSlidingObstacleInterval()
        )
    }

    fun reset() {
        // Reset pause tracking
        pauseStartTime = null
        totalPausedTime = 0L

        // Reset performance tracking
        frameCount = 0
        lastUpdateDuration = 0L
        avgUpdateDuration = 0L
        lastCleanupTime = 0L

        // Reset all components
        gridState.reset()
        obstacleGenerator.reset()
    }

    // Helper functions for functional grid manipulation

    private fun clearSlidingObstacleFromGrid(
        grid: Array<Array<Cell>>,
        obstacle: SlidingObstacle
    ): Array<Array<Cell>> {
        val startTime = TimeUtils.nanoTime()
        val centerX = obstacle.x.toInt()
        val centerY = obstacle.y
        var cellsCleared = 0

        for (dy in -obstacle.height / 2..obstacle.height / 2) {
            for (dx in -obstacle.width / 2..obstacle.width / 2) {
                val x = centerX + dx
                val y = centerY + dy
                if (x in 0 until this.width && y in 0 until this.height) {
                    val cell = grid[y][x]
                    if (cell.type == CellType.SLIDING_OBSTACLE) {
                        if (cell.slidingObstacles.size > 1) {
                            grid.setCell(
                                x,
                                y,
                                Cell(
                                    CellType.SLIDING_OBSTACLE,
                                    null,
                                    cell.slidingObstacles.filter { it.id != obstacle.id })
                            )
                        } else {
                            grid.setCell(x, y, Cell(CellType.EMPTY))
                        }
                        cellsCleared++
                    }
                }
            }
        }

        val elapsedMs = (TimeUtils.nanoTime() - startTime) / 1_000_000.0
        Logger.d(
            "GridPerf",
            "CLEAR: ${elapsedMs}ms for ${cellsCleared} cells (${obstacle.width}x${obstacle.height})"
        )

        return grid
    }

    private fun placeSlidingObstacleInGrid(
        grid: Array<Array<Cell>>,
        obstacle: SlidingObstacle
    ): Array<Array<Cell>> {
        val startTime = TimeUtils.nanoTime()
        val centerX = obstacle.x.toInt()
        val centerY = obstacle.y
        var cellsPlaced = 0

        for (dy in -obstacle.height / 2..obstacle.height / 2) {
            for (dx in -obstacle.width / 2..obstacle.width / 2) {
                val x = centerX + dx
                val y = centerY + dy
                if (x in 0 until this.width && y in 0 until this.height) {
                    if (grid[y][x].type == CellType.EMPTY) {
                        grid.setCell(x, y, Cell(CellType.SLIDING_OBSTACLE, null, listOf(obstacle)))
                        cellsPlaced++
                    } else if (grid[y][x].type == CellType.SLIDING_OBSTACLE) {
                        // Add to existing sliding obstacles
                        val existingObstacles = grid[y][x].slidingObstacles.toMutableList()
                        if (existingObstacles.none { it.id == obstacle.id }) {
                            existingObstacles.add(obstacle)
                            grid.setCell(
                                x,
                                y,
                                Cell(CellType.SLIDING_OBSTACLE, null, existingObstacles)
                            )
                            cellsPlaced++
                        }
                    }
                }
            }
        }

        val elapsedMs = (TimeUtils.nanoTime() - startTime) / 1_000_000.0
        Logger.d(
            "GridPerf",
            "PLACE: ${elapsedMs}ms for ${cellsPlaced} cells (${obstacle.width}x${obstacle.height})"
        )


        return grid
    }

    private fun updateSettledParticles(
        grid: Array<Array<Cell>>,
        obstacle: SlidingObstacle,
        updatedObstacle: SlidingObstacle
    ): Array<Array<Cell>> {
        // Calculate the delta (movement difference) between old and new obstacle positions
        // Round both positions to get integer grid deltas
        val oldGridX = obstacle.x.roundToInt()
        val newGridX = updatedObstacle.x.roundToInt()
        val deltaX = newGridX - oldGridX
        val deltaY = updatedObstacle.y - obstacle.y

        // Only process if there's actual movement
        if (deltaX == 0 && deltaY == 0) return grid

        // Get settled particles that belong to this obstacle directly (much faster)
        val obstacleParticles = gridState.getSettledParticlesByObstacleId(obstacle.id)
        val particlesToMove = obstacleParticles.mapNotNull { settledParticle ->
            val cell = grid[settledParticle.y][settledParticle.x]
            if (cell.type == CellType.SAND && cell.particle?.isSettled == true) {
                Triple(settledParticle.x, settledParticle.y, cell.particle)
            } else {
                null // Particle state is inconsistent, skip it
            }
        }

        if (particlesToMove.isEmpty()) return grid

        // First, clear all the old positions of particles we're about to move
        for ((x, y, _) in particlesToMove) {
            grid.setCell(x, y, Cell())
        }

        // Then, move all particles to their new positions without collision checking
        val movedParticles = mutableListOf<ParticlePosition>()
        for ((oldX, oldY, particle) in particlesToMove) {
            val newX = oldX + deltaX
            val newY = oldY + deltaY

            // Check if new position is valid (within bounds)
            if (newX in 0 until width && newY in 0 until height) {
                // Move particle to new position (no collision checking)
                grid[newY][newX] = Cell(CellType.SAND, particle)
                movedParticles.add(ParticlePosition(newX, newY, obstacle.id))
            }
        }

        // Update settled particle position tracking
        for ((oldX, oldY, _) in particlesToMove) {
            gridState.removeSettledParticle(oldX, oldY)
        }

        for (movedParticle in movedParticles) {
            gridState.addSettledParticle(movedParticle)
        }

        Logger.d(
            "SlidingObstacle",
            "🏃 Moved ${movedParticles.size}/${particlesToMove.size} settled particles with obstacle ${obstacle.id}"
        )

        return grid
    }

    private fun calculateSandHeightAboveSlidingObstacle(
        grid: Array<Array<Cell>>,
        obstacle: SlidingObstacle
    ): Pair<Int, Boolean> {
        val startTime = TimeUtils.nanoTime()

        // Get all particles tied to this obstacle
        val fetchStartTime = TimeUtils.nanoTime()
        val obstacleParticles = gridState.getSettledParticlesByObstacleId(obstacle.id)
        val fetchTime = (TimeUtils.nanoTime() - fetchStartTime) / 1_000_000.0

        var totalWeight = 0
        var particleCount = 0
        var fromObstacleParticles = 0

        val calculationStartTime = TimeUtils.nanoTime()
        for (settledParticle in obstacleParticles) {
            val cell = grid[settledParticle.y][settledParticle.x]
            if (cell.type == CellType.SAND && cell.particle != null) {
                // Double weight if color matches obstacle color
                val weight = if (cell.particle.colorType == obstacle.colorType) 3 else 1
                totalWeight += weight
                particleCount++

                // Count particles from destroyed obstacles
                if (cell.particle.fromObstacle) {
                    fromObstacleParticles++
                }
            }
        }
        val calculationTime = (TimeUtils.nanoTime() - calculationStartTime) / 1_000_000.0

        // Check for bonus condition: at least 10% of sand from other obstacles
        val isBonus = particleCount > 0 && (fromObstacleParticles.toFloat() / particleCount.toFloat()) >= 0.1f

        val totalTime = (TimeUtils.nanoTime() - startTime) / 1_000_000.0

        Logger.d(
            "SandWeight",
            "Obstacle ${obstacle.id}: ${particleCount} particles, weight: ${totalWeight} | FromObstacle: ${fromObstacleParticles} (${if (particleCount > 0) (fromObstacleParticles.toFloat() / particleCount.toFloat() * 100).toInt() else 0}%) | Bonus: $isBonus | Fetch: ${fetchTime}ms | Calc: ${calculationTime}ms | Total: ${totalTime}ms"
        )

        // Return the weighted count as an integer and bonus flag
        return Pair(totalWeight, isBonus)
    }

    private fun destroySlidingObstacle(
        grid: Array<Array<Cell>>,
        obstacle: SlidingObstacle
    ): Array<Array<Cell>> {
        val centerX = obstacle.x.roundToInt()
        val centerY = obstacle.y
        val halfWidth = obstacle.width / 2
        val halfHeight = obstacle.height / 2

        // Convert entire obstacle block to sand particles
        var convertedCells = 0
        var totalCellsChecked = 0
        for (dy in -halfHeight..halfHeight) {
            for (dx in -halfWidth..halfWidth) {
                val x = centerX + dx
                val y = centerY + dy
                if (x in 0 until width && y in 0 until height) {
                    totalCellsChecked++
                    val cellType = grid[y][x].type

                    // Convert any cell within obstacle bounds to sand (not just SLIDING_OBSTACLE)
                    if (cellType == CellType.SLIDING_OBSTACLE || cellType == CellType.EMPTY) {
                        val unsettlingDelay = 300L // 500ms delay before particles can settle again
                        val sandParticle = particlePhysics.createSandParticle(
                            obstacle.colorType,
                            TimeUtils.currentTimeMillis()
                        ).copy(
                            velocityY = 0.5f, // Give some initial velocity so they fall immediately
                            isActive = true,
                            isSettled = false,
                            unsettlingUntil = TimeUtils.currentTimeMillis() + unsettlingDelay,
                            fromObstacle = true
                        )
                        grid.setCell(x, y, Cell(CellType.SAND, sandParticle))
                        gridState.addMovingParticle(MovingParticle(x, y, sandParticle))
                        convertedCells++
                    }
                }
            }
        }

        Logger.d(
            "SlidingObstacle",
            "💥 Converted ${convertedCells}/${totalCellsChecked} cells to sand for obstacle ${obstacle.id} (${obstacle.width}x${obstacle.height})"
        )

        // Convert all linked settled particles to normal falling sand
        for (settledParticle in gridState.getSettledParticlesByObstacleId(obstacle.id)) {
            val x = settledParticle.x
            val y = settledParticle.y
            val cell = grid[y][x]

            if (cell.type == CellType.SAND && cell.particle != null) {
                val unsettlingDelay = 300L // Shorter delay for reactivated particles (300ms)
                val reactivatedParticle = cell.particle.copy(
                    isActive = true,
                    velocityY = 0.5f, // Give some initial velocity to start falling
                    isSettled = false, // No longer settled since support is gone
                    obstacleId = null, // No longer associated with the destroyed obstacle
                    unsettlingUntil = TimeUtils.currentTimeMillis() + unsettlingDelay
                )
                grid.setCell(x, y, Cell(CellType.SAND, reactivatedParticle))
                gridState.addMovingParticle(MovingParticle(x, y, reactivatedParticle))

                // Remove from settled particles
                gridState.removeSettledParticle(x, y)
            }
        }

        return grid
    }

    private fun cleanupInconsistentSettledParticles() {
        val grid = gridState.getGrid()
        val settledParticles = gridState.getSettledParticles()
        val particlesToReactivate = mutableListOf<ParticlePosition>()

        for (settledParticle in settledParticles) {
            val x = settledParticle.x
            val y = settledParticle.y
            val cell = grid[y][x]

            // Check if the particle is actually settled in the grid
            if (cell.type == CellType.SAND && cell.particle?.isSettled == true) {
                // Check if particle has proper support below
                if (!hasProperSupport(x, y, grid)) {
                    particlesToReactivate.add(settledParticle)
                }
            }
        }

        if (particlesToReactivate.isNotEmpty()) {
            Logger.d(
                "SandCleanup",
                "🧹 Cleaning up ${particlesToReactivate.size} inconsistent settled particles at y=${particlesToReactivate.map { it.y }}"
            )

            for (particlePos in particlesToReactivate) {
                reactivateParticleColumn(particlePos.x, particlePos.y, grid)
            }
        }
    }

    private fun reactivateParticleColumn(startX: Int, startY: Int, grid: Array<Array<Cell>>) {
        // Reactivate particles starting from the given position upward until we hit empty space
        var currentY = startY
        var reactivatedCount = 0

        while (currentY >= 0) {
            val cell = grid[currentY][startX]

            if (cell.type == CellType.SAND && cell.particle != null) {
                // Convert to normal falling sand
                val reactivatedParticle = cell.particle.copy(
                    isActive = true,
                    velocityY = 0.2f, // Give gentle initial velocity
                    isSettled = false,
                    obstacleId = null // Clear obstacle link
                )
                gridState.setCell(startX, currentY, Cell(CellType.SAND, reactivatedParticle))
                gridState.addMovingParticle(MovingParticle(startX, currentY, reactivatedParticle))

                // Remove from settled particles list if it was settled
                if (cell.particle.isSettled) {
                    gridState.removeSettledParticle(startX, currentY)
                }

                reactivatedCount++
                currentY-- // Move up to check the next particle
            } else {
                // Hit empty space or non-sand cell, stop the chain
                break
            }
        }

        Logger.d(
            "SandCleanup",
            "🔗 Reactivated $reactivatedCount particles in column at x=$startX, starting from y=$startY"
        )
    }

    private fun hasProperSupport(x: Int, y: Int, grid: Array<Array<Cell>>): Boolean {
        // Check if at bottom of screen
        if (y >= height - 1) return true

        // Check cell directly below
        val belowY = y + 1
        val cellBelow = grid[belowY][x]

        // Has support if there's sand, obstacle, or sliding obstacle below
        return when (cellBelow.type) {
            CellType.SAND -> true
            CellType.OBSTACLE -> true
            CellType.SLIDING_OBSTACLE -> true
            CellType.EMPTY -> false
        }
    }
}
