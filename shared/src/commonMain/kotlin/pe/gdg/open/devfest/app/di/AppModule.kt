package pe.gdg.open.devfest.app.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import pe.gdg.open.devfest.app.AppConfig
import pe.gdg.open.devfest.app.data.SessionEvents
import pe.gdg.open.devfest.app.data.api.DevFestApi
import pe.gdg.open.devfest.app.data.api.FakeDevFestApi
import pe.gdg.open.devfest.app.data.api.FakeScenario
import pe.gdg.open.devfest.app.data.local.AgendaLocalStore
import pe.gdg.open.devfest.app.data.local.DATASTORE_FILE_NAME
import pe.gdg.open.devfest.app.data.local.DataStoreKeyValueStore
import pe.gdg.open.devfest.app.data.local.KeyValueStore
import pe.gdg.open.devfest.app.data.local.createDataStore
import pe.gdg.open.devfest.app.data.repository.AgendaRepositoryImpl
import pe.gdg.open.devfest.app.data.repository.AuthRepositoryImpl
import pe.gdg.open.devfest.app.data.repository.EventRepositoryImpl
import pe.gdg.open.devfest.app.data.repository.GemsRepositoryImpl
import pe.gdg.open.devfest.app.domain.repository.GemsRepository
import pe.gdg.open.devfest.app.ui.screens.gems.GemsViewModel
import pe.gdg.open.devfest.app.ui.screens.scanner.ScannerViewModel
import pe.gdg.open.devfest.app.ui.screens.ranking.RankingViewModel
import pe.gdg.open.devfest.app.ui.screens.profile.ProfileViewModel
import pe.gdg.open.devfest.app.ui.AppViewModel
import pe.gdg.open.devfest.app.data.repository.RankingRepositoryImpl
import pe.gdg.open.devfest.app.domain.repository.RankingRepository
import pe.gdg.open.devfest.app.data.repository.UserRepositoryImpl
import pe.gdg.open.devfest.app.domain.repository.AgendaRepository
import pe.gdg.open.devfest.app.domain.repository.AuthRepository
import pe.gdg.open.devfest.app.domain.repository.EventRepository
import pe.gdg.open.devfest.app.domain.repository.UserRepository
import pe.gdg.open.devfest.app.ui.screens.agenda.AgendaViewModel
import pe.gdg.open.devfest.app.ui.screens.myagenda.MyAgendaViewModel
import pe.gdg.open.devfest.app.ui.screens.talk.TalkDetailViewModel
import org.koin.core.module.dsl.viewModel
import pe.gdg.open.devfest.app.ui.util.NowSource
import pe.gdg.open.devfest.app.ui.util.TickingNowSource
import pe.gdg.open.devfest.app.platform.AuthGateway
import pe.gdg.open.devfest.app.platform.DataStorePathProvider
import pe.gdg.open.devfest.app.ui.screens.login.LoginViewModel
import kotlin.time.Clock

/**
 * Dependencias comunes. El [AuthGateway] lo registra cada plataforma: el real con Firebase si
 * la app tiene su configuración, o el simulado si no (T053).
 */
val appModule: Module = module {
    single<Clock> { Clock.System }
    single<NowSource> { TickingNowSource(get()) }
    single { SessionEvents() }

    // Fuente de datos: datos falsos en esta versión (FR-070).
    single { FakeScenario(latency = AppConfig.fakeLatency) }
    single<DevFestApi> {
        val auth = get<AuthGateway>()
        FakeDevFestApi(identity = { auth.currentSession() }, clock = get(), scenario = get())
    }

    // Almacenamiento local.
    single { createDataStore(get<DataStorePathProvider>().path(DATASTORE_FILE_NAME)) }
    single<KeyValueStore> { DataStoreKeyValueStore(get()) }
    single { AgendaLocalStore(get()) }

    // Repositorios.
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<AgendaRepository> { AgendaRepositoryImpl(get(), get(), get()) }
    single<EventRepository> { EventRepositoryImpl(get(), get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<GemsRepository> { GemsRepositoryImpl(get(), get(), get()) }
    single<RankingRepository> { RankingRepositoryImpl(get(), get()) }

    // ViewModels.
    viewModelOf(::LoginViewModel)
    viewModelOf(::AgendaViewModel)
    viewModelOf(::MyAgendaViewModel)
    viewModelOf(::GemsViewModel)
    viewModelOf(::ScannerViewModel)
    viewModelOf(::RankingViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::AppViewModel)
    viewModel { params -> TalkDetailViewModel(talkId = params.get(), agenda = get()) }
}

/** Dependencias que entrega cada plataforma (rutas de archivos, login). */
expect val platformModule: Module

/** Inicia Koin una sola vez por proceso. */
fun initKoin(
    extraModules: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {},
) {
    if (KoinPlatform.getKoinOrNull() != null) return
    startKoin {
        appDeclaration()
        modules(listOf(platformModule, appModule) + extraModules)
    }
}
