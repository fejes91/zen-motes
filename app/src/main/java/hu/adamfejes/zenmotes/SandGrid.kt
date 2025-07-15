package hu.adamfejes.zenmotes

import androidx.compose.ui.graphics.Color
import android.util.Log

class SandGrid(
    private val width: Int,
    private val height: Int,
    allowSandBuildup: Boolean,
    slidingObstacleTransitTimeSeconds: Float = 5.0f
) {
    // Non-settle zone at top 5% of screen to prevent stuck particles
    private val nonSettleZoneHeight = (height * 0.05f).toInt().coerceAtLeast(3)
    
    // Non-obstacle zone at top 15% of screen to prevent obstacles from sitting on top
    private val nonObstacleZoneHeight = (height * 0.15f).toInt().coerceAtLeast(10)
    
    // Separate components for different responsibilities
    private val gridState = GridState(width, height)
    private val obstacleGenerator = ObstacleGenerator(width, height, nonObstacleZoneHeight, slidingObstacleTransitTimeSeconds)
    private val particlePhysics = ParticlePhysics(width, height, allowSandBuildup, nonSettleZoneHeight)
    
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
        
        // Update sliding obstacles
        updateSlidingObstacles(currentTime)
        
        // Create new grid for updates
        val newGrid = gridState.createNewGrid()
        
        // Process moving particles
        processMovingParticles(newGrid, currentTime)
        
        // Update grid state
        gridState.updateGrid(newGrid)
        gridState.clearActiveRegions()
        
        val totalUpdateTime = (System.nanoTime() - updateStartTime) / 1_000_000.0
        Log.d("SandPerf", "Total update: ${totalUpdateTime}ms, Moving particles: ${gridState.getMovingParticles().size}, Settled particles: ${gridState.getSettledParticles().size}")
    }
    
    private fun updateSlidingObstacles(currentTime: Long) {
        // Generate new sliding obstacles if needed
        obstacleGenerator.generateSlidingObstacle(currentTime)?.let { newObstacle ->
            gridState.addSlidingObstacle(newObstacle)
            Log.d("SlidingObstacle", "🎯 Generated sliding obstacle: ${newObstacle.size}x${newObstacle.size} at y=${newObstacle.y}")
        }
        
        // Update existing sliding obstacles
        val currentObstacles = gridState.getSlidingObstacles()
        val updatedObstacles = mutableListOf<SlidingObstacle>()
        
        for (obstacle in currentObstacles) {
            // Clear old obstacle position from grid
            gridState.clearSlidingObstacleFromGrid(obstacle)
            
            // Update obstacle position
            val updatedObstacle = obstacleGenerator.updateObstaclePosition(obstacle)
            
            // Check if obstacle has moved off screen
            if (!obstacleGenerator.isObstacleOffScreen(updatedObstacle)) {
                updatedObstacles.add(updatedObstacle)
                // Place updated obstacle in grid
                gridState.placeSlidingObstacleInGrid(updatedObstacle)
            }
        }
        
        // Update the sliding obstacles list
        gridState.setSlidingObstacles(updatedObstacles)
        
        Log.d("SlidingObstacle", "🚀 Active sliding obstacles: ${updatedObstacles.size}")
    }
    
    private fun processMovingParticles(newGrid: Array<Array<Cell>>, currentTime: Long) {
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
            val currentCell = gridState.getCell(x, y)
            if (currentCell?.type != CellType.SAND || currentCell.particle != particle) {
                continue
            }
            
            val particleStartTime = System.nanoTime()
            val result = particlePhysics.tryMoveSandWithGravity(x, y, particle, newGrid, currentTime)
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
    }
    
    fun getAllCells(): List<Triple<Int, Int, Cell>> = gridState.getAllCells()
    
    fun getWidth() = width
    fun getHeight() = height
}