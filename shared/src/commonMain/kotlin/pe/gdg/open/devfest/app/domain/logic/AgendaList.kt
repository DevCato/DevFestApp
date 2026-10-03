package pe.gdg.open.devfest.app.domain.logic

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import pe.gdg.open.devfest.app.domain.model.Break
import pe.gdg.open.devfest.app.domain.model.Session
import pe.gdg.open.devfest.app.domain.model.Talk
import pe.gdg.open.devfest.app.domain.model.TrackId
import kotlin.time.Instant

/** Filtros fijos de la Agenda (FR-012). `GENERAL` no tiene filtro propio. */
enum class AgendaFilter(val trackId: TrackId?) {
    ALL(null),
    IA(TrackId.IA),
    WEB(TrackId.WEB),
    MOBILE(TrackId.MOBILE),
    CLOUD(TrackId.CLOUD),
}

/** Una fila de la lista de la Agenda. */
sealed interface AgendaRow {
    val key: String

    data class TalkRow(val talk: Talk, val showTime: Boolean, val isPast: Boolean) : AgendaRow {
        override val key: String get() = "talk-${talk.id}"
    }

    data class BreakRow(val pause: Break, val showTime: Boolean, val isPast: Boolean) : AgendaRow {
        override val key: String get() = "break-${pause.id}"
    }

    data class NowRow(val now: Instant) : AgendaRow {
        override val key: String get() = "now"
    }
}

/**
 * Arma la lista de la Agenda (data-model.md, "Lista de la Agenda"):
 * - "Todo": charlas y pausas, con la línea AHORA.
 * - Filtro de track: solo charlas de ese track, sin pausas ni línea AHORA.
 * - La hora se muestra solo en la primera sesión de cada grupo con el mismo inicio.
 *
 * @param eventDate fecha del evento; si se desconoce, la del día de la primera sesión en Lima.
 */
fun buildAgendaRows(
    sessions: List<Session>,
    filter: AgendaFilter,
    now: Instant,
    eventDate: LocalDate?,
): List<AgendaRow> {
    val visible = when (filter.trackId) {
        null -> sessions
        else -> sessions.filter { it is Talk && it.track.id == filter.trackId }
    }
    var previousStart: Instant? = null
    val rows: MutableList<AgendaRow> = visible.mapTo(mutableListOf()) { session ->
        val showTime = session.startsAt != previousStart
        previousStart = session.startsAt
        when (session) {
            is Talk -> AgendaRow.TalkRow(session, showTime, isPast(session, now))
            is Break -> AgendaRow.BreakRow(session, showTime, isPast(session, now))
        }
    }
    if (filter == AgendaFilter.ALL) {
        val date = eventDate ?: visible.firstOrNull()?.startsAt?.toLocalDateTime(LimaTimeZone)?.date
        val index = date?.let { nowLineIndex(visible, now, it) }
        if (index != null) rows.add(index, AgendaRow.NowRow(now))
    }
    return rows
}
