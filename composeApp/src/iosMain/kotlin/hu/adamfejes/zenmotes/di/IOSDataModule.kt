package hu.adamfejes.zenmotes.di

import hu.adamfejes.zenmotes.config.KoinNames
import hu.adamfejes.zenmotes.data.createDataStore
import hu.adamfejes.zenmotes.service.AnalyticsService
import hu.adamfejes.zenmotes.service.IOSAnalyticsService
import hu.adamfejes.zenmotes.service.IOSMusicManager
import hu.adamfejes.zenmotes.service.IOSSoundManager
import hu.adamfejes.zenmotes.service.MusicManager
import hu.adamfejes.zenmotes.service.SoundManager
import org.koin.core.qualifier.named
import org.koin.dsl.module

val iosDataModule = module {
    single { createDataStore() }
    single<SoundManager> { IOSSoundManager() }
    single<MusicManager> { IOSMusicManager() }
    single<AnalyticsService> { IOSAnalyticsService() }
    single(named(KoinNames.ADMOB_UNIT_ID)) { "" }
}