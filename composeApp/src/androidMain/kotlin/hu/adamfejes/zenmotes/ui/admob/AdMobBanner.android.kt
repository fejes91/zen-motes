package hu.adamfejes.zenmotes.ui.admob

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import hu.adamfejes.zenmotes.utils.Logger

//import androidx.compose.ui.viewinterop.AndroidView
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.AdSize
//import com.google.android.gms.ads.AdView

@Composable
actual fun AdMobBanner(unitId: String, modifier: Modifier) {
    var canRequestAds by remember { mutableStateOf(false) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val consentInformation: ConsentInformation =
            UserMessagingPlatform.getConsentInformation(context)

        canRequestAds = consentInformation.canRequestAds()
        Logger.d("AdMobBanner", "Can request ads: $canRequestAds")
    }

    // todo turn on ads after publishing
//    if (canRequestAds) {
//    AndroidView(
//        modifier = modifier,
//        factory = { context ->
//            AdView(context).apply {
//                setAdSize(AdSize.BANNER)
//                adUnitId = unitId
//                loadAd(AdRequest.Builder().build())
//            }
//        }
//    )
//    }
}