package hu.adamfejes.zenmotes

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import hu.adamfejes.zenmotes.di.androidDataModule
import hu.adamfejes.zenmotes.di.appModule
import hu.adamfejes.zenmotes.utils.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ZenMotesApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        MobileAds.initialize(this) {
            Logger.d("Application", "AdMob initialized with status")
        }

        startKoin {
            androidContext(this@ZenMotesApplication)
            modules(appModule, androidDataModule)
        }
    }
}