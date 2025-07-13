package hu.adamfejes.zenmotes

import androidx.compose.ui.graphics.Color
import kotlin.random.Random
import android.util.Log

class SandGrid(
    private val width: Int,
    private val height: Int
) {
    private val gravity = 0.8f
    private val maxVelocity = 30f
    private val terminalVelocity = 15f
    private val grid = Array(height) { Array(width) { Cell() } }
    private val activeRegions = mutableSetOf<Pair<Int, Int>>()
    private var rotationAngle = 0f
    private val rotationSpeed = 0.5f // degrees per frame
    
    // Separate collections for performance optimization
    private val movingParticles = mutableListOf<Triple<Int, Int, SandParticle>>()
    private val settledParticles = mutableSetOf<Pair<Int, Int>>()
    
    // Non-settle zone at top 5% of screen to prevent stuck particles
    private val nonSettleZoneHeight = (height * 0.05f).toInt().coerceAtLeast(3)
    
    // Control whether sand builds up or falls through the screen
    private var allowSandBuildup = true
    
    init {
        // Temporarily removing obstacles
        // createMiddleObstacle()
        // createVerticalWall()
        // Both angled walls will be created dynamically in updateObstacles()
    }
    
    private fun createMiddleObstacle() {
        createRoundedRectangle(
            centerX = width / 2,
            centerY = height / 2,
            width = width / 8,
            height = height / 12,
            cornerRadius = 2f
        )
    }
    
    private fun createVerticalWall() {
        createRoundedRectangle(
            centerX = width / 2,
            centerY = height - (height * 0.1f).toInt(),
            width = 2,
            height = (height * 0.2f).toInt(),
            cornerRadius = 1f
        )
    }
    
    private fun createRoundedRectangle(centerX: Int, centerY: Int, width: Int, height: Int, cornerRadius: Float) {
        for (y in (centerY - height/2)..(centerY + height/2)) {
            for (x in (centerX - width/2)..(centerX + width/2)) {
                if (isValidPosition(x, y)) {
                    val dx = x - centerX
                    val dy = y - centerY
                    
                    val isInCorner = (kotlin.math.abs(dx) > width/2 - cornerRadius && 
                                     kotlin.math.abs(dy) > height/2 - cornerRadius)
                    
                    if (!isInCorner || (dx*dx + dy*dy) <= cornerRadius * cornerRadius) {
                        grid[y][x] = Cell(CellType.OBSTACLE)
                    }
                }
            }
        }
    }
    
    private fun updateObstacles() {
        // Unsettle sand particles near rotating obstacles before clearing
        unsettleSandNearRotatingObstacles()
        
        // Clear previous rotating obstacles only
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (grid[y][x].type == CellType.ROTATING_OBSTACLE) {
                    grid[y][x] = Cell(CellType.EMPTY)
                }
            }
        }
        
        // Create rotating walls
        createRotatingLeftWall()
        createRotatingRightWall()
    }
    
    private fun unsettleSandNearRotatingObstacles() {
        val leftCenterX = (width / 2.3f).toInt()
        val leftCenterY = (height / 4.8f).toInt()
        val rightCenterX = (width / 1.7f).toInt()
        val rightCenterY = (height / 2.8f).toInt()
        val radius = width / 3 // Area around rotating obstacles
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val cell = grid[y][x]
                if (cell.type == CellType.SAND && cell.particle?.isSettled == true) {
                    // Check if near any rotating obstacle
                    val distToLeft = kotlin.math.sqrt(
                        ((x - leftCenterX) * (x - leftCenterX) + (y - leftCenterY) * (y - leftCenterY)).toDouble()
                    )
                    val distToRight = kotlin.math.sqrt(
                        ((x - rightCenterX) * (x - rightCenterX) + (y - rightCenterY) * (y - rightCenterY)).toDouble()
                    )
                    
                    if (distToLeft < radius || distToRight < radius) {
                        // Unsettle the particle
                        val unsettledParticle = cell.particle.copy(isSettled = false)
                        grid[y][x] = Cell(CellType.SAND, unsettledParticle)
                        // Move from settled to moving list
                        settledParticles.removeIf { it.first == x && it.second == y }
                        movingParticles.add(Triple(x, y, unsettledParticle))
                    }
                }
            }
        }
    }
    
    private fun createRotatingLeftWall() {
        createRotatingWall(
            centerX = (width / 2.3f).toInt(),
            centerY = (height / 4.8f).toInt(),
            length = width / 4,
            angle = rotationAngle
        )
    }
    
    private fun createRotatingRightWall() {
        createRotatingWall(
            centerX = (width / 1.7f).toInt(),
            centerY = (height / 2.8f).toInt(),
            length = width / 3,
            angle = -rotationAngle
        )
    }
    
    private fun createRotatingWall(centerX: Int, centerY: Int, length: Int, angle: Float) {
        val thickness = 2
        val angleRad = Math.toRadians(angle.toDouble())
        val cosAngle = kotlin.math.cos(angleRad)
        val sinAngle = kotlin.math.sin(angleRad)
        
        for (i in -length/2 until length/2) {
            val localX = i.toDouble()
            val rotatedX = localX * cosAngle
            val rotatedY = localX * sinAngle
            
            val worldX = centerX + rotatedX.toInt()
            val worldY = centerY + rotatedY.toInt()
            
            for (thickX in -thickness/2..thickness/2) {
                for (thickY in -thickness/2..thickness/2) {
                    val x = worldX + thickX
                    val y = worldY + thickY
                    
                    if (isValidPosition(x, y) && shouldPlaceWallPixel(i, length, thickX, thickY)) {
                        if (grid[y][x].type == CellType.EMPTY) {
                            grid[y][x] = Cell(CellType.ROTATING_OBSTACLE)
                        }
                    }
                }
            }
        }
    }
    
    private fun shouldPlaceWallPixel(i: Int, length: Int, thickX: Int, thickY: Int): Boolean {
        val distFromEnd = kotlin.math.abs(i)
        val isNearEnd = distFromEnd > length/2 - 3
        val distFromThickCenter = kotlin.math.sqrt((thickX * thickX + thickY * thickY).toDouble())
        return !isNearEnd || distFromThickCenter <= 1.5
    }
    
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
            movingParticles.add(Triple(x, y, particle))
        }
    }
    
    fun update(currentTime: Long = System.currentTimeMillis()) {
        val updateStartTime = System.nanoTime()
        
        // Update rotation angle
        rotationAngle += rotationSpeed
        if (rotationAngle >= 360f) rotationAngle -= 360f
        
        // Temporarily removing dynamic obstacles
        // val obstacleStartTime = System.nanoTime()
        // updateObstacles()
        // val obstacleTime = (System.nanoTime() - obstacleStartTime) / 1_000_000.0
        // Log.d("SandPerf", "Obstacle update: ${obstacleTime}ms")
        
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
        val newMovingParticles = mutableListOf<Triple<Int, Int, SandParticle>>()
        
        // Process moving particles in random order to avoid asymmetry
        val shuffleStartTime = System.nanoTime()
        val shuffledMoving = movingParticles.shuffled()
        val shuffleTime = (System.nanoTime() - shuffleStartTime) / 1_000_000.0
        
        var collisionTime = 0.0
        var movementTime = 0.0
        var particleCount = 0
        
        for ((x, y, particle) in shuffledMoving) {
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
    
    private fun tryMoveSandWithGravity(x: Int, y: Int, newGrid: Array<Array<Cell>>, currentTime: Long, newMovingParticles: MutableList<Triple<Int, Int, SandParticle>>): Boolean {
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
                newMovingParticles.add(Triple(x, finalY, updatedParticle))
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
                newMovingParticles.add(Triple(newX, newY, updatedParticle))
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
                        newMovingParticles.add(Triple(newX, newY, updatedParticle))
                        return true
                    }
                }
            }
        }
        
        // If no movement possible, mark as settled only if truly stable and not near rotating obstacles
        val isAtBottom = y >= height - 2
        val isSurrounded = checkIfSurrounded(x, y, newGrid)
        val isNearRotatingObstacle = checkIfNearRotatingObstacle(x, y)
        val isInNonSettleZone = y < nonSettleZoneHeight
        
        // If sand buildup is disabled and particle reaches bottom, remove it
        if (!allowSandBuildup && y >= height - 3) {
            newGrid[y][x] = Cell() // Remove particle
            return true
        }
        
        val stoppedParticle = particle.copy(
            velocityY = 0f,
            lastUpdateTime = currentTime,
            isSettled = allowSandBuildup && (isAtBottom || isSurrounded) && !isNearRotatingObstacle && !isInNonSettleZone
        )
        newGrid[y][x] = Cell(CellType.SAND, stoppedParticle)
        
        // If particle is truly settled, add to settled list, otherwise keep in moving list
        if (stoppedParticle.isSettled) {
            settledParticles.add(Pair(x, y))
        } else {
            newMovingParticles.add(Triple(x, y, stoppedParticle))
        }
        return false
    }
    
    private fun checkIfNearRotatingObstacle(x: Int, y: Int): Boolean {
        val leftCenterX = (width / 2.3f).toInt()
        val leftCenterY = (height / 4.8f).toInt()
        val rightCenterX = (width / 1.7f).toInt()
        val rightCenterY = (height / 2.8f).toInt()
        val radius = width / 4 // Smaller radius for settling check
        
        val distToLeft = kotlin.math.sqrt(
            ((x - leftCenterX) * (x - leftCenterX) + (y - leftCenterY) * (y - leftCenterY)).toDouble()
        )
        val distToRight = kotlin.math.sqrt(
            ((x - rightCenterX) * (x - rightCenterX) + (y - rightCenterY) * (y - rightCenterY)).toDouble()
        )
        
        return distToLeft < radius || distToRight < radius
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
                    grid[y][x].type == CellType.ROTATING_OBSTACLE) {
                    cells.add(Triple(x, y, grid[y][x]))
                }
            }
        }
        return cells
    }
    
    fun getWidth() = width
    fun getHeight() = height
    
    fun setAllowSandBuildup(allow: Boolean) {
        allowSandBuildup = allow
    }
}