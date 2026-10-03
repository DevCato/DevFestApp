package pe.gdg.open.devfest.app.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Avisos de sesión a nivel de app. Cualquier repositorio que reciba `DataError.SessionExpired`
 * lo publica aquí; la app muestra "Tu sesión expiró" y vuelve a Login sin borrar las charlas
 * guardadas.
 */
class SessionEvents {
    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val expired: SharedFlow<Unit> = _expired.asSharedFlow()

    fun notifyExpired() {
        _expired.tryEmit(Unit)
    }
}
