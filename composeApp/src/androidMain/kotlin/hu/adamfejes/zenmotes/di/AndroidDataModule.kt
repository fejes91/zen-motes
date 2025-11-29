package hu.adamfejes.zenmotes.di

import hu.adamfejes.zenmotes.BuildConfig
import hu.adamfejes.zenmotes.config.KoinNames
import hu.adamfejes.zenmotes.data.createDataStore
import hu.adamfejes.zenmotes.service.AndroidAnalyticsService
import hu.adamfejes.zenmotes.service.AndroidSoundManager
import hu.adamfejes.zenmotes.service.AnalyticsService
import hu.adamfejes.zenmotes.service.LicenseService
import hu.adamfejes.zenmotes.service.SoundManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val androidDataModule = module {
    single { createDataStore(androidContext()) }
    single<SoundManager> { AndroidSoundManager(androidContext()) }
    single<AnalyticsService> { AndroidAnalyticsService(androidContext()) }
    single { LicenseService(androidContext()) }
    single(named(KoinNames.ADMOB_UNIT_ID)) { BuildConfig.ADMOB_AD_UNIT_ID }
}