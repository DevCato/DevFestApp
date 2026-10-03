package pe.gdg.open.devfest.app.domain.repository

import kotlinx.coroutines.flow.StateFlow
import pe.gdg.open.devfest.app.domain.model.User

interface UserRepository {
    /** Usuario con saldo y posición. */
    val user: StateFlow<User?>

    suspend fun refresh(): Outcome<Unit>

    /** Fija el saldo con el valor exacto devuelto por un escaneo. Nunca suma. */
    fun setBalance(gems: Int)
}
