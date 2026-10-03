package pe.gdg.open.devfest.app.domain.logic

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pe.gdg.open.devfest.app.domain.model.Session
import kotlin.time.Instant

/**
 * Posición de la línea "AHORA" en [sessions] (ordenadas por inicio): el índice de la primera
 * sesión que aún no empieza, o `sessions.size` si ya empezaron todas. `null` si no se muestra:
 * otro día en Lima, antes del inicio de la primera sesión o después del fin de la última.
 */
fun nowLineIndex(
    sessions: List<Session>,
    now: Instant,
    eventDate: LocalDate,
    timeZone: TimeZone = LimaTimeZone,
): Int? {
    if (sessions.isEmpty()) return null
    if (now.toLocalDateTime(timeZone).date != eventDate) return null
    val firstStart = sessions.minOf { it.startsAt }
    val lastEnd = sessions.maxOf { it.endsAt }
    if (now < firstStart || now > lastEnd) return null
    val next = sessions.indexOfFirst { it.startsAt > now }
    return if (next >= 0) next else sessions.size
}
