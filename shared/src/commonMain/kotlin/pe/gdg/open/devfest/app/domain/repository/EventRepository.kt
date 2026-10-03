package pe.gdg.open.devfest.app.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.gdg.open.devfest.app.domain.model.Event

interface EventRepository {
    /** Último evento guardado; `null` si nunca se cargó. */
    val event: Flow<Event?>

    suspend fun refresh(): Outcome<Unit>
}
