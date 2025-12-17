package hu.adamfejes.zenmotes

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val appVersion: String = BuildConfig.VERSION_NAME
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}

@Composable
actual fun rememberIsLandscape(): State<Boolean> {
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        derivedStateOf {
            configuration.screenWidthDp > configuration.screenHeightDp
        }
    }
}

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}

@Composable
actual fun rememberOpenUrl(): (String) -> Unit {
    val context = LocalContext.current
    return remember {
        { url: String ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }
}