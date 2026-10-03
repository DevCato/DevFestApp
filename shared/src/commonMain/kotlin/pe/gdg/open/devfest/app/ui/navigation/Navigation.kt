package pe.gdg.open.devfest.app.ui.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import pe.gdg.open.devfest.app.ui.components.MainTab

/**
 * Navega solo si la pantalla de origen sigue activa. Tras el primer toque la pantalla deja de
 * estar en primer plano, así un doble toque no apila la misma pantalla dos veces.
 */
fun NavHostController.navigateFrom(from: NavBackStackEntry, route: Route) {
    if (from.isActive()) navigate(route)
}

/** "Volver" una sola vez aunque el botón se toque dos veces seguidas. */
fun NavHostController.popBackStackFrom(from: NavBackStackEntry) {
    if (from.isActive()) popBackStack()
}

private fun NavBackStackEntry.isActive(): Boolean = lifecycle.currentState == Lifecycle.State.RESUMED

/**
 * Cambia de pestaña. La Agenda es la raíz: ir a una pestaña quita lo que haya encima de ella,
 * así "volver" desde Mi agenda regresa a la Agenda.
 */
fun NavHostController.navigateToTab(tab: MainTab) {
    val route: Route = when (tab) {
        MainTab.AGENDA -> Route.Agenda
        MainTab.MY_AGENDA -> Route.MyAgenda
    }
    navigate(route) {
        popUpTo<Route.Agenda>()
        launchSingleTop = true
    }
}

/** Vuelve a Login sin nada en la pila (cierre de sesión o sesión vencida). */
fun NavHostController.navigateToLogin() {
    navigate(Route.Login) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
