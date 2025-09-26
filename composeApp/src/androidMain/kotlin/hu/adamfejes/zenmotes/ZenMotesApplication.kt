package hu.adamfejes.zenmotes

import android.app.Application
import com.google.firebase.FirebaseApp
import hu.adamfejes.zenmotes.di.androidDataModule
import hu.adamfejes.zenmotes.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ZenMotesApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        startKoin {
            androidContext(this@ZenMotesApplication)
            modules(appModule, androidDataModule)
        }
    }
}