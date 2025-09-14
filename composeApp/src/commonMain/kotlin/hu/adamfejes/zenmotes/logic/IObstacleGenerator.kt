package hu.adamfejes.zenmotes.logic

interface IObstacleGenerator {
    fun generateSlidingObstacle(frameTime: Long, obstacleTypes: List<SlidingObstacleType>): SlidingObstacle?
    fun reset()
}