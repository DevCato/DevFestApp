package pe.gdg.open.devfest.app.platform

import android.app.Activity
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.AuthSession
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Login con Firebase Auth en Android (research.md, R1): Credential Manager para Google y
 * `OAuthProvider` de Firebase para Apple y GitHub. Firebase persiste la sesión.
 *
 * @param webClientId `default_web_client_id` que genera el plugin google-services.
 */
class FirebaseAuthGateway(
    private val activities: CurrentActivityHolder,
    private val webClientId: String,
) : AuthGateway {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun currentSession(): AuthSession? = auth.currentUser?.toSession()

    override fun signIn(provider: AuthProvider, onResult: (AuthSignInResult) -> Unit) {
        val activity = activities.current
        if (activity == null) {
            onResult(AuthSignInResult.Failed("No hay una pantalla activa"))
            return
        }
        scope.launch {
            val result = try {
                val user = when (provider) {
                    AuthProvider.GOOGLE -> signInWithGoogle(activity)
                    AuthProvider.APPLE -> signInWithProvider(activity, APPLE_PROVIDER_ID, listOf("email", "name"))
                    AuthProvider.GITHUB -> signInWithProvider(activity, GITHUB_PROVIDER_ID, listOf("read:user", "user:email"))
                }
                if (user == null) Log.w(TAG, "signIn($provider): Firebase no devolvió usuario")
                user?.let { AuthSignInResult.Success(it.toSession(provider)) } ?: AuthSignInResult.Failed(null)
            } catch (e: GetCredentialCancellationException) {
                Log.i(TAG, "signIn($provider): cancelado por el usuario")
                AuthSignInResult.Cancelled
            } catch (e: CancellationException) {
                throw e
            } catch (e: FirebaseAuthUserCollisionException) {
                Log.w(TAG, "signIn($provider): ${e.errorCode} - email=${e.email}")
                if (e.errorCode == ERROR_ACCOUNT_EXISTS) {
                    AuthSignInResult.AccountExists(existingProviderFor(e.email, provider))
                } else {
                    AuthSignInResult.Failed(e.message)
                }
            } catch (e: FirebaseAuthException) {
                if (e.errorCode == ERROR_WEB_CONTEXT_CANCELED) {
                    Log.i(TAG, "signIn($provider): flujo web cerrado por el usuario")
                    AuthSignInResult.Cancelled
                } else {
                    Log.e(TAG, "signIn($provider) falló: ${e.errorCode} - ${e.message}", e)
                    AuthSignInResult.Failed(e.message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "signIn($provider) falló: ${e::class.simpleName} - ${e.message}", e)
                AuthSignInResult.Failed(e.message)
            }
            onResult(result)
        }
    }

    private suspend fun signInWithGoogle(activity: Activity): FirebaseUser? {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(webClientId).build())
            .build()
        val credential = CredentialManager.create(activity).getCredential(activity, request).credential
        require(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
        ) { "Unexpected credential type" }
        val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        return auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await().user
    }

    private suspend fun signInWithProvider(
        activity: Activity,
        providerId: String,
        scopes: List<String>,
    ): FirebaseUser? {
        // Si Android cerró la app durante el flujo web, el resultado queda pendiente.
        val pending = auth.pendingAuthResult
        Log.d(TAG, "signInWithProvider($providerId, $scopes): pendiente=${pending != null}")
        val task = pending
            ?: auth.startActivityForSignInWithProvider(
                activity,
                OAuthProvider.newBuilder(providerId).setScopes(scopes).build(),
            )
        return task.await().user
    }

    /**
     * Con qué proveedor ya está registrado [email]. Devuelve `null` si no se puede saber: con la
     * protección contra enumeración de emails de Firebase activa, la consulta llega vacía.
     */
    @Suppress("DEPRECATION")
    private suspend fun existingProviderFor(email: String?, attempted: AuthProvider): AuthProvider? {
        if (email.isNullOrBlank()) return null
        val methods = runCatching { auth.fetchSignInMethodsForEmail(email).await().signInMethods }
            .onFailure { Log.w(TAG, "fetchSignInMethodsForEmail falló", it) }
            .getOrNull()
            .orEmpty()
        Log.d(TAG, "Métodos registrados para el email: $methods")
        return methods.mapNotNull(::providerFromId).firstOrNull { it != attempted }
    }

    private fun providerFromId(id: String): AuthProvider? = when (id) {
        GOOGLE_PROVIDER_ID -> AuthProvider.GOOGLE
        APPLE_PROVIDER_ID -> AuthProvider.APPLE
        GITHUB_PROVIDER_ID -> AuthProvider.GITHUB
        else -> null
    }

    override fun signOut() {
        auth.signOut()
        // Olvida la cuenta de Google elegida para que la próxima vez se pueda elegir otra.
        val activity = activities.current ?: return
        scope.launch {
            runCatching { CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest()) }
        }
    }

    override fun idToken(onResult: (String?) -> Unit) {
        val user = auth.currentUser ?: return onResult(null)
        user.getIdToken(false)
            .addOnSuccessListener { onResult(it.token) }
            .addOnFailureListener { onResult(null) }
    }

    private fun FirebaseUser.toSession(provider: AuthProvider? = null): AuthSession = AuthSession(
        userId = uid,
        fullName = displayName?.takeIf { it.isNotBlank() } ?: email.orEmpty(),
        photoUrl = photoUrl?.toString(),
        provider = provider ?: providerFromData(),
    )

    private fun FirebaseUser.providerFromData(): AuthProvider {
        val ids = providerData.map { it.providerId }
        return when {
            APPLE_PROVIDER_ID in ids -> AuthProvider.APPLE
            GITHUB_PROVIDER_ID in ids -> AuthProvider.GITHUB
            else -> AuthProvider.GOOGLE
        }
    }

    private companion object {
        const val TAG = "DevFestAuth"
        const val GOOGLE_PROVIDER_ID = "google.com"
        const val APPLE_PROVIDER_ID = "apple.com"
        const val GITHUB_PROVIDER_ID = "github.com"
        const val ERROR_WEB_CONTEXT_CANCELED = "ERROR_WEB_CONTEXT_CANCELED"
        const val ERROR_ACCOUNT_EXISTS = "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL"
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
    addOnCanceledListener { continuation.cancel() }
}
