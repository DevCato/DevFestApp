package pe.gdg.open.devfest.app.platform

import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.AuthSession

/**
 * Inicio de sesión con el SDK de Firebase de cada plataforma (research.md, R1).
 *
 * Usa callbacks en lugar de `suspend` porque en iOS la implementa una clase Swift, y Swift no
 * puede implementar funciones `suspend` de una interfaz Kotlin. AuthRepositoryImpl la adapta a
 * corrutinas.
 */
interface AuthGateway {
    /** Sesión persistida por el proveedor; `null` si no hay. */
    fun currentSession(): AuthSession?

    fun signIn(provider: AuthProvider, onResult: (AuthSignInResult) -> Unit)

    fun signOut()

    /** Firebase ID token para el backend; `null` si no hay sesión. */
    fun idToken(onResult: (String?) -> Unit)
}

sealed interface AuthSignInResult {
    data class Success(val session: AuthSession) : AuthSignInResult

    /** El usuario cerró el flujo del proveedor. */
    data object Cancelled : AuthSignInResult

    data class Failed(val message: String?) : AuthSignInResult

    /**
     * El email ya está registrado con otro proveedor (Firebase permite una cuenta por email).
     * [existingProvider] es `null` si no se pudo averiguar cuál.
     */
    data class AccountExists(val existingProvider: AuthProvider?) : AuthSignInResult
}
