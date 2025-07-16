package hu.adamfejes.zenmotes

import androidx.compose.ui.graphics.Color
import android.util.Log
import kotlin.math.roundToInt

class SandGrid(
    private val width: Int,
    private val height: Int,
    slidingObstacleTransitTimeSeconds: Float = 5.0f
) {
    // Non-settle zone at top 5% of screen to prevent stuck particles
    private val nonSettleZoneHeight = (height * 0.05f).toInt().coerceAtLeast(3)
    
    // Non-obstacle zone at top 15% of screen to prevent obstacles from sitting on top
    private val nonObstacleZoneHeight = (height * 0.15f).toInt().coerceAtLeast(10)
    
    // Separate components for different responsibilities
    private val gridState = GridState(width, height)
    private val obstacleGenerator = ObstacleGenerator(width, height, nonObstacleZoneHeight, slidingObstacleTransitTimeSeconds)
    private val particlePhysics = ParticlePhysics(width, height, nonSettleZoneHeight)
    
    fun getCell(x: Int, y: Int): Cell? = gridState.getCell(x, y)
    
    fun setCell(x: Int, y: Int, cell: Cell) = gridState.setCell(x, y, cell)
    
    fun addSand(x: Int, y: Int, color: Color, currentTime: Long = System.currentTimeMillis()) {
        if (x in 0 until width && y in 0 until height && gridState.getCell(x, y)?.type == CellType.EMPTY) {
            val particle = particlePhysics.createSandParticle(color, currentTime)
            setCell(x, y, Cell(CellType.SAND, particle))
            gridState.addMovingParticle(MovingParticle(x, y, particle))
        }
    }
    
    fun update(currentTime: Long = System.currentTimeMillis()) {
        val updateStartTime = System.nanoTime()
        
        // Chain all grid modifications together functionally
        val initialGrid = gridState.createNewGrid()
        val gridAfterObstacles = updateSlidingObstacles(initialGrid, currentTime)
        val gridAfterParticles = processMovingParticles(gridAfterObstacles, currentTime)
        
        // Update grid state with final result
        gridState.updateGrid(gridAfterParticles)
        gridState.clearActiveRegions()
        
        val totalUpdateTime = (System.nanoTime() - updateStartTime) / 1_000_000.0
        Log.d("SandPerf", "Total update: ${totalUpdateTime}ms, Moving particles: ${gridState.getMovingParticles().size}, Settled particles: ${gridState.getSettledParticles().size}")
    }
    
    private fun updateSlidingObstacles(grid: Array<Array<Cell>>, currentTime: Long): Array<Array<Cell>> {
        // Generate new sliding obstacles if needed
        obstacleGenerator.generateSlidingObstacle(currentTime)?.let { newObstacle ->
            gridState.addSlidingObstacle(newObstacle)
            Log.d("SlidingObstacle", "🎯 Generated sliding obstacle: ${newObstacle.size}x${newObstacle.size} at y=${newObstacle.y}")
        }
        
        // Update existing sliding obstacles
        val currentObstacles = gridState.getSlidingObstacles()
        val updatedObstacles = mutableListOf<SlidingObstacle>()
        
        // Start with the passed grid
        var workingGrid = grid
        
        for (obstacle in currentObstacles) {
            // Clear old obstacle position from working grid
            workingGrid = clearSlidingObstacleFromGrid(workingGrid, obstacle)
            
            // Update obstacle position
            val updatedObstacle = obstacleGenerator.updateObstaclePosition(obstacle)

            // Move settled particles to new position to follow the obstacle
            workingGrid = updateSettledParticles(workingGrid, obstacle, updatedObstacle)
            
            // Check if obstacle has moved off screen
            if (!obstacleGenerator.isObstacleOffScreen(updatedObstacle)) {
                updatedObstacles.add(updatedObstacle)
                // Place updated obstacle in working grid
                workingGrid = placeSlidingObstacleInGrid(workingGrid, updatedObstacle)
            }
        }
        
        // Update the sliding obstacles list
        gridState.setSlidingObstacles(updatedObstacles)
        
        Log.d("SlidingObstacle", "🚀 Active sliding obstacles: ${updatedObstacles.size}")
        
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
                gridState.addActiveRegion(x, y)
                
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
        Log.d("SandPerf", "Shuffle: ${shuffleTime}ms, Movement: ${movementTime}ms, Collision: ${collisionTime}ms, Total Physics: ${physicsTime}ms, Particles: $particleCount")
        
        return grid
    }
    
    fun getAllCells(): List<Triple<Int, Int, Cell>> = gridState.getAllCells()
    
    fun getWidth() = width
    fun getHeight() = height
    
    // Helper functions for functional grid manipulation
    
    private fun clearSlidingObstacleFromGrid(grid: Array<Array<Cell>>, obstacle: SlidingObstacle): Array<Array<Cell>> {
        val centerX = obstacle.x.toInt()
        val centerY = obstacle.y
        val size = obstacle.size
        
        for (dy in -size/2..size/2) {
            for (dx in -size/2..size/2) {
                val x = centerX + dx
                val y = centerY + dy
                if (x in 0 until width && y in 0 until height) {
                    if (grid[y][x].type == CellType.SLIDING_OBSTACLE) {
                        grid[y][x] = Cell(CellType.EMPTY)
                    }
                }
            }
        }
        
        return grid
    }
    
    private fun placeSlidingObstacleInGrid(grid: Array<Array<Cell>>, obstacle: SlidingObstacle): Array<Array<Cell>> {
        val centerX = obstacle.x.toInt()
        val centerY = obstacle.y
        val size = obstacle.size
        
        for (dy in -size/2..size/2) {
            for (dx in -size/2..size/2) {
                val x = centerX + dx
                val y = centerY + dy
                if (x in 0 until width && y in 0 until height) {
                    if (grid[y][x].type == CellType.EMPTY) {
                        grid[y][x] = Cell(CellType.SLIDING_OBSTACLE, null, obstacle)
                    }
                }
            }
        }
        
        return grid
    }
    
    private fun updateSettledParticles(grid: Array<Array<Cell>>, obstacle: SlidingObstacle, updatedObstacle: SlidingObstacle): Array<Array<Cell>> {
        // Calculate the delta (movement difference) between old and new obstacle positions
        val deltaX = (updatedObstacle.x - obstacle.x).roundToInt()
        val deltaY = updatedObstacle.y - obstacle.y

        // Only process if there's actual movement
        if (deltaX == 0 && deltaY == 0) return grid

        // Filter settled particles that belong to this obstacle
        val particlesToMove = gridState.getSettledParticles().mapNotNull { settledParticle ->
            val cell = grid[settledParticle.y][settledParticle.x]
            if (cell.type == CellType.SAND && 
                cell.particle?.isSettled == true &&
                cell.particle.obstacleId == obstacle.id) {
                Triple(settledParticle.x, settledParticle.y, cell.particle)
            } else {
                null
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
                movedParticles.add(ParticlePosition(newX, newY))
            }
        }
        
        // Update settled particle position tracking
        for ((oldX, oldY, _) in particlesToMove) {
            gridState.removeSettledParticle(oldX, oldY)
        }
        
        for (movedParticle in movedParticles) {
            gridState.addSettledParticle(movedParticle)
        }

        Log.d("SlidingObstacle", "🏃 Moved ${movedParticles.size}/${particlesToMove.size} settled particles with obstacle ${obstacle.id}")
        
        return grid
    }
}