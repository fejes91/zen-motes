package hu.adamfejes.zenmotes.logic

class GridState(
    private val width: Int,
    private val height: Int
) {
    private val grid = Array(height) { Array(width) { Cell() } }
    private val movingParticles = mutableListOf<MovingParticle>()
    private val settledParticlesByObstacle = mutableMapOf<String?, MutableSet<ParticlePosition>>()
    private val slidingObstacles = mutableListOf<SlidingObstacle>()

    // Cache active cells to avoid scanning entire grid
    private val activeCells = mutableListOf<Triple<Int, Int, Cell>>()

    fun getCell(x: Int, y: Int): Cell? {
        return if (x in 0 until width && y in 0 until height) {
            grid[y][x]
        } else null
    }

    fun setCell(x: Int, y: Int, cell: Cell) {
        if (x in 0 until width && y in 0 until height) {
            grid.setCell(x, y, cell)
        }
    }

    fun updateGrid(newGrid: Array<Array<Cell>>) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                grid.setCell(x, y, newGrid[y][x])
            }
        }
        rebuildActiveCells()
    }

    fun getAllCells(): List<Triple<Int, Int, Cell>> {
        return activeCells.toList()
    }

    private fun rebuildActiveCells() {
        activeCells.clear()
        // TODO how much time is this?
        for (y in 0 until height) {
            for (x in 0 until width) {
                val cell = grid[y][x]
                if (cell.type == CellType.SAND ||
                    cell.type == CellType.OBSTACLE ||
                    cell.type == CellType.SLIDING_OBSTACLE) {
                    activeCells.add(Triple(x, y, cell))
                }
            }
        }
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
            particles.retainAll { !(it.x == x && it.y == y) }
        }
        // Clean up empty sets to prevent memory leaks
        val keysToRemove = settledParticlesByObstacle.keys.filter { key ->
            settledParticlesByObstacle[key]?.isEmpty() == true
        }
        keysToRemove.forEach { settledParticlesByObstacle.remove(it) }
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

    fun reset() {
        // Clear all grid cells
        for (y in 0 until height) {
            for (x in 0 until width) {
                grid.setCell(x, y, Cell())
            }
        }

        // Clear all collections
        movingParticles.clear()
        settledParticlesByObstacle.clear()
        slidingObstacles.clear()
        activeCells.clear()
    }
}