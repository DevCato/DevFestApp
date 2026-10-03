package pe.gdg.open.devfest.app.domain.repository

import kotlinx.coroutines.flow.StateFlow
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.AuthSession

interface AuthRepository {
    /** `null` = sin sesión. Persiste entre aperturas de la app. */
    val session: StateFlow<AuthSession?>

    /** Abre el flujo del proveedor. Un fallo o una cancelación devuelven [Outcome.Failure]. */
    suspend fun signIn(provider: AuthProvider): Outcome<AuthSession>

    suspend fun signOut()

    /** Token para el backend (Firebase ID token). No se usa con los datos falsos. */
    suspend fun idToken(): String?
}
