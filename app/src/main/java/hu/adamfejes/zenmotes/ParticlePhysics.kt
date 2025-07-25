package hu.adamfejes.zenmotes

import kotlin.random.Random

class ParticlePhysics(
    private val width: Int,
    private val height: Int,
    private val nonSettleZoneHeight: Int
) {
    private val gravity = 0.8f
    private val terminalVelocity = 15f
    
    fun createSandParticle(colorType: ObstacleColorType, currentTime: Long): SandParticle {
        // Add slight random variation to initial velocity for more natural sprinkling
        val randomVelocity = 0.01f + Random.nextFloat() * 0.05f
        
        // Add random noise variation - 8% chance for darker particles
        val noiseVariation = if (Random.nextFloat() < 0.08f) {
            0.8f + Random.nextFloat() * 0.1f // Range: 0.8 - 0.9 (slightly darker)
        } else {
            1f // Normal brightness
        }
        
        return SandParticle(
            colorType = colorType,
            isActive = true,
            velocityY = randomVelocity,
            lastUpdateTime = currentTime,
            noiseVariation = noiseVariation
        )
    }
    
    fun tryMoveSandWithGravity(
        x: Int,
        y: Int,
        particle: SandParticle,
        newGrid: Array<Array<Cell>>,
        currentTime: Long
    ): MovementResult {
        // Update velocity with gravity
        val newVelocityY = (particle.velocityY + gravity).coerceAtMost(terminalVelocity)
        
        // Calculate how many cells to move based on velocity
        val cellsToMove = calculateCellsToMove(newVelocityY)
        
        // Try direct downward movement first
        val downwardResult = tryDirectDownwardMovement(x, y, particle, newGrid, currentTime, cellsToMove, newVelocityY)
        if (downwardResult.moved) return downwardResult
        
        // Try diagonal movement
        val diagonalResult = tryDiagonalMovement(x, y, particle, newGrid, currentTime, cellsToMove, newVelocityY)
        if (diagonalResult.moved) return diagonalResult
        
        // Try multi-step diagonal for fast particles
        val multiStepResult = tryMultiStepDiagonal(x, y, particle, newGrid, currentTime, cellsToMove, newVelocityY)
        if (multiStepResult.moved) return multiStepResult


        // Particle cannot move - settle it
        val obstacleId = determineObstacleId(x, y, newGrid)
        return settleParticle(x, y, particle, newGrid, currentTime, obstacleId)
    }
    
    private fun calculateCellsToMove(velocityY: Float): Int {
        return when {
            velocityY < 0.5f -> 0  // Very slow - no movement yet
            velocityY < 1.5f -> 1  // Slow - move 1 cell
            velocityY < 3f -> 2    // Medium - move 2 cells
            else -> velocityY.toInt().coerceAtMost(6)  // Fast - move multiple cells
        }
    }
    
    private fun tryDirectDownwardMovement(
        x: Int,
        y: Int,
        particle: SandParticle,
        newGrid: Array<Array<Cell>>,
        currentTime: Long,
        cellsToMove: Int,
        newVelocityY: Float
    ): MovementResult {
        if (cellsToMove <= 0) return MovementResult(false, null)
        
        // Find the furthest position we can move down
        var finalY = y
        for (step in 1..cellsToMove) {
            val targetY = y + step
            if (targetY < height && canMoveToPosition(x, targetY, newGrid)) {
                finalY = targetY
            } else {
                break
            }
        }
        
        // If we can move down, do it
        if (finalY > y) {
            newGrid[y][x] = Cell()
            
            // If sand buildup is disabled and particle reaches bottom, remove it
            if (finalY >= height - 3) {
                return MovementResult(true, null) // Particle falls out of screen
            }
            
            val updatedParticle = particle.copy(
                velocityY = newVelocityY,
                lastUpdateTime = currentTime
            )
            newGrid[finalY][x] = Cell(CellType.SAND, updatedParticle)
            return MovementResult(true, MovingParticle(x, finalY, updatedParticle))
        }
        
        return MovementResult(false, null)
    }
    
    private fun tryDiagonalMovement(
        x: Int,
        y: Int,
        particle: SandParticle,
        newGrid: Array<Array<Cell>>,
        currentTime: Long,
        cellsToMove: Int,
        newVelocityY: Float
    ): MovementResult {
        val directions = listOf(-1, 1).shuffled() // Randomize left/right preference
        
        for (dx in directions) {
            // Try simple diagonal movement first (just 1 step down)
            val newX = x + dx
            val newY = y + 1
            if (newX in 0 until width && newY < height && canMoveToPosition(newX, newY, newGrid)) {
                newGrid[y][x] = Cell()
                
                // If sand buildup is disabled and particle reaches bottom, remove it
                if (newY >= height - 3) {
                    return MovementResult(true, null) // Particle falls out of screen
                }
                
                val updatedParticle = particle.copy(
                    velocityY = newVelocityY * 0.8f, // Keep most velocity when sliding
                    lastUpdateTime = currentTime
                )
                newGrid[newY][newX] = Cell(CellType.SAND, updatedParticle)
                return MovementResult(true, MovingParticle(newX, newY, updatedParticle))
            }
        }
        
        return MovementResult(false, null)
    }
    
    private fun tryMultiStepDiagonal(
        x: Int,
        y: Int,
        particle: SandParticle,
        newGrid: Array<Array<Cell>>,
        currentTime: Long,
        cellsToMove: Int,
        newVelocityY: Float
    ): MovementResult {
        if (cellsToMove <= 2) return MovementResult(false, null)
        
        val directions = listOf(-1, 1).shuffled()
        
        for (dx in directions) {
            for (step in 2..(cellsToMove / 2).coerceAtLeast(1)) {
                val newX = x + dx
                val newY = y + step
                if (newX in 0 until width && newY < height && canMoveToPosition(newX, newY, newGrid)) {
                    newGrid[y][x] = Cell()
                    
                    // If sand buildup is disabled and particle reaches bottom, remove it
                    if (newY >= height - 3) {
                        return MovementResult(true, null) // Particle falls out of screen
                    }
                    
                    val updatedParticle = particle.copy(
                        velocityY = newVelocityY * 0.7f, // Reduce velocity more for multi-step slides
                        lastUpdateTime = currentTime
                    )
                    newGrid[newY][newX] = Cell(CellType.SAND, updatedParticle)
                    return MovementResult(true, MovingParticle(newX, newY, updatedParticle))
                }
            }
        }
        
        return MovementResult(false, null)
    }
    
    private fun settleParticle(
        x: Int,
        y: Int,
        particle: SandParticle,
        newGrid: Array<Array<Cell>>,
        currentTime: Long,
        obstacleId: String?
    ): MovementResult {
        // If sand buildup is disabled and particle reaches bottom, remove it
        if (y >= height - 3) {
            newGrid[y][x] = Cell() // Remove particle
            return MovementResult(true, null)
        }
        
        // Check if particle should settle
        val isSurrounded = checkIfSurrounded(x, y, newGrid)
        val isInNonSettleZone = y < nonSettleZoneHeight
        
        val stoppedParticle = particle.copy(
            velocityY = 0f,
            lastUpdateTime = currentTime,
            isSettled = (obstacleId != null || isSurrounded) && !isInNonSettleZone && !particle.used,
            obstacleId = obstacleId
        )
        newGrid[y][x] = Cell(CellType.SAND, stoppedParticle)
        
        return MovementResult(
            moved = false,
            newParticle = if (stoppedParticle.isSettled) null else MovingParticle(x, y, stoppedParticle),
            isSettled = stoppedParticle.isSettled,
            settledPosition = if (stoppedParticle.isSettled) ParticlePosition(x, y, obstacleId) else null
        )
    }
    
    private fun canMoveToPosition(x: Int, y: Int, newGrid: Array<Array<Cell>>): Boolean {
        return newGrid[y][x].type == CellType.EMPTY
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
    
    private fun determineObstacleId(x: Int, y: Int, grid: Array<Array<Cell>>): String? {
        // Check the cell directly below first
        val belowY = y + 1
        if (belowY < height) {
            val cellBelow = grid[belowY][x]
            
            // If there's a sliding obstacle below, use its ID
            if (cellBelow.type == CellType.SLIDING_OBSTACLE) {
                return cellBelow.slidingObstacle?.id
            }
            
            // If there's a sand particle below, inherit its obstacle ID
            if (cellBelow.type == CellType.SAND) {
                return cellBelow.particle?.obstacleId
            }
        }
        
        // Check current cell if it's a sliding obstacle
        val currentCell = grid[y][x]
        if (currentCell.type == CellType.SLIDING_OBSTACLE) {
            return currentCell.slidingObstacle?.id
        }
        
        // No obstacle found
        return null
    }
    
    data class MovementResult(
        val moved: Boolean,
        val newParticle: MovingParticle?,
        val isSettled: Boolean = false,
        val settledPosition: ParticlePosition? = null
    )
}