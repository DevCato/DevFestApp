package pe.gdg.open.devfest.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.gdg.open.devfest.app.data.SessionEvents
import pe.gdg.open.devfest.app.data.api.DevFestApi
import pe.gdg.open.devfest.app.data.api.safeApiCall
import pe.gdg.open.devfest.app.data.local.AgendaLocalStore
import pe.gdg.open.devfest.app.data.mapper.toDomain
import pe.gdg.open.devfest.app.domain.model.Event
import pe.gdg.open.devfest.app.domain.repository.EventRepository
import pe.gdg.open.devfest.app.domain.repository.Outcome

class EventRepositoryImpl(
    private val api: DevFestApi,
    private val store: AgendaLocalStore,
    private val sessionEvents: SessionEvents,
) : EventRepository {

    override val event: Flow<Event?> = store.event.map { dto ->
        dto?.let { runCatching { it.toDomain() }.getOrNull() }
    }

    override suspend fun refresh(): Outcome<Unit> {
        val result = safeApiCall {
            val dto = api.getEvent()
            dto.toDomain()
            dto
        }.reportSessionExpiry(sessionEvents)
        if (result is Outcome.Success) store.saveEvent(result.value)
        return result.asUnit()
    }
}
