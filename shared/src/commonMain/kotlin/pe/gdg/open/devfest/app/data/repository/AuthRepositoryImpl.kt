package pe.gdg.open.devfest.app.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.AuthSession
import pe.gdg.open.devfest.app.domain.repository.AuthRepository
import pe.gdg.open.devfest.app.domain.repository.SignInOutcome
import pe.gdg.open.devfest.app.platform.AuthGateway
import pe.gdg.open.devfest.app.platform.AuthSignInResult
import kotlin.coroutines.resume

/** Adapta el [AuthGateway] de cada plataforma (callbacks) a corrutinas. */
class AuthRepositoryImpl(private val gateway: AuthGateway) : AuthRepository {

    private val _session = MutableStateFlow(gateway.currentSession())
    override val session: StateFlow<AuthSession?> = _session.asStateFlow()

    /** Cancelar el flujo del proveedor también es un fallo: se muestra el mismo modal. */
    override suspend fun signIn(provider: AuthProvider): SignInOutcome {
        val result = suspendCancellableCoroutine { continuation ->
            gateway.signIn(provider) { result ->
                if (continuation.isActive) continuation.resume(result)
            }
        }
        return when (result) {
            is AuthSignInResult.Success -> {
                _session.value = result.session
                SignInOutcome.Success(result.session)
            }
            is AuthSignInResult.AccountExists -> SignInOutcome.AccountExists(result.existingProvider)
            AuthSignInResult.Cancelled, is AuthSignInResult.Failed -> SignInOutcome.Failed
        }
    }

    override suspend fun signOut() {
        gateway.signOut()
        _session.value = null
    }

    override suspend fun idToken(): String? = suspendCancellableCoroutine { continuation ->
        gateway.idToken { token ->
            if (continuation.isActive) continuation.resume(token)
        }
    }
}
