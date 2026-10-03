package pe.gdg.open.devfest.app

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.dsl.module
import pe.gdg.open.devfest.app.di.initKoin
import pe.gdg.open.devfest.app.platform.AuthGateway
import pe.gdg.open.devfest.app.platform.FakeAuthGateway
import platform.UIKit.UIViewController

/**
 * Punto de entrada de iOS.
 *
 * @param authGateway login con Firebase implementado en Swift (`FirebaseAuthGateway.swift`);
 * `null` si la app no tiene `GoogleService-Info.plist`, y entonces se usa el login simulado.
 */
fun MainViewController(authGateway: AuthGateway?): UIViewController {
    initKoin(
        extraModules = listOf(
            module { single<AuthGateway> { authGateway ?: FakeAuthGateway() } },
        ),
    )
    return ComposeUIViewController { App() }
}
