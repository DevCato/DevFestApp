package pe.gdg.open.devfest.app.domain.model

import kotlin.time.Instant

/** Una sesión de la agenda: una charla (tocable) o una pausa (no tocable). */
sealed interface Session {
    val id: String
    val title: String
    val startsAt: Instant
    val endsAt: Instant
}

data class Talk(
    override val id: String,
    override val title: String,
    override val startsAt: Instant,
    override val endsAt: Instant,
    /** Vacío si el backend no indica sala. */
    val room: String,
    val track: Track,
    val level: Level,
    val description: String,
    val topics: List<String>,
    /** Vacío = "Ponente por confirmar". */
    val speakers: List<Speaker>,
    /** Flag del administrador: muestra "LIVE NOW". No depende de la hora. */
    val isLive: Boolean,
) : Session

data class Break(
    override val id: String,
    override val title: String,
    override val startsAt: Instant,
    override val endsAt: Instant,
) : Session

enum class Level { BASICO, INTERMEDIO, AVANZADO, TODOS }

data class Speaker(
    val name: String,
    val photoUrl: String?,
    val role: String?,
    val company: String?,
)

data class Agenda(
    val updatedAt: Instant,
    val tracks: List<Track>,
    /** Ordenadas por hora de inicio; a igual hora se conserva el orden del backend. */
    val sessions: List<Session>,
)
