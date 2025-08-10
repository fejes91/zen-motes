package hu.adamfejes.zenmotes

import android.app.Application
import hu.adamfejes.zenmotes.di.androidDataModule
import hu.adamfejes.zenmotes.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ZenMotesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@ZenMotesApplication)
            modules(appModule, androidDataModule)
        }
    }
}