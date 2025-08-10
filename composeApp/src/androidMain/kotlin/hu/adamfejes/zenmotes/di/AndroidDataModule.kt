package hu.adamfejes.zenmotes.di

import hu.adamfejes.zenmotes.data.createDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidDataModule = module {
    single { createDataStore(androidContext()) }
}