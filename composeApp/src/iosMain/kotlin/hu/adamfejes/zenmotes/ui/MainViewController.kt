package hu.adamfejes.zenmotes.ui

import androidx.compose.ui.window.ComposeUIViewController
import hu.adamfejes.zenmotes.di.appModule
import hu.adamfejes.zenmotes.di.iosDataModule
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initializeKoin()
    return ComposeUIViewController { SandSimulation() }
}

fun initializeKoin() {
    startKoin {
        modules(appModule, iosDataModule)
    }
}

