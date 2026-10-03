package pe.gdg.open.devfest.app.ui.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.domain.logic.durationMinutes
import pe.gdg.open.devfest.app.domain.logic.formatHour
import pe.gdg.open.devfest.app.domain.model.Level
import pe.gdg.open.devfest.app.domain.model.Session
import pe.gdg.open.devfest.app.domain.model.Talk
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.duration_minutes
import pe.gdg.open.devfest.app.resources.level_avanzado
import pe.gdg.open.devfest.app.resources.level_basico
import pe.gdg.open.devfest.app.resources.level_intermedio
import pe.gdg.open.devfest.app.resources.level_todos

@Composable
fun levelLabel(level: Level): String = stringResource(
    when (level) {
        Level.BASICO -> Res.string.level_basico
        Level.INTERMEDIO -> Res.string.level_intermedio
        Level.AVANZADO -> Res.string.level_avanzado
        Level.TODOS -> Res.string.level_todos
    },
)

/** "45 min". */
@Composable
fun durationLabel(session: Session): String =
    stringResource(Res.string.duration_minutes, durationMinutes(session.startsAt, session.endsAt))

/** "10:30–11:15" en hora de Lima. */
fun timeRange(session: Session): String = "${formatHour(session.startsAt)}–${formatHour(session.endsAt)}"

/** "Sala 1 · 45 min · Intermedio": datos de la tarjeta de la Agenda (se omite la sala vacía). */
@Composable
fun agendaInfoLine(talk: Talk): String =
    listOf(talk.room, durationLabel(talk), levelLabel(talk.level)).filter { it.isNotBlank() }.joinToString(" · ")

/** "Sala 1 · 10:30–11:15": datos de la tarjeta de Mi agenda. */
fun myAgendaInfoLine(talk: Talk): String =
    listOf(talk.room, timeRange(talk)).filter { it.isNotBlank() }.joinToString(" · ")
