package pe.gdg.open.devfest.app.di

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.gdg.open.devfest.app.platform.AndroidDataStorePathProvider
import pe.gdg.open.devfest.app.platform.AuthGateway
import pe.gdg.open.devfest.app.platform.CurrentActivityHolder
import pe.gdg.open.devfest.app.platform.DataStorePathProvider
import pe.gdg.open.devfest.app.platform.FakeAuthGateway
import pe.gdg.open.devfest.app.platform.FirebaseAuthGateway

actual val platformModule: Module = module {
    single<DataStorePathProvider> { AndroidDataStorePathProvider(androidContext()) }

    // Se crea al iniciar para registrar la primera Activity.
    single(createdAtStart = true) { CurrentActivityHolder(androidApplication()) }

    // Login real si la app se compiló con google-services.json; si no, simulado (T053).
    single<AuthGateway> {
        val context = androidContext()
        val webClientId = context.defaultWebClientId()
        if (FirebaseApp.getApps(context).isNotEmpty() && webClientId != null) {
            FirebaseAuthGateway(get(), webClientId)
        } else {
            FakeAuthGateway()
        }
    }
}

/** Recurso que genera el plugin google-services en el módulo de la app. */
@SuppressLint("DiscouragedApi")
private fun Context.defaultWebClientId(): String? {
    val id = resources.getIdentifier("default_web_client_id", "string", packageName)
    return if (id != 0) getString(id) else null
}
