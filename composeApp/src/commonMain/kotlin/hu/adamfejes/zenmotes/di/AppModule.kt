package hu.adamfejes.zenmotes.di

import hu.adamfejes.zenmotes.logic.SandColorManager
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.logic.ScoreHolderImpl
import hu.adamfejes.zenmotes.logic.SessionTimer
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.ui.SandSimulationViewModel
import org.koin.dsl.module

val appModule = module {
    single<ScoreHolder> { ScoreHolderImpl() }
    single { SandColorManager() }
    single { SessionTimer() }
    single { PreferencesService(get()) }
    factory { SandSimulationViewModel(get(), get(), get(), get()) }
}