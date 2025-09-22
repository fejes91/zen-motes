package hu.adamfejes.zenmotes.di

import hu.adamfejes.zenmotes.logic.GameStateHolder
import hu.adamfejes.zenmotes.logic.SandGridHolder
import hu.adamfejes.zenmotes.logic.SandColorManager
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.logic.ScoreHolderImpl
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.ui.PauseViewModel
import hu.adamfejes.zenmotes.ui.SandSimulationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<ScoreHolder> { ScoreHolderImpl() }
    single { SandColorManager() }
    single { PreferencesService(get()) }
    single { SandGridHolder() }
    single { GameStateHolder(
        get(),
        get(),
        get()
    ) }
    viewModel {
        SandSimulationViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        PauseViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
}