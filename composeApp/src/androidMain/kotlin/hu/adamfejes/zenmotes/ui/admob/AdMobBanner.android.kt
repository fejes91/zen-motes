package hu.adamfejes.zenmotes.ui.admob

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
//import androidx.compose.ui.viewinterop.AndroidView
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.AdSize
//import com.google.android.gms.ads.AdView

@Composable
actual fun AdMobBanner(unitId: String, modifier: Modifier) {
    // todo turn on ads after publishing
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
}