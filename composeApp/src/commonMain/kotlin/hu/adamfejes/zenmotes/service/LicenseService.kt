package hu.adamfejes.zenmotes.service

import hu.adamfejes.zenmotes.model.LicenseInfo

expect class LicenseService {
    suspend fun getLicenses(): List<LicenseInfo>
}