package hu.adamfejes.zenmotes.di

import hu.adamfejes.zenmotes.data.createDataStore
import hu.adamfejes.zenmotes.service.IOSSoundManager
import hu.adamfejes.zenmotes.service.SoundManager
import org.koin.dsl.module

val iosDataModule = module {
    single { createDataStore() }
    single<SoundManager> { IOSSoundManager() }
}