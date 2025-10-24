package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.service.PreferencesService
import kotlinx.coroutines.launch

class TutorialViewModel(private val preferencesService: PreferencesService) :
    BaseViewModel(preferencesService) {

    fun initializeTutorial() {
        viewModelScope.launch {
            preferencesService.saveTutorialShown(true)
        }
    }
}