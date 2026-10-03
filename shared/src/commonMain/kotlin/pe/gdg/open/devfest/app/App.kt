package pe.gdg.open.devfest.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import pe.gdg.open.devfest.app.domain.repository.AuthRepository
import pe.gdg.open.devfest.app.ui.AppEvent
import pe.gdg.open.devfest.app.ui.AppViewModel
import pe.gdg.open.devfest.app.ui.modal.ModalHost
import pe.gdg.open.devfest.app.ui.modal.ModalRequestEffect
import pe.gdg.open.devfest.app.ui.navigation.AppNavHost
import pe.gdg.open.devfest.app.ui.navigation.Route
import pe.gdg.open.devfest.app.ui.navigation.navigateToLogin
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestTheme

/** Raíz de la app: tema, host único de modales, navegación y sesión vencida. */
@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }
    // Con una sesión guardada se entra directo a la Agenda (US1, escenario 4).
    val auth = koinInject<AuthRepository>()
    val startDestination = remember { if (auth.session.value != null) Route.Agenda else Route.Login }
    val navController = rememberNavController()
    val appViewModel = koinViewModel<AppViewModel>()

    DevFestTheme {
        Box(Modifier.fillMaxSize().background(DevFestColors.Background)) {
            ModalHost {
                ModalRequestEffect(appViewModel.modals)
                LaunchedEffect(appViewModel) {
                    appViewModel.events.collect { event ->
                        when (event) {
                            AppEvent.NavigateToLogin -> navController.navigateToLogin()
                        }
                    }
                }
                AppNavHost(navController = navController, startDestination = startDestination)
            }
        }
    }
}
