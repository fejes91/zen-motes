package hu.adamfejes.zenmotes.logic

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TutorialObstacleGenerator(
    private val tutorialManager: TutorialManager,
    private val sandColorManager: SandColorManager
) : BaseObstacleGenerator() {
    private var currentTutorialStep: TutorialStep = TutorialStep.SAND_INTRO
    init {
        scope.launch {
            tutorialManager.currentStepFlow
                .collect {
                    currentTutorialStep = it
                }
        }
    }

    override fun generateSlidingObstacle(frameTime: Long, obstacleTypes: List<SlidingObstacleType>): SlidingObstacle? {
        // Tutorial mode: don't generate any obstacles
        return null
    }

    override fun reset() {
        // Nothing to reset
    }

    override fun onPause() {
        // Nothing to pause
    }

    override fun onResume() {
        // Nothing to resume
    }

    override fun setDemoMode(isDemoMode: Boolean) {
        // Tutorial mode doesn't care about demo mode
    }

    override fun shouldGenerateObstacle(frameTime: Long): Boolean {
        return if (currentTutorialStep == TutorialStep.SAND_INTRO) {
            false
        } else {
            true // todo based on frameTime and some interval
        }
    }
}