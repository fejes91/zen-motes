package hu.adamfejes.zenmotes.utils

import hu.adamfejes.zenmotes.logic.ScoreEvent
import hu.adamfejes.zenmotes.logic.SlidingObstacle
import hu.adamfejes.zenmotes.logic.getBallparkScore
import kotlin.math.roundToInt

internal fun createScoreDecreaseEvent(slidingObstacle: SlidingObstacle): ScoreEvent = ScoreEvent(
    x = slidingObstacle.x.roundToInt(),
    y = slidingObstacle.y,
    score = -slidingObstacle.getBallparkScore() * 2,
    obstacle = slidingObstacle
)

internal fun createScoreIncreaseEvent(
    isBonus: Boolean,
    slidingObstacle: SlidingObstacle
): ScoreEvent {
    val finalScore = if (isBonus) {
        slidingObstacle.getBallparkScore() * 2
    } else {
        slidingObstacle.getBallparkScore()
    }
    val event = ScoreEvent(
        x = slidingObstacle.x.roundToInt(),
        y = slidingObstacle.y,
        score = finalScore,
        obstacle = slidingObstacle,
        isBonus = isBonus
    )
    return event
}