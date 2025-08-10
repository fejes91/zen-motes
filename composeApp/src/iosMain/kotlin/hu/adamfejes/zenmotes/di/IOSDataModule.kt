package hu.adamfejes.zenmotes.di

import hu.adamfejes.zenmotes.data.createDataStore
import org.koin.dsl.module

val iosDataModule = module {
    single { createDataStore() }
}