package pe.gdg.open.devfest.app.domain.logic

import pe.gdg.open.devfest.app.domain.model.Session
import pe.gdg.open.devfest.app.domain.model.Talk

/** "Charla N / total". */
data class TalkPosition(val number: Int, val total: Int)

/**
 * Posición de una charla (FR-031): charlas sin pausas, ordenadas por hora de inicio; N desde 1.
 * `null` si el id no es una charla de la agenda.
 */
fun talkPosition(sessions: List<Session>, talkId: String): TalkPosition? {
    val talks = sessions.filterIsInstance<Talk>().sortedBy { it.startsAt }
    val index = talks.indexOfFirst { it.id == talkId }
    return if (index < 0) null else TalkPosition(index + 1, talks.size)
}
