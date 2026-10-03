package pe.gdg.open.devfest.app.domain.repository

import kotlinx.coroutines.flow.StateFlow
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.AuthSession

sealed interface SignInOutcome {
    data class Success(val session: AuthSession) : SignInOutcome

    /** El email ya tiene cuenta con otro proveedor; [existingProvider] es `null` si no se sabe cuál. */
    data class AccountExists(val existingProvider: AuthProvider?) : SignInOutcome

    /** Cancelación o cualquier otro error: se muestra el mismo modal. */
    data object Failed : SignInOutcome
}

interface AuthRepository {
    /** `null` = sin sesión. Persiste entre aperturas de la app. */
    val session: StateFlow<AuthSession?>

    /** Abre el flujo del proveedor. */
    suspend fun signIn(provider: AuthProvider): SignInOutcome

    suspend fun signOut()

    /** Token para el backend (Firebase ID token). No se usa con los datos falsos. */
    suspend fun idToken(): String?
}
