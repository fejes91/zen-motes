package hu.adamfejes.zenmotes.model

data class LicenseInfo(
    val name: String,
    val groupId: String,
    val artifactId: String,
    val version: String,
    val licenses: List<SpdxLicense>,
    val scmUrl: String?
)

data class SpdxLicense(
    val identifier: String,
    val name: String,
    val url: String
)