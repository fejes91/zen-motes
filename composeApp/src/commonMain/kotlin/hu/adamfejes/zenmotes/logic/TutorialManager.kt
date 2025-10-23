package hu.adamfejes.zenmotes.logic

import hu.adamfejes.zenmotes.service.PreferencesService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class TutorialManager(
    private val preferencesService: PreferencesService
) {
    private val coroutineScope = CoroutineScope(kotlinx.coroutines.Dispatchers.Default)
    private val steps = listOf(
        TutorialStep.SAND_INTRO,
        TutorialStep.SAME_COLOR_DESTRUCTION,
        TutorialStep.DIFFERENT_COLOR_DESTRUCTION,
        TutorialStep.CHAIN_REACTION,
        TutorialStep.SCORE_INTRO,
        TutorialStep.COMPLETED
    )

    private val currentStep: MutableStateFlow<TutorialStep?> = MutableStateFlow(null)
    val currentStepFlow: Flow<TutorialStep> = currentStep.filterNotNull()

    fun advanceToNextStep() {
        val currentIndex = steps.indexOf(currentStep.value)
        if (currentIndex < steps.size - 1) {
            currentStep.value = steps[currentIndex + 1]
        }

        persistCompleted()
    }

    fun startTutorial() {
        currentStep.value = TutorialStep.SAND_INTRO
    }

    fun finishTutorial() {
        currentStep.value = TutorialStep.COMPLETED
        persistCompleted()
    }

    private fun persistCompleted() {
        if (currentStep.value == TutorialStep.COMPLETED) {
            coroutineScope.launch {
                preferencesService.saveTutorialCompleted(true)
            }
        }
    }
}