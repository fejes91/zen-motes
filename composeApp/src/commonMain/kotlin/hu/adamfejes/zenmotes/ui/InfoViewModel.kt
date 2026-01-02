package hu.adamfejes.zenmotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.adamfejes.zenmotes.model.LicenseInfo
import hu.adamfejes.zenmotes.model.SpdxLicense
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
            val musicCredit = getMusicLicences()

            val generatedLicenses = licenseService.getLicenses().sortedBy { it.name }
            _licenses.value = listOf(musicCredit) + generatedLicenses
        }
    }

    private fun getMusicLicences(): LicenseInfo {
        return LicenseInfo(
            name = "Theme Music by Denis Pavlov",
            groupId = "",
            artifactId = "Marimba Game Music Playful Tropical Jungle Puzzle",
            version = "",
            licenses = listOf(
                SpdxLicense(
                    identifier = "Pixabay-License",
                    name = "Pixabay Content License",
                    url = "https://pixabay.com/service/license-summary/"
                )
            ),
            scmUrl = "https://pixabay.com/music/video-games-marimba-game-music-playful-tropical-jungle-puzzle-399759/"
        )
    }
}