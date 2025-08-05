package hu.adamfejes.zenmotes.di

import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.logic.ScoreHolderImpl
import hu.adamfejes.zenmotes.ui.SandGrid
import hu.adamfejes.zenmotes.ui.SandSimulationViewModel
import org.koin.dsl.module

val appModule = module {
    single<ScoreHolder> { ScoreHolderImpl() }
    factory { SandSimulationViewModel(get()) }
}