package hu.adamfejes.zenmotes

import timber.log.Timber
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

private const val slidingObstacleTransitTimeSeconds = 7.5f

class SandGrid(
    private val width: Int,
    private val height: Int
) {
    // Non-settle zone at top 5% of screen to prevent stuck particles
    private val nonSettleZoneHeight = (height * 0.05f).toInt().coerceAtLeast(3)

    // Non-obstacle zone at top 15% of screen to prevent obstacles from sitting on top
    private val nonObstacleZoneHeight = (height * 0.15f).toInt().coerceAtLeast(10)

    // Cleanup routine timing - run every 2 seconds
    private var lastCleanupTime = 0L
    private val cleanupIntervalMs = 500L

    // Performance tracking
    private var frameCount = 0
    private var lastUpdateDuration = 0L
    private var avgUpdateDuration = 0L
    
    // Pause tracking
    private var pauseStartTime: Long? = null
    private var totalPausedTime = 0L

    // Separate components for different responsibilities
    private val gridState = GridState(width, height)
    private val obstacleGenerator = ObstacleGenerator(width, height, nonObstacleZoneHeight, slidingObstacleTransitTimeSeconds)
    private val particlePhysics = ParticlePhysics(width, height, nonSettleZoneHeight)

    fun getCell(x: Int, y: Int): Cell? = gridState.getCell(x, y)

    fun setCell(x: Int, y: Int, cell: Cell) = gridState.setCell(x, y, cell)

    fun addSand(x: Int, y: Int, colorType: ColorType, currentTime: Long = System.currentTimeMillis()) {
        if (x in 0 until width && y in 0 until height && gridState.getCell(x, y)?.type == CellType.EMPTY) {
            val particle = particlePhysics.createSandParticle(colorType, currentTime)
            setCell(x, y, Cell(CellType.SAND, particle))
            gridState.addMovingParticle(MovingParticle(x, y, particle))
        }
    }
    
    fun onPause() {
        pauseStartTime = System.currentTimeMillis()
    }
    
    fun onResume() {
        pauseStartTime?.let { startTime ->
            val pauseDuration = System.currentTimeMillis() - startTime
            totalPausedTime += pauseDuration
            pauseStartTime = null
        }
    }

    fun update(currentTime: Long = System.currentTimeMillis()) {
        // 1. Grid creation
        var initialGrid: Array<Array<Cell>>
        val gridCreateTime = measureTimeMillis {
            initialGrid = gridState.getGrid()//createNewGrid()
        }

        // 2. Obstacle updates  
        var gridAfterObstacles: Array<Array<Cell>>
        val obstacleTime = measureTimeMillis {
            gridAfterObstacles = updateSlidingObstacles(initialGrid, currentTime)
        }

        // 3. Particle physics
        var gridAfterParticles: Array<Array<Cell>>
        val particleTime = measureTimeMillis {
            gridAfterParticles = processMovingParticles(gridAfterObstacles, currentTime)
        }

        // 4. Grid state update
        val updateGridTime = measureTimeMillis {
            gridState.updateGrid(gridAfterParticles)
        }

        // 5. Cleanup routine (periodic)
        val cleanupTime = if (currentTime - lastCleanupTime >= cleanupIntervalMs) {
            measureTimeMillis {
                cleanupInconsistentSettledParticles(currentTime)
                lastCleanupTime = currentTime
            }
        } else 0L

        val totalTime = gridCreateTime + obstacleTime + particleTime + updateGridTime + cleanupTime

        // Update performance tracking
        frameCount++
        avgUpdateDuration = (avgUpdateDuration * 0.8 + totalTime * 0.2).toLong()

        // Calculate FPS every 20 frames
        if (frameCount % 10 == 0) {
            lastUpdateDuration = totalTime
        }

        Timber.tag("SandPerf").d("BREAKDOWN: Grid: ${gridCreateTime}ms | Obstacles: ${obstacleTime}ms | Particles: ${particleTime}ms | Update: ${updateGridTime}ms | Cleanup: ${cleanupTime}ms")
        Timber.tag("SandPerf").d("TOTAL: ${totalTime}ms | Moving: ${gridState.getMovingParticles().size} | Settled: ${gridState.getSettledParticles().size}")
    }

    private fun updateSlidingObstacles(grid: Array<Array<Cell>>, currentTime: Long): Array<Array<Cell>> {
        // Generate new sliding obstacles if needed (using adjusted time)
        val adjustedTime = currentTime - totalPausedTime
        val generationTime = measureTimeMillis {
            obstacleGenerator.generateSlidingObstacle(adjustedTime)?.let { newObstacle ->
                gridState.addSlidingObstacle(newObstacle)
                Timber.tag("SlidingObstacle").d("🎯 Generated sliding obstacle: ${newObstacle.size}x${newObstacle.size} at y=${newObstacle.y}")
            }
        }

        // Update existing sliding obstacles
        val currentObstacles = gridState.getSlidingObstacles()
        val updatedObstacles = mutableListOf<SlidingObstacle>()

        // Start with the passed grid
        var workingGrid = grid

        var clearTime = 0L
        var positionUpdateTime = 0L
        var particleMoveTime = 0L
        var gridPlacementTime = 0L
        var gridClearTime = 0L
        var gridPlaceTime = 0L

        for (obstacle in currentObstacles) {
            // Clear old obstacle position from working grid
            val clearStartTime = System.nanoTime()
            workingGrid = clearSlidingObstacleFromGrid(workingGrid, obstacle)
            clearTime += (System.nanoTime() - clearStartTime)

            // Check if obstacle should be destroyed by sand weight
            val sandHeight = calculateSandHeightAboveSlidingObstacle(workingGrid, obstacle)
            val weightThreshold = obstacle.size * obstacle.size / 2f // Threshold based on obstacle size

            if (sandHeight >= weightThreshold) {
                Timber.tag("SlidingObstacle").d("💥 Destroying sliding obstacle due to sand weight: $sandHeight >= $weightThreshold")
                // Convert obstacle to sand particles instead of updating position
                workingGrid = destroySlidingObstacle(workingGrid, obstacle)
                continue
            }

            // Update obstacle position with adjusted time (excluding paused time)
            val posUpdateStartTime = System.nanoTime()
            val adjustedTime = currentTime - totalPausedTime
            val updatedObstacle = obstacleGenerator.updateObstaclePosition(obstacle, adjustedTime)
            positionUpdateTime += (System.nanoTime() - posUpdateStartTime)

            // Move settled particles to new position to follow the obstacle
            val particleMoveStartTime = System.nanoTime()
            workingGrid = updateSettledParticles(workingGrid, obstacle, updatedObstacle)
            particleMoveTime += (System.nanoTime() - particleMoveStartTime)

            // Check if obstacle has moved off screen
            if (!obstacleGenerator.isObstacleOffScreen(updatedObstacle)) {
                updatedObstacles.add(updatedObstacle)
                // Place updated obstacle in working grid
                val placementStartTime = System.nanoTime()
                workingGrid = placeSlidingObstacleInGrid(workingGrid, updatedObstacle)
                gridPlacementTime += (System.nanoTime() - placementStartTime)
            }
        }

        // Update the sliding obstacles list
        gridState.setSlidingObstacles(updatedObstacles)

        val clearTimeMs = clearTime / 1_000_000.0
        val positionUpdateTimeMs = positionUpdateTime / 1_000_000.0
        val particleMoveTimeMs = particleMoveTime / 1_000_000.0
        val gridPlacementTimeMs = gridPlacementTime / 1_000_000.0

        Timber.tag("ObstaclePerf").d("SLIDING: Gen=${generationTime}ms | Clear=${clearTimeMs}ms | PosUpdate=${positionUpdateTimeMs}ms | ParticleMove=${particleMoveTimeMs}ms | Placement=${gridPlacementTimeMs}ms")
        Timber.tag("SlidingObstacle").d("🚀 Active sliding obstacles: ${updatedObstacles.size}")

        return workingGrid
    }

    private fun processMovingParticles(grid: Array<Array<Cell>>, currentTime: Long): Array<Array<Cell>> {
        val physicsStartTime = System.nanoTime()
        val newMovingParticles = mutableListOf<MovingParticle>()

        // Process moving particles in random order to avoid asymmetry
        val shuffleStartTime = System.nanoTime()
        val shuffledMoving = gridState.getMovingParticles().shuffled()
        val shuffleTime = (System.nanoTime() - shuffleStartTime) / 1_000_000.0

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

            val particleStartTime = System.nanoTime()
            val result = particlePhysics.tryMoveSandWithGravity(x, y, particle, grid, currentTime)
            val particleTime = (System.nanoTime() - particleStartTime) / 1_000_000.0

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

        // Update moving particles list
        gridState.setMovingParticles(newMovingParticles)

        val physicsTime = (System.nanoTime() - physicsStartTime) / 1_000_000.0
        Timber.tag("SandPerf").d("Shuffle: ${shuffleTime}ms, Movement: ${movementTime}ms, Collision: ${collisionTime}ms, Total Physics: ${physicsTime}ms, Particles: $particleCount")

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
            obstacles = gridState.getSlidingObstacles().size
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

    private fun clearSlidingObstacleFromGrid(grid: Array<Array<Cell>>, obstacle: SlidingObstacle): Array<Array<Cell>> {
        val startTime = System.nanoTime()
        val centerX = obstacle.x.toInt()
        val centerY = obstacle.y
        val size = obstacle.size
        var cellsCleared = 0

        for (dy in -size/2..size/2) {
            for (dx in -size/2..size/2) {
                val x = centerX + dx
                val y = centerY + dy
                if (x in 0 until width && y in 0 until height) {
                    if (grid[y][x].type == CellType.SLIDING_OBSTACLE) {
                        grid[y][x] = Cell(CellType.EMPTY)
                        cellsCleared++
                    }
                }
            }
        }

        val elapsedMs = (System.nanoTime() - startTime) / 1_000_000.0
        Timber.tag("GridPerf").d("CLEAR: ${elapsedMs}ms for ${cellsCleared} cells (${obstacle.size}x${obstacle.size})")

        return grid
    }

    private fun placeSlidingObstacleInGrid(grid: Array<Array<Cell>>, obstacle: SlidingObstacle): Array<Array<Cell>> {
        val startTime = System.nanoTime()
        val centerX = obstacle.x.toInt()
        val centerY = obstacle.y
        val size = obstacle.size
        var cellsPlaced = 0

        for (dy in -size/2..size/2) {
            for (dx in -size/2..size/2) {
                val x = centerX + dx
                val y = centerY + dy
                if (x in 0 until width && y in 0 until height) {
                    if (grid[y][x].type == CellType.EMPTY) {
                        grid[y][x] = Cell(CellType.SLIDING_OBSTACLE, null, obstacle)
                        cellsPlaced++
                    }
                }
            }
        }

        val elapsedMs = (System.nanoTime() - startTime) / 1_000_000.0
        Timber.tag("GridPerf").d("PLACE: ${elapsedMs}ms for ${cellsPlaced} cells (${obstacle.size}x${obstacle.size})")


        return grid
    }

    private fun updateSettledParticles(grid: Array<Array<Cell>>, obstacle: SlidingObstacle, updatedObstacle: SlidingObstacle): Array<Array<Cell>> {
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
            grid[y][x] = Cell()
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

        Timber.tag("SlidingObstacle").d("🏃 Moved ${movedParticles.size}/${particlesToMove.size} settled particles with obstacle ${obstacle.id}")

        return grid
    }

    private fun calculateSandHeightAboveSlidingObstacle(grid: Array<Array<Cell>>, obstacle: SlidingObstacle): Int {
        val startTime = System.nanoTime()
        
        // Get all particles tied to this obstacle
        val fetchStartTime = System.nanoTime()
        val obstacleParticles = gridState.getSettledParticlesByObstacleId(obstacle.id)
        val fetchTime = (System.nanoTime() - fetchStartTime) / 1_000_000.0
        
        var totalWeight = 0
        var particleCount = 0
        
        val calculationStartTime = System.nanoTime()
        for (settledParticle in obstacleParticles) {
            val cell = grid[settledParticle.y][settledParticle.x]
            if (cell.type == CellType.SAND && cell.particle != null) {
                // Double weight if color matches obstacle color
                val weight = if (cell.particle.colorType == obstacle.colorType) 2 else 1
                totalWeight += weight
                particleCount++
            }
        }
        val calculationTime = (System.nanoTime() - calculationStartTime) / 1_000_000.0
        
        val totalTime = (System.nanoTime() - startTime) / 1_000_000.0
        
        Timber.tag("SandWeight").d("Obstacle ${obstacle.id}: ${particleCount} particles, weight: ${totalWeight} | Fetch: ${fetchTime}ms | Calc: ${calculationTime}ms | Total: ${totalTime}ms")
        
        // Return the weighted count as an integer (rounded down)
        return totalWeight
    }

    private fun destroySlidingObstacle(grid: Array<Array<Cell>>, obstacle: SlidingObstacle): Array<Array<Cell>> {
        val centerX = obstacle.x.roundToInt()
        val centerY = obstacle.y
        val halfSize = obstacle.size / 2

        // Convert entire obstacle block to sand particles
        var convertedCells = 0
        var totalCellsChecked = 0
        for (dy in -halfSize..halfSize) {
            for (dx in -halfSize..halfSize) {
                val x = centerX + dx
                val y = centerY + dy
                if (x in 0 until width && y in 0 until height) {
                    totalCellsChecked++
                    val cellType = grid[y][x].type
                    
                    // Convert any cell within obstacle bounds to sand (not just SLIDING_OBSTACLE)
                    if (cellType == CellType.SLIDING_OBSTACLE || cellType == CellType.EMPTY) {
                        val sandParticle = particlePhysics.createSandParticle(obstacle.colorType, System.currentTimeMillis()).copy(
                            velocityY = 0.5f, // Give some initial velocity so they fall immediately
                            noiseVariation = 0.9f,
                            isActive = true,
                            isSettled = false,
                            used = true // Mark as used so it never settles again
                        )
                        grid[y][x] = Cell(CellType.SAND, sandParticle)
                        gridState.addMovingParticle(MovingParticle(x, y, sandParticle))
                        convertedCells++
                    }
                }
            }
        }
        
        Timber.tag("SlidingObstacle").d("💥 Converted ${convertedCells}/${totalCellsChecked} cells to sand for obstacle ${obstacle.id} (${obstacle.size}x${obstacle.size})")

        // Convert all linked settled particles to normal falling sand
        for (settledParticle in gridState.getSettledParticlesByObstacleId(obstacle.id)) {
            val x = settledParticle.x
            val y = settledParticle.y
            val cell = grid[y][x]

            if (cell.type == CellType.SAND && cell.particle != null) {
                val reactivatedParticle = cell.particle.copy(
                    isActive = true,
                    velocityY = 0.5f, // Give some initial velocity to start falling
                    isSettled = false, // No longer settled since support is gone
                    obstacleId = null, // No longer associated with the destroyed obstacle
                    used = true // Mark as used so it never settles again
                )
                grid[y][x] = Cell(CellType.SAND, reactivatedParticle)
                gridState.addMovingParticle(MovingParticle(x, y, reactivatedParticle))

                // Remove from settled particles
                gridState.removeSettledParticle(x, y)
            }
        }

        return grid
    }

    private fun cleanupInconsistentSettledParticles(currentTime: Long) {
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
            } else {
                // Particle position doesn't match - remove from settled list
                particlesToReactivate.add(settledParticle)
            }
        }

        if (particlesToReactivate.isNotEmpty()) {
            Timber.tag("SandCleanup").d("🧹 Cleaning up ${particlesToReactivate.size} inconsistent settled particles")

            for (particlePos in particlesToReactivate) {
                val x = particlePos.x
                val y = particlePos.y
                val cell = grid[y][x]

                if (cell.type == CellType.SAND && cell.particle != null) {
                    // Convert to normal falling sand
                    val reactivatedParticle = cell.particle.copy(
                        isActive = true,
                        velocityY = 0.2f, // Give gentle initial velocity
                        isSettled = false,
                        obstacleId = null // Clear obstacle link
                    )
                    gridState.setCell(x, y, Cell(CellType.SAND, reactivatedParticle))
                    gridState.addMovingParticle(MovingParticle(x, y, reactivatedParticle))
                }

                // Remove from settled particles list
                gridState.removeSettledParticle(x, y)
            }
        }
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