package hu.adamfejes.zenmotes.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import hu.adamfejes.zenmotes.utils.Logger

@Composable
actual fun ConsentDialog() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val activity = context.getActivity()
        if (activity == null) {
            Logger.e("ConsentDialog", "Context is not an Activity")
            return@LaunchedEffect
        }

        val consentInformation: ConsentInformation =
            UserMessagingPlatform.getConsentInformation(context)

        val consentRequestParameters = ConsentRequestParameters.Builder()
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            consentRequestParameters,
            {
                Logger.d("ConsentDialog", "Consent info updated successfully")
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    Logger.d(
                        "ConsentDialog",
                        "Consent form handled with error: ${formError?.message}"
                    )
                }

            },
            { requestConsentError ->
                Logger.d(
                    "ConsentDialog",
                    "Error updating consent info: ${requestConsentError.message}"
                )
            },
        )
    }
}

fun Context.getActivity(): Activity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}