package hu.adamfejes.zenmotes.logic

/**
 * ObstacleGenerator acts as a provider that returns either
 * NormalObstacleGenerator or TutorialObstacleGenerator based on the game mode.
 */
class ObstacleGeneratorProvider(
    private val tutorialObstacleGenerator: TutorialObstacleGenerator,
    private val normalObstacleGenerator: NormalObstacleGenerator,
) {
    private var isTutorialMode = false

    fun getGenerator(): IObstacleGenerator {
        return if (isTutorialMode) tutorialObstacleGenerator else normalObstacleGenerator
    }

    fun setTutorialMode(isTutorialMode: Boolean) {
        this.isTutorialMode = isTutorialMode
    }

    fun setDimensions(width: Int, height: Int) {
        normalObstacleGenerator.width = width
        normalObstacleGenerator.height = height
        // todo tutorial generator might need dimensions in the future as well
    }
}