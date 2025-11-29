package hu.adamfejes.zenmotes.service

import android.content.Context
import hu.adamfejes.zenmotes.model.LicenseInfo
import hu.adamfejes.zenmotes.model.SpdxLicense
import hu.adamfejes.zenmotes.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

actual class LicenseService(private val context: Context) {
    actual suspend fun getLicenses(): List<LicenseInfo> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("licenses.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            (0 until jsonArray.length()).map { i ->
                val jsonObject = jsonArray.getJSONObject(i)

                val spdxLicenses = if (jsonObject.has("spdxLicenses")) {
                    jsonObject.getJSONArray("spdxLicenses")
                } else {
                    JSONArray()
                }
                val licenses = (0 until spdxLicenses.length()).map { j ->
                    val licenseObj = spdxLicenses.getJSONObject(j)
                    SpdxLicense(
                        identifier = licenseObj.getString("identifier"),
                        name = licenseObj.getString("name"),
                        url = licenseObj.getString("url")
                    )
                }

                val scmUrl = if (jsonObject.has("scm")) {
                    jsonObject.getJSONObject("scm").optString("url", null)
                } else {
                    null
                }

                try {
                    LicenseInfo(
                        name = jsonObject.getStringOr("name", ""),
                        groupId = jsonObject.getString("groupId"),
                        artifactId = jsonObject.getString("artifactId"),
                        version = jsonObject.getString("version"),
                        licenses = licenses,
                        scmUrl = scmUrl
                    )
                } catch (e: Exception) {
                    Logger.e("LicenseService", "Error parsing license info at index $i: ${e.message}")
                    Logger.e("LicenseService", "Offending JSON: ${jsonObject.toString()}")
                    throw e
                }
            }.also {
                Logger.d("LicenseService", "Parsed ${it.size} licenses")
            }
        } catch (e: Exception) {
            Logger.e("LicenseService", "Error parsing licenses: ${e.message}")
            emptyList()
        }
    }

    private fun JSONObject.getStringOr(key: String, default: String): String {
        return if (this.has(key)) this.getString(key) else default
    }
}