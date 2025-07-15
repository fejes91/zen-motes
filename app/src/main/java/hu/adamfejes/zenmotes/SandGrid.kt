package hu.adamfejes.zenmotes

import androidx.compose.ui.graphics.Color
import kotlin.random.Random
import android.util.Log

class SandGrid(
    private val width: Int,
    private val height: Int,
    private val allowSandBuildup: Boolean = true, // If false, sand will fall through the bottom without building up
    private val slidingObstacleTransitTimeSeconds: Float = 5.0f // Time in seconds for obstacle to cross screen
) {
    private val gravity = 0.8f
    private val maxVelocity = 30f
    private val terminalVelocity = 15f
    private val grid = Array(height) { Array(width) { Cell() } }
    private val activeRegions = mutableSetOf<Pair<Int, Int>>()
    
    // Separate collections for performance optimization
    private val movingParticles = mutableListOf<MovingParticle>()
    private val settledParticles = mutableSetOf<ParticlePosition>()
    
    // Non-settle zone at top 5% of screen to prevent stuck particles
    private val nonSettleZoneHeight = (height * 0.05f).toInt().coerceAtLeast(3)
    
    // Non-obstacle zone at top 15% of screen to prevent obstacles from sitting on top
    private val nonObstacleZoneHeight = (height * 0.15f).toInt().coerceAtLeast(10)
    
    
    // Sliding obstacles
    private val slidingObstacles = mutableListOf<SlidingObstacle>()
    private var lastSlidingObstacleTime = 0L
    private val slidingObstacleInterval = 3000L // 3 seconds between obstacles
    
    // Calculate sliding speed based on transit time (assumes 60 FPS)
    private val slidingSpeed = width / (slidingObstacleTransitTimeSeconds * 60f)
    
    
    
    
    
    
    private fun isValidPosition(x: Int, y: Int): Boolean {
        return x in 0 until width && y in 0 until height
    }
    
    fun getCell(x: Int, y: Int): Cell? {
        return if (x in 0 until width && y in 0 until height) {
            grid[y][x]
        } else null
    }
    
    fun setCell(x: Int, y: Int, cell: Cell) {
        if (x in 0 until width && y in 0 until height) {
            grid[y][x] = cell
            if (cell.type == CellType.SAND) {
                markActiveRegion(x, y)
            }
        }
    }
    
    private fun markActiveRegion(x: Int, y: Int) {
        for (dy in -1..1) {
            for (dx in -1..1) {
                val nx = x + dx
                val ny = y + dy
                if (nx in 0 until width && ny in 0 until height) {
                    activeRegions.add(Pair(nx, ny))
                }
            }
        }
    }
    
    fun addSand(x: Int, y: Int, color: Color, currentTime: Long = System.currentTimeMillis()) {
        if (x in 0 until width && y in 0 until height && grid[y][x].type == CellType.EMPTY) {
            // Add slight random variation to initial velocity for more natural sprinkling
            val randomVelocity = 0.01f + Random.nextFloat() * 0.05f
            
            // Add random noise variation - 8% chance for darker particles
            val noiseVariation = if (Random.nextFloat() < 0.08f) {
                0.8f + Random.nextFloat() * 0.1f // Range: 0.8 - 0.9 (slightly darker, max darkness is old minimum)
            } else {
                1f // Normal brightness
            }
            
            val particle = SandParticle(
                color = color, 
                isActive = true, 
                velocityY = randomVelocity, 
                lastUpdateTime = currentTime,
                noiseVariation = noiseVariation
            )
            setCell(x, y, Cell(CellType.SAND, particle))
            // Add to moving particles list for performance optimization
            movingParticles.add(MovingParticle(x, y, particle))
        }
    }
    
    fun update(currentTime: Long = System.currentTimeMillis()) {
        val updateStartTime = System.nanoTime()
        
        // Update sliding obstacles
        updateSlidingObstacles(currentTime)
        
        val newGrid = Array(height) { Array(width) { Cell() } }
        val newActiveRegions = mutableSetOf<Pair<Int, Int>>()
        
        // Copy current state
        for (y in 0 until height) {
            for (x in 0 until width) {
                newGrid[y][x] = grid[y][x]
            }
        }
        
        // Only process moving particles for massive performance gain
        val physicsStartTime = System.nanoTime()
        val newMovingParticles = mutableListOf<MovingParticle>()
        
        // Process moving particles in random order to avoid asymmetry
        val shuffleStartTime = System.nanoTime()
        val shuffledMoving = movingParticles.shuffled()
        val shuffleTime = (System.nanoTime() - shuffleStartTime) / 1_000_000.0
        
        var collisionTime = 0.0
        var movementTime = 0.0
        var particleCount = 0
        
        for (movingParticle in shuffledMoving) {
            val (x, y, particle) = movingParticle
            // Skip if position has been overwritten by obstacle update
            if (grid[y][x].type != CellType.SAND || grid[y][x].particle != particle) {
                continue
            }
            
            val particleStartTime = System.nanoTime()
            val moved = tryMoveSandWithGravity(x, y, newGrid, currentTime, newMovingParticles)
            val particleTime = (System.nanoTime() - particleStartTime) / 1_000_000.0
            
            if (moved) {
                movementTime += particleTime
                newActiveRegions.add(Pair(x, y))
            } else {
                collisionTime += particleTime
            }
            particleCount++
        }
        
        // Update moving particles list
        movingParticles.clear()
        movingParticles.addAll(newMovingParticles)
        
        val physicsTime = (System.nanoTime() - physicsStartTime) / 1_000_000.0
        
        Log.d("SandPerf", "Shuffle: ${shuffleTime}ms, Movement: ${movementTime}ms, Collision: ${collisionTime}ms, Total Physics: ${physicsTime}ms, Particles: $particleCount")
        
        // Update grid and active regions
        for (y in 0 until height) {
            for (x in 0 until width) {
                grid[y][x] = newGrid[y][x]
            }
        }
        
        activeRegions.clear()
        activeRegions.addAll(newActiveRegions)
        
        val totalUpdateTime = (System.nanoTime() - updateStartTime) / 1_000_000.0
        Log.d("SandPerf", "Total update: ${totalUpdateTime}ms, Moving particles: ${movingParticles.size}, Settled particles: ${settledParticles.size}")
    }
    
    private fun tryMoveSandWithGravity(x: Int, y: Int, newGrid: Array<Array<Cell>>, currentTime: Long, newMovingParticles: MutableList<MovingParticle>): Boolean {
        val cell = grid[y][x]
        val particle = cell.particle ?: return false
        
        // Update velocity with gravity (simplified - no time delta)
        val newVelocityY = (particle.velocityY + gravity).coerceAtMost(terminalVelocity)
        
        // Calculate how many cells to move based on velocity
        val cellsToMove = when {
            newVelocityY < 0.5f -> 0  // Very slow - no movement yet
            newVelocityY < 1.5f -> 1  // Slow - move 1 cell
            newVelocityY < 3f -> 2    // Medium - move 2 cells
            else -> newVelocityY.toInt().coerceAtMost(6)  // Fast - move multiple cells
        }
        
        // Handle movement based on calculated cells to move
        if (cellsToMove > 0) {
            // Find the furthest position we can move down
            var finalY = y
            for (step in 1..cellsToMove) {
                val targetY = y + step
                if (targetY < height && newGrid[targetY][x].type == CellType.EMPTY) {
                    finalY = targetY
                } else {
                    break
                }
            }
            
            // If we can move down, do it
            if (finalY > y) {
                newGrid[y][x] = Cell()
                
                // If sand buildup is disabled and particle reaches bottom, remove it
                if (!allowSandBuildup && finalY >= height - 3) {
                    return true // Particle falls out of screen
                }
                
                val updatedParticle = particle.copy(
                    velocityY = newVelocityY,
                    lastUpdateTime = currentTime
                )
                newGrid[finalY][x] = Cell(CellType.SAND, updatedParticle)
                newMovingParticles.add(MovingParticle(x, finalY, updatedParticle))
                return true
            }
        }
        
        // If direct fall failed, try diagonal movement with reduced velocity
        val directions = listOf(-1, 1).shuffled() // Randomize left/right preference
        
        for (dx in directions) {
            // Try simple diagonal movement first (just 1 step down)
            val newX = x + dx
            val newY = y + 1
            if (newX in 0 until width && newY < height && newGrid[newY][newX].type == CellType.EMPTY) {
                newGrid[y][x] = Cell()
                
                // If sand buildup is disabled and particle reaches bottom, remove it
                if (!allowSandBuildup && newY >= height - 3) {
                    return true // Particle falls out of screen
                }
                
                val updatedParticle = particle.copy(
                    velocityY = newVelocityY * 0.8f, // Keep most velocity when sliding
                    lastUpdateTime = currentTime
                )
                newGrid[newY][newX] = Cell(CellType.SAND, updatedParticle)
                newMovingParticles.add(MovingParticle(newX, newY, updatedParticle))
                return true
            }
        }
        
        // If single diagonal failed, try multiple steps for fast particles
        if (cellsToMove > 2) {
            for (dx in directions) {
                for (step in 2..(cellsToMove / 2).coerceAtLeast(1)) {
                    val newX = x + dx
                    val newY = y + step
                    if (newX in 0 until width && newY < height && newGrid[newY][newX].type == CellType.EMPTY) {
                        newGrid[y][x] = Cell()
                        
                        // If sand buildup is disabled and particle reaches bottom, remove it
                        if (!allowSandBuildup && newY >= height - 3) {
                            return true // Particle falls out of screen
                        }
                        
                        val updatedParticle = particle.copy(
                            velocityY = newVelocityY * 0.7f, // Reduce velocity more for multi-step slides
                            lastUpdateTime = currentTime
                        )
                        newGrid[newY][newX] = Cell(CellType.SAND, updatedParticle)
                        newMovingParticles.add(MovingParticle(newX, newY, updatedParticle))
                        return true
                    }
                }
            }
        }
        
        // If no movement possible, mark as settled only if truly stable and not near rotating obstacles
        val isAtBottom = y >= height - 2
        val isSurrounded = checkIfSurrounded(x, y, newGrid)
        val isInNonSettleZone = y < nonSettleZoneHeight
        
        // If sand buildup is disabled and particle reaches bottom, remove it
        if (!allowSandBuildup && y >= height - 3) {
            newGrid[y][x] = Cell() // Remove particle
            return true
        }
        
        val stoppedParticle = particle.copy(
            velocityY = 0f,
            lastUpdateTime = currentTime,
            isSettled = allowSandBuildup && (isAtBottom || isSurrounded) && !isInNonSettleZone
        )
        newGrid[y][x] = Cell(CellType.SAND, stoppedParticle)
        
        // If particle is truly settled, add to settled list, otherwise keep in moving list
        if (stoppedParticle.isSettled) {
            settledParticles.add(ParticlePosition(x, y))
        } else {
            newMovingParticles.add(MovingParticle(x, y, stoppedParticle))
        }
        return false
    }
    
    
    private fun checkIfSurrounded(x: Int, y: Int, grid: Array<Array<Cell>>): Boolean {
        // Check if particle is surrounded by other sand or obstacles
        for (dy in -1..1) {
            for (dx in -1..1) {
                if (dx == 0 && dy == 0) continue
                val checkX = x + dx
                val checkY = y + dy
                if (checkX in 0 until width && checkY in 0 until height) {
                    if (grid[checkY][checkX].type == CellType.EMPTY) {
                        return false
                    }
                } else if (checkY >= height) {
                    // Bottom boundary
                    continue
                } else {
                    // Side boundaries
                    return false
                }
            }
        }
        return true
    }
    
    fun getAllCells(): List<Triple<Int, Int, Cell>> {
        val cells = mutableListOf<Triple<Int, Int, Cell>>()
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (grid[y][x].type == CellType.SAND || 
                    grid[y][x].type == CellType.OBSTACLE || 
                    grid[y][x].type == CellType.SLIDING_OBSTACLE) {
                    cells.add(Triple(x, y, grid[y][x]))
                }
            }
        }
        return cells
    }
    
    fun getWidth() = width
    fun getHeight() = height
    
    
    private fun updateSlidingObstacles(currentTime: Long) {
        // Generate new sliding obstacles if enough time has passed
        if (currentTime - lastSlidingObstacleTime >= slidingObstacleInterval) {
            generateSlidingObstacle()
            lastSlidingObstacleTime = currentTime
        }
        
        // Update existing sliding obstacles
        val obstaclesToRemove = mutableListOf<SlidingObstacle>()
        val updatedObstacles = mutableListOf<SlidingObstacle>()
        
        for (obstacle in slidingObstacles) {
            // Clear old obstacle position from grid
            clearSlidingObstacleFromGrid(obstacle)
            
            // Update obstacle position
            val newX = obstacle.x + slidingSpeed
            
            // Check if obstacle has moved off screen
            if (newX > width + obstacle.size) {
                obstaclesToRemove.add(obstacle)
                continue
            }
            
            // Create updated obstacle
            val updatedObstacle = obstacle.copy(x = newX)
            updatedObstacles.add(updatedObstacle)
            
            // Place updated obstacle in grid
            placeSlidingObstacleInGrid(updatedObstacle)
        }
        
        // Remove obstacles that have moved off screen
        slidingObstacles.removeAll(obstaclesToRemove)
        
        // Update the sliding obstacles list
        slidingObstacles.clear()
        slidingObstacles.addAll(updatedObstacles)
        
        Log.d("SlidingObstacle", "🚀 Active sliding obstacles: ${slidingObstacles.size}")
    }
    
    private fun generateSlidingObstacle() {
        // Generate random Y position avoiding non-obstacle zone
        val minY = nonObstacleZoneHeight + 6 // Add margin
        val maxY = height - 20 // Add margin from bottom
        
        if (minY >= maxY) return // Not enough space to place obstacle
        
        val obstacleY = (minY..maxY).random()
        val obstacleSize = listOf(8, 10, 12, 14, 16).random()
        
        // Sliding obstacle colors (different from destroyable obstacles)
        val slidingColors = listOf(
            Color(0xFF00BCD4), // Cyan
            Color(0xFF4CAF50), // Green
            Color(0xFFFF9800), // Orange
            Color(0xFF9C27B0), // Purple
            Color(0xFFE91E63), // Pink
            Color(0xFF2196F3)  // Blue
        )
        
        val obstacle = SlidingObstacle(
            x = -obstacleSize.toFloat(), // Start just off screen to the left
            y = obstacleY,
            targetX = width.toFloat() + obstacleSize, // Target is off screen to the right
            speed = slidingSpeed,
            size = obstacleSize,
            color = slidingColors.random()
        )
        
        slidingObstacles.add(obstacle)
        Log.d("SlidingObstacle", "🎯 Generated sliding obstacle: ${obstacleSize}x${obstacleSize} at y=$obstacleY")
    }
    
    private fun clearSlidingObstacleFromGrid(obstacle: SlidingObstacle) {
        val halfSize = obstacle.size / 2
        val centerX = obstacle.x.toInt()
        val centerY = obstacle.y
        
        for (dy in -halfSize..halfSize) {
            for (dx in -halfSize..halfSize) {
                val x = centerX + dx
                val y = centerY + dy
                if (isValidPosition(x, y) && grid[y][x].type == CellType.SLIDING_OBSTACLE) {
                    grid[y][x] = Cell(CellType.EMPTY)
                }
            }
        }
    }
    
    private fun placeSlidingObstacleInGrid(obstacle: SlidingObstacle) {
        val halfSize = obstacle.size / 2
        val centerX = obstacle.x.toInt()
        val centerY = obstacle.y
        
        for (dy in -halfSize..halfSize) {
            for (dx in -halfSize..halfSize) {
                val x = centerX + dx
                val y = centerY + dy
                if (isValidPosition(x, y) && grid[y][x].type == CellType.EMPTY) {
                    grid[y][x] = Cell(CellType.SLIDING_OBSTACLE, null, obstacle)
                }
            }
        }
    }
}