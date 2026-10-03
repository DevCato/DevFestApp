package pe.gdg.open.devfest.app.platform

import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.AuthSession

/** [AuthGateway] de test que deja el inicio de sesión pendiente hasta que el test lo resuelve. */
class ControllableAuthGateway(private var session: AuthSession? = null) : AuthGateway {

    private var pending: ((AuthSignInResult) -> Unit)? = null
    var signInCalls = 0
        private set
    var signOutCalls = 0
        private set

    override fun currentSession(): AuthSession? = session

    override fun signIn(provider: AuthProvider, onResult: (AuthSignInResult) -> Unit) {
        signInCalls++
        pending = onResult
    }

    fun complete(result: AuthSignInResult) {
        if (result is AuthSignInResult.Success) session = result.session
        val callback = checkNotNull(pending) { "No sign-in in progress" }
        pending = null
        callback(result)
    }

    override fun signOut() {
        signOutCalls++
        session = null
    }

    override fun idToken(onResult: (String?) -> Unit) {
        onResult(session?.let { "token-${it.userId}" })
    }
}
