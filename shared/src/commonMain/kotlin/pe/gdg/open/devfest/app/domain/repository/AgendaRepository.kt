package pe.gdg.open.devfest.app.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.gdg.open.devfest.app.domain.model.Agenda

interface AgendaRepository {
    /** Última agenda guardada; `null` si nunca se cargó. */
    val agenda: Flow<Agenda?>

    /**
     * Pide la agenda, la guarda y depura las charlas guardadas que ya no existen.
     * Un fallo nunca borra la agenda guardada.
     */
    suspend fun refresh(): Outcome<Unit>

    /** Charlas guardadas (solo locales). */
    val savedTalkIds: Flow<Set<String>>

    /** Guarda o quita una charla. Funciona sin conexión. */
    suspend fun toggleSaved(talkId: String)

    /** Borra todas las charlas guardadas (cierre de sesión confirmado). */
    suspend fun clearSaved()
}
