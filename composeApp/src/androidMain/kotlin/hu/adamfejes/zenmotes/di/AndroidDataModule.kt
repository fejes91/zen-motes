package hu.adamfejes.zenmotes.di

import hu.adamfejes.zenmotes.data.createDataStore
import hu.adamfejes.zenmotes.service.AndroidSoundManager
import hu.adamfejes.zenmotes.service.SoundManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidDataModule = module {
    single { createDataStore(androidContext()) }
    single<SoundManager> { AndroidSoundManager(androidContext()) }
}