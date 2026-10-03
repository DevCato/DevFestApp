package pe.gdg.open.devfest.app.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import pe.gdg.open.devfest.app.data.api.DevFestJson
import pe.gdg.open.devfest.app.data.api.dto.AgendaDto
import pe.gdg.open.devfest.app.data.api.dto.EventDto

/**
 * Caché de agenda y evento (JSON de los DTOs del contrato) y charlas guardadas.
 * Claves: data-model.md, "Persistencia local".
 */
class AgendaLocalStore(
    private val store: KeyValueStore,
    private val json: Json = DevFestJson,
) {
    val agenda: Flow<AgendaDto?> = store.observeString(KEY_AGENDA).map { decode(it, AgendaDto.serializer()) }

    suspend fun saveAgenda(agenda: AgendaDto) {
        store.putString(KEY_AGENDA, json.encodeToString(AgendaDto.serializer(), agenda))
    }

    val event: Flow<EventDto?> = store.observeString(KEY_EVENT).map { decode(it, EventDto.serializer()) }

    suspend fun saveEvent(event: EventDto) {
        store.putString(KEY_EVENT, json.encodeToString(EventDto.serializer(), event))
    }

    val savedTalkIds: Flow<Set<String>> = store.observeStringSet(KEY_SAVED_TALK_IDS)

    suspend fun setSavedTalkIds(ids: Set<String>) {
        store.putStringSet(KEY_SAVED_TALK_IDS, ids)
    }

    suspend fun clearSavedTalkIds() {
        store.remove(KEY_SAVED_TALK_IDS)
    }

    /** Un JSON ilegible (p. ej. de una versión anterior) cuenta como "sin caché". */
    private fun <T> decode(raw: String?, serializer: KSerializer<T>): T? =
        raw?.let { runCatching { json.decodeFromString(serializer, it) }.getOrNull() }

    companion object {
        const val KEY_AGENDA = "agenda_cache"
        const val KEY_EVENT = "event_cache"
        const val KEY_SAVED_TALK_IDS = "saved_talk_ids"
    }
}
