package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.model.LicenseInfo
import hu.adamfejes.zenmotes.service.LicenseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InfoViewModel(
    private val licenseService: LicenseService
) : ViewModel() {

    private val _licenses = MutableStateFlow<List<LicenseInfo>>(emptyList())
    val licenses: StateFlow<List<LicenseInfo>> = _licenses.asStateFlow()

    init {
        loadLicenses()
    }

    private fun loadLicenses() {
        viewModelScope.launch {
            _licenses.value = licenseService.getLicenses()
        }
    }
}