package pe.gdg.open.devfest.app.di

import org.koin.core.module.Module
import org.koin.dsl.module
import pe.gdg.open.devfest.app.platform.DataStorePathProvider
import pe.gdg.open.devfest.app.platform.IosDataStorePathProvider

actual val platformModule: Module = module {
    single<DataStorePathProvider> { IosDataStorePathProvider() }
}
