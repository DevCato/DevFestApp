package pe.gdg.open.devfest.app.ui.navigation

import kotlinx.serialization.Serializable

/** Destinos de la app (rutas tipadas de Navigation Compose). */
sealed interface Route {
    @Serializable
    data object Login : Route

    /** Pestaña 1. */
    @Serializable
    data object Agenda : Route

    /** Pestaña 2. */
    @Serializable
    data object MyAgenda : Route

    @Serializable
    data class TalkDetail(val talkId: String) : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data object Gems : Route

    @Serializable
    data object Scanner : Route

    @Serializable
    data object Ranking : Route
}
