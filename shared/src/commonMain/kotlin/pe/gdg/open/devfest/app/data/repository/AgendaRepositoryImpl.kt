package pe.gdg.open.devfest.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.gdg.open.devfest.app.data.SessionEvents
import pe.gdg.open.devfest.app.data.api.DevFestApi
import pe.gdg.open.devfest.app.data.api.safeApiCall
import pe.gdg.open.devfest.app.data.local.AgendaLocalStore
import pe.gdg.open.devfest.app.data.mapper.toDomain
import pe.gdg.open.devfest.app.domain.model.Agenda
import pe.gdg.open.devfest.app.domain.repository.AgendaRepository
import pe.gdg.open.devfest.app.domain.repository.Outcome

/** Agenda offline-first: la UI siempre lee la caché; `refresh()` la reemplaza si hay respuesta. */
class AgendaRepositoryImpl(
    private val api: DevFestApi,
    private val store: AgendaLocalStore,
    private val sessionEvents: SessionEvents,
) : AgendaRepository {

    private val savedMutex = Mutex()

    override val agenda: Flow<Agenda?> = store.agenda.map { dto ->
        dto?.let { runCatching { it.toDomain() }.getOrNull() }
    }

    override suspend fun refresh(): Outcome<Unit> {
        val result = safeApiCall {
            val dto = api.getAgenda()
            dto.toDomain() // valida antes de reemplazar la caché
            dto
        }.reportSessionExpiry(sessionEvents)
        if (result is Outcome.Success) {
            store.saveAgenda(result.value)
            pruneSavedTalks(result.value.sessions.map { it.id }.toSet())
        }
        return result.asUnit()
    }

    override val savedTalkIds: Flow<Set<String>> = store.savedTalkIds

    override suspend fun toggleSaved(talkId: String) {
        savedMutex.withLock {
            val current = store.savedTalkIds.first()
            store.setSavedTalkIds(if (talkId in current) current - talkId else current + talkId)
        }
    }

    override suspend fun clearSaved() {
        savedMutex.withLock { store.clearSavedTalkIds() }
    }

    /** Una charla guardada que se eliminó de la agenda publicada desaparece sin error. */
    private suspend fun pruneSavedTalks(publishedIds: Set<String>) {
        savedMutex.withLock {
            val current = store.savedTalkIds.first()
            val kept = current intersect publishedIds
            if (kept != current) store.setSavedTalkIds(kept)
        }
    }
}
