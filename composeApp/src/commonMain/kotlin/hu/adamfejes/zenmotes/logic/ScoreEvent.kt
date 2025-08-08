package hu.adamfejes.zenmotes.logic

data class ScoreEvent(
    val x: Int,
    val y: Int,
    val score: Int,
    val obstacleId: String
)
