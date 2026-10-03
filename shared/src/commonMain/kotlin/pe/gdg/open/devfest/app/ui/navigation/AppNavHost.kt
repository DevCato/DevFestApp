package pe.gdg.open.devfest.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import pe.gdg.open.devfest.app.ui.components.MainTab
import pe.gdg.open.devfest.app.ui.screens.agenda.AgendaScreen
import pe.gdg.open.devfest.app.ui.screens.gems.GemsScreen
import pe.gdg.open.devfest.app.ui.screens.login.LoginScreen
import pe.gdg.open.devfest.app.ui.screens.myagenda.MyAgendaScreen
import pe.gdg.open.devfest.app.ui.screens.profile.ProfileScreen
import pe.gdg.open.devfest.app.ui.screens.ranking.RankingScreen
import pe.gdg.open.devfest.app.ui.screens.scanner.ScannerScreen
import pe.gdg.open.devfest.app.ui.screens.talk.TalkDetailScreen
import pe.gdg.open.devfest.app.ui.theme.EaseOutStrong
import pe.gdg.open.devfest.app.ui.theme.LocalReduceMotion

/**
 * Grafo de navegación. Push ~280 ms y pop ~220 ms (desplazamiento corto + fundido); con
 * "reducir movimiento", solo fundido. Entre las pestañas Agenda y Mi agenda no hay animación.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Route = Route.Login,
) {
    val reduceMotion = LocalReduceMotion.current
    val transitions = NavTransitions(reduceMotion)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { transitions.enter(this, pop = false) },
        exitTransition = { transitions.exit(this, pop = false) },
        popEnterTransition = { transitions.enter(this, pop = true) },
        popExitTransition = { transitions.exit(this, pop = true) },
    ) {
        composable<Route.Login> {
            LoginScreen(
                onSignedIn = {
                    // Login no queda en la pila: "volver" desde la Agenda sale de la app.
                    navController.navigate(Route.Agenda) {
                        popUpTo<Route.Login> { inclusive = true }
                    }
                },
            )
        }
        // navigateFrom / popBackStackFrom: un doble toque no abre ni cierra dos pantallas.
        composable<Route.Agenda> { entry ->
            AgendaScreen(
                onTalkClick = { navController.navigateFrom(entry, Route.TalkDetail(it)) },
                onTabSelected = navController::navigateToTab,
                onProfileClick = { navController.navigateFrom(entry, Route.Profile) },
                onGemsClick = { navController.navigateFrom(entry, Route.Gems) },
            )
        }
        composable<Route.MyAgenda> { entry ->
            MyAgendaScreen(
                onTalkClick = { navController.navigateFrom(entry, Route.TalkDetail(it)) },
                onTabSelected = navController::navigateToTab,
                onProfileClick = { navController.navigateFrom(entry, Route.Profile) },
                onGemsClick = { navController.navigateFrom(entry, Route.Gems) },
            )
        }
        composable<Route.TalkDetail> { entry ->
            // "Volver" regresa a la pantalla de origen (Agenda o Mi agenda).
            TalkDetailScreen(
                talkId = entry.toRoute<Route.TalkDetail>().talkId,
                onBack = { navController.popBackStackFrom(entry) },
            )
        }
        composable<Route.Profile> { entry ->
            // La pila es real: Perfil → Mis gemas → Ranking → volver → volver → Perfil (FR-057).
            ProfileScreen(
                onBack = { navController.popBackStackFrom(entry) },
                onAgendaClick = { navController.navigateToTab(MainTab.AGENDA) },
                onMyAgendaClick = { navController.navigateToTab(MainTab.MY_AGENDA) },
                onGemsClick = { navController.navigateFrom(entry, Route.Gems) },
                onRankingClick = { navController.navigateFrom(entry, Route.Ranking) },
                onLoggedOut = navController::navigateToLogin,
            )
        }
        composable<Route.Gems> { entry ->
            GemsScreen(
                onBack = { navController.popBackStackFrom(entry) },
                onScanClick = { navController.navigateFrom(entry, Route.Scanner) },
                onRankingClick = { navController.navigateFrom(entry, Route.Ranking) },
            )
        }
        composable<Route.Scanner> { entry ->
            ScannerScreen(onClose = { navController.popBackStackFrom(entry) })
        }
        composable<Route.Ranking> { entry ->
            RankingScreen(onBack = { navController.popBackStackFrom(entry) })
        }
    }
}

private class NavTransitions(private val reduceMotion: Boolean) {

    fun enter(scope: AnimatedContentTransitionScope<NavBackStackEntry>, pop: Boolean): EnterTransition {
        if (scope.isTabSwitch()) return EnterTransition.None
        val duration = if (pop) POP_MILLIS else PUSH_MILLIS
        val fade = fadeIn(tween(duration, easing = EaseOutStrong))
        if (reduceMotion) return fade
        // Prototipo: push entra desde +28 px, pop desde -20 px.
        val offset = if (pop) -20.dp else 28.dp
        return fade + slideInHorizontally(tween(duration, easing = EaseOutStrong)) { fullWidth ->
            (fullWidth * offset.value / REFERENCE_WIDTH).toInt()
        }
    }

    fun exit(scope: AnimatedContentTransitionScope<NavBackStackEntry>, pop: Boolean): ExitTransition {
        if (scope.isTabSwitch()) return ExitTransition.None
        return fadeOut(tween(if (pop) POP_MILLIS else PUSH_MILLIS, easing = EaseOutStrong))
    }

    private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean =
        initialState.isTab() && targetState.isTab()

    private fun NavBackStackEntry.isTab(): Boolean =
        destination.hasRoute<Route.Agenda>() || destination.hasRoute<Route.MyAgenda>()

    private companion object {
        const val PUSH_MILLIS = 280
        const val POP_MILLIS = 220

        /** Ancho del prototipo (390 px) para escalar los desplazamientos. */
        const val REFERENCE_WIDTH = 390f
    }
}
