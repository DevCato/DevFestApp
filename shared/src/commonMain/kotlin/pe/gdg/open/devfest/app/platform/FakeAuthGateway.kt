package pe.gdg.open.devfest.app.platform

import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.AuthSession

/**
 * Login simulado: lo usan los tests y la app mientras no estén los archivos de configuración
 * de Firebase. La sesión vive solo en memoria.
 */
class FakeAuthGateway(
    initialSession: AuthSession? = null,
    var behavior: Behavior = Behavior.SUCCESS,
) : AuthGateway {

    enum class Behavior { SUCCESS, CANCEL, FAIL }

    private var session: AuthSession? = initialSession

    override fun currentSession(): AuthSession? = session

    override fun signIn(provider: AuthProvider, onResult: (AuthSignInResult) -> Unit) {
        val result = when (behavior) {
            Behavior.SUCCESS -> {
                val newSession = AuthSession(
                    userId = "fake-user",
                    fullName = "Asistente DevFest",
                    photoUrl = null,
                    provider = provider,
                )
                session = newSession
                AuthSignInResult.Success(newSession)
            }
            Behavior.CANCEL -> AuthSignInResult.Cancelled
            Behavior.FAIL -> AuthSignInResult.Failed("Fallo simulado")
        }
        onResult(result)
    }

    override fun signOut() {
        session = null
    }

    override fun idToken(onResult: (String?) -> Unit) {
        onResult(session?.let { "fake-id-token-${it.userId}" })
    }
}
