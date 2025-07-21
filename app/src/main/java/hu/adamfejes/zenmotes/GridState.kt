package hu.adamfejes.zenmotes

class GridState(
    private val width: Int,
    private val height: Int
) {
    private val grid = Array(height) { Array(width) { Cell() } }
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
        }
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
    
    fun setSlidingObstacles(obstacles: List<SlidingObstacle>) {
        slidingObstacles.clear()
        slidingObstacles.addAll(obstacles)
    }

    fun getGrid(): Array<Array<Cell>> {
        return grid
    }
}