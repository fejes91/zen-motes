package hu.adamfejes.zenmotes.service

import hu.adamfejes.zenmotes.model.LicenseInfo

actual class LicenseService {
    actual suspend fun getLicenses(): List<LicenseInfo> {
        // iOS implementation not yet available
        return emptyList()
    }
}