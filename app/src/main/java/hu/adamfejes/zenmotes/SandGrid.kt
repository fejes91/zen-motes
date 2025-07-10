package hu.adamfejes.zenmotes

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

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
    
    init {
        // Add static obstacles
        createMiddleObstacle()
        createVerticalWall()
        // Both angled walls will be created dynamically in updateObstacles()
    }
    
    private fun createMiddleObstacle() {
        val centerX = width / 2
        val centerY = height / 2
        val obstacleWidth = width / 8
        val obstacleHeight = height / 12
        
        // Create a rectangular obstacle in the middle with rounded corners
        for (y in (centerY - obstacleHeight/2)..(centerY + obstacleHeight/2)) {
            for (x in (centerX - obstacleWidth/2)..(centerX + obstacleWidth/2)) {
                if (x in 0 until width && y in 0 until height) {
                    // Check if we're at a corner and apply rounding
                    val dx = x - centerX
                    val dy = y - centerY
                    val cornerRadius = 2f
                    
                    val isInCorner = (kotlin.math.abs(dx) > obstacleWidth/2 - cornerRadius && 
                                     kotlin.math.abs(dy) > obstacleHeight/2 - cornerRadius)
                    
                    if (!isInCorner || 
                        (dx*dx + dy*dy) <= cornerRadius * cornerRadius) {
                        grid[y][x] = Cell(CellType.OBSTACLE)
                    }
                }
            }
        }
    }
    
    private fun createVerticalWall() {
        val centerX = width / 2
        val wallHeight = (height * 0.2f).toInt() // 20% of screen height
        val wallWidth = 2 // Make wall 2 pixels thick for better visibility
        
        // Create vertical wall from bottom up with rounded top
        for (y in (height - wallHeight) until height) {
            for (x in (centerX - wallWidth/2)..(centerX + wallWidth/2)) {
                if (x in 0 until width && y in 0 until height) {
                    // Round the top of the wall
                    val isTop = y == height - wallHeight
                    val dx = x - centerX
                    val cornerRadius = 1f
                    
                    if (!isTop || kotlin.math.abs(dx) <= cornerRadius) {
                        grid[y][x] = Cell(CellType.OBSTACLE)
                    }
                }
            }
        }
    }
    
    private fun updateObstacles() {
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
    
    private fun createRotatingLeftWall() {
        val wallCenterX = (width / 2.3f).toInt()
        val wallCenterY = (height / 4.8f).toInt()
        val wallLength = width / 4
        val wallThickness = 2
        
        // Convert rotation angle to radians
        val angleRad = Math.toRadians(rotationAngle.toDouble())
        val cosAngle = kotlin.math.cos(angleRad)
        val sinAngle = kotlin.math.sin(angleRad)
        
        // Create rotating wall around center point
        for (i in -wallLength/2 until wallLength/2) {
            // Calculate point along the wall line
            val localX = i.toDouble()
            val localY = 0.0
            
            // Rotate the point
            val rotatedX = localX * cosAngle - localY * sinAngle
            val rotatedY = localX * sinAngle + localY * cosAngle
            
            // Translate to world position
            val worldX = wallCenterX + rotatedX.toInt()
            val worldY = wallCenterY + rotatedY.toInt()
            
            // Add thickness around the line
            for (thickX in -wallThickness/2..wallThickness/2) {
                for (thickY in -wallThickness/2..wallThickness/2) {
                    val x = worldX + thickX
                    val y = worldY + thickY
                    
                    if (x in 0 until width && y in 0 until height) {
                        // Round the ends
                        val distFromEnd = kotlin.math.abs(i)
                        val isNearEnd = distFromEnd > wallLength/2 - 3
                        val distFromThickCenter = kotlin.math.sqrt((thickX * thickX + thickY * thickY).toDouble())
                        
                        if (!isNearEnd || distFromThickCenter <= 1.5) {
                            // Only place if not already occupied by static obstacle
                            if (grid[y][x].type == CellType.EMPTY) {
                                grid[y][x] = Cell(CellType.ROTATING_OBSTACLE)
                            }
                        }
                    }
                }
            }
        }
    }
    
    
    private fun createRotatingRightWall() {
        val wallCenterX = (width / 1.7f).toInt()
        val wallCenterY = (height / 2.8f).toInt()
        val wallLength = width / 3 // Make it similar to the old static wall
        val wallThickness = 2
        
        // Convert rotation angle to radians (rotate in opposite direction for variety)
        val angleRad = Math.toRadians((-rotationAngle).toDouble())
        val cosAngle = kotlin.math.cos(angleRad)
        val sinAngle = kotlin.math.sin(angleRad)
        
        // Create rotating wall around center point
        for (i in -wallLength/2 until wallLength/2) {
            // Calculate point along the wall line
            val localX = i.toDouble()
            val localY = 0.0
            
            // Rotate the point
            val rotatedX = localX * cosAngle - localY * sinAngle
            val rotatedY = localX * sinAngle + localY * cosAngle
            
            // Translate to world position
            val worldX = wallCenterX + rotatedX.toInt()
            val worldY = wallCenterY + rotatedY.toInt()
            
            // Add thickness around the line
            for (thickX in -wallThickness/2..wallThickness/2) {
                for (thickY in -wallThickness/2..wallThickness/2) {
                    val x = worldX + thickX
                    val y = worldY + thickY
                    
                    if (x in 0 until width && y in 0 until height) {
                        // Round the ends
                        val distFromEnd = kotlin.math.abs(i)
                        val isNearEnd = distFromEnd > wallLength/2 - 3
                        val distFromThickCenter = kotlin.math.sqrt((thickX * thickX + thickY * thickY).toDouble())
                        
                        if (!isNearEnd || distFromThickCenter <= 1.5) {
                            // Only place if not already occupied by static obstacle
                            if (grid[y][x].type == CellType.EMPTY) {
                                grid[y][x] = Cell(CellType.ROTATING_OBSTACLE)
                            }
                        }
                    }
                }
            }
        }
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
        }
    }
    
    fun update(currentTime: Long = System.currentTimeMillis()) {
        // Update rotation angle
        rotationAngle += rotationSpeed
        if (rotationAngle >= 360f) rotationAngle -= 360f
        
        // Update dynamic obstacles
        updateObstacles()
        
        val newGrid = Array(height) { Array(width) { Cell() } }
        val newActiveRegions = mutableSetOf<Pair<Int, Int>>()
        
        // Copy current state
        for (y in 0 until height) {
            for (x in 0 until width) {
                newGrid[y][x] = grid[y][x]
            }
        }
        
        // Update sand particles from bottom to top, randomize x order to fix asymmetry
        for (y in height - 2 downTo 0) {
            val xIndices = (0 until width).shuffled()
            for (x in xIndices) {
                val cell = grid[y][x]
                if (cell.type == CellType.SAND && cell.particle != null) {
                    val moved = tryMoveSandWithGravity(x, y, newGrid, currentTime)
                    if (moved) {
                        newActiveRegions.add(Pair(x, y))
                    }
                }
            }
        }
        
        // Update grid and active regions
        for (y in 0 until height) {
            for (x in 0 until width) {
                grid[y][x] = newGrid[y][x]
            }
        }
        
        activeRegions.clear()
        activeRegions.addAll(newActiveRegions)
    }
    
    private fun tryMoveSandWithGravity(x: Int, y: Int, newGrid: Array<Array<Cell>>, currentTime: Long): Boolean {
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
                val updatedParticle = particle.copy(
                    velocityY = newVelocityY,
                    lastUpdateTime = currentTime
                )
                newGrid[finalY][x] = Cell(CellType.SAND, updatedParticle)
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
                val updatedParticle = particle.copy(
                    velocityY = newVelocityY * 0.8f, // Keep most velocity when sliding
                    lastUpdateTime = currentTime
                )
                newGrid[newY][newX] = Cell(CellType.SAND, updatedParticle)
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
                        val updatedParticle = particle.copy(
                            velocityY = newVelocityY * 0.7f, // Reduce velocity more for multi-step slides
                            lastUpdateTime = currentTime
                        )
                        newGrid[newY][newX] = Cell(CellType.SAND, updatedParticle)
                        return true
                    }
                }
            }
        }
        
        // If no movement possible, reset velocity
        val stoppedParticle = particle.copy(
            velocityY = 0f,
            lastUpdateTime = currentTime
        )
        newGrid[y][x] = Cell(CellType.SAND, stoppedParticle)
        return false
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
}