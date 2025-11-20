package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.service.PreferencesService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TutorialViewModel(private val preferencesService: PreferencesService) :
    BaseViewModel(preferencesService) {

    val wasTutorialShown: StateFlow<Boolean> = preferencesService.isTutorialShown
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun initializeTutorial() {
        viewModelScope.launch {
            preferencesService.saveTutorialShown(true)
        }
    }
}