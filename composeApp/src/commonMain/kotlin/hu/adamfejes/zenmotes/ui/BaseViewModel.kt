package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

open class BaseViewModel(
    preferencesService: PreferencesService
) : ViewModel() {

    val appTheme: StateFlow<AppTheme?> = preferencesService.getTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    val soundEnabled: StateFlow<Boolean> = preferencesService.getSoundEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = true
        )
}