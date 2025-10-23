package hu.adamfejes.zenmotes.di

import hu.adamfejes.zenmotes.logic.GameStateHolder
import hu.adamfejes.zenmotes.logic.NormalObstacleGenerator
import hu.adamfejes.zenmotes.logic.ObstacleGeneratorProvider
import hu.adamfejes.zenmotes.logic.SandGridHolder
import hu.adamfejes.zenmotes.logic.SandColorManager
import hu.adamfejes.zenmotes.logic.ScoreHolder
import hu.adamfejes.zenmotes.logic.ScoreHolderImpl
import hu.adamfejes.zenmotes.logic.TutorialManager
import hu.adamfejes.zenmotes.logic.TutorialObstacleGenerator
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.ui.GameOverViewModel
import hu.adamfejes.zenmotes.ui.MainMenuViewModel
import hu.adamfejes.zenmotes.ui.OrientationWarningViewModel
import hu.adamfejes.zenmotes.ui.PauseViewModel
import hu.adamfejes.zenmotes.ui.SandSimulationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<ScoreHolder> { ScoreHolderImpl() }
    single { TutorialManager(get()) }
    single { SandColorManager(get()) }
    single { PreferencesService(get()) }
    single { NormalObstacleGenerator(get()) }
    single { TutorialObstacleGenerator(
        get(),
        get(),
    ) }
    single { ObstacleGeneratorProvider(
        get(),
        get()
    ) }
    single { SandGridHolder(
        get(),
        get(),
        get()
    ) }
    single {
        GameStateHolder(
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel {
        SandSimulationViewModel(
            get(),
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
            get(),
            get()
        )
    }
    viewModel {
        GameOverViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        MainMenuViewModel(
            get()
        )
    }
    viewModel { OrientationWarningViewModel(get()) }
}