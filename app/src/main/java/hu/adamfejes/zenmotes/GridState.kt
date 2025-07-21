package hu.adamfejes.zenmotes

class GridState(
    private val width: Int,
    private val height: Int
) {
    private val grid = Array(height) { Array(width) { Cell() } }
    private val activeRegions = mutableSetOf<Pair<Int, Int>>()
    private val movingParticles = mutableListOf<MovingParticle>()
    private val settledParticlesByObstacle = mutableMapOf<String?, MutableSet<ParticlePosition>>()
    private val slidingObstacles = mutableListOf<SlidingObstacle>()
    
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
    
    fun isValidPosition(x: Int, y: Int): Boolean {
        return x in 0 until width && y in 0 until height
    }
    
    fun clearCell(x: Int, y: Int) {
        if (isValidPosition(x, y)) {
            grid[y][x] = Cell(CellType.EMPTY)
        }
    }
    
    fun createNewGrid(): Array<Array<Cell>> {
        val newGrid = Array(height) { Array(width) { Cell() } }
        // Copy current state
        for (y in 0 until height) {
            for (x in 0 until width) {
                newGrid[y][x] = grid[y][x]
            }
        }
        return newGrid
    }
    
    fun updateGrid(newGrid: Array<Array<Cell>>) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                grid[y][x] = newGrid[y][x]
            }
        }
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
    
    // Moving particles management
    fun addMovingParticle(particle: MovingParticle) {
        movingParticles.add(particle)
    }
    
    fun getMovingParticles(): List<MovingParticle> = movingParticles.toList()
    
    fun clearMovingParticles() {
        movingParticles.clear()
    }
    
    fun setMovingParticles(particles: List<MovingParticle>) {
        movingParticles.clear()
        movingParticles.addAll(particles)
    }
    
    // Settled particles management
    fun addSettledParticle(position: ParticlePosition) {
        val obstacleId = position.obstacleId
        settledParticlesByObstacle.getOrPut(obstacleId) { mutableSetOf() }.add(position)
    }
    
    fun removeSettledParticle(x: Int, y: Int) {
        settledParticlesByObstacle.values.forEach { particles ->
            particles.removeIf { it.x == x && it.y == y }
        }
        // Clean up empty sets to prevent memory leaks
        settledParticlesByObstacle.entries.removeIf { it.value.isEmpty() }
    }
    
    fun getSettledParticles(): Set<ParticlePosition> {
        return settledParticlesByObstacle.values.flatten().toSet()
    }
    
    fun getSettledParticlesByObstacleId(obstacleId: String): Set<ParticlePosition> {
        return settledParticlesByObstacle[obstacleId]?.toSet() ?: emptySet()
    }
    
    // Sliding obstacles management
    fun addSlidingObstacle(obstacle: SlidingObstacle) {
        slidingObstacles.add(obstacle)
    }
    
    fun getSlidingObstacles(): List<SlidingObstacle> = slidingObstacles.toList()
    
    fun clearSlidingObstacles() {
        slidingObstacles.clear()
    }
    
    fun setSlidingObstacles(obstacles: List<SlidingObstacle>) {
        slidingObstacles.clear()
        slidingObstacles.addAll(obstacles)
    }
    
    // Active regions management
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
    
    fun clearActiveRegions() {
        activeRegions.clear()
    }
    
    fun addActiveRegion(x: Int, y: Int) {
        activeRegions.add(Pair(x, y))
    }
    
    fun getActiveRegions(): Set<Pair<Int, Int>> = activeRegions.toSet()
    
    // Grid operations for obstacles
    fun clearSlidingObstacleFromGrid(obstacle: SlidingObstacle) {
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
    
    fun placeSlidingObstacleInGrid(obstacle: SlidingObstacle) {
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
    
    fun getWidth() = width
    fun getHeight() = height
}