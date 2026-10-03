package pe.gdg.open.devfest.app.data.repository

import kotlinx.coroutines.test.runTest
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.AuthSession
import pe.gdg.open.devfest.app.domain.repository.DataError
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.platform.FakeAuthGateway
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AuthRepositoryImplTest {

    private val existing = AuthSession("u1", "Ana Pérez", null, AuthProvider.APPLE)

    @Test
    fun noSessionAtStart() {
        val repository = AuthRepositoryImpl(FakeAuthGateway())

        assertNull(repository.session.value)
    }

    @Test
    fun sessionPersistedByProviderIsRestored() {
        val repository = AuthRepositoryImpl(FakeAuthGateway(initialSession = existing))

        assertEquals(existing, repository.session.value)
    }

    @Test
    fun successfulSignInPublishesSession() = runTest {
        val repository = AuthRepositoryImpl(FakeAuthGateway())

        val result = repository.signIn(AuthProvider.GITHUB)

        val session = assertIs<Outcome.Success<AuthSession>>(result).value
        assertEquals(AuthProvider.GITHUB, session.provider)
        assertEquals(session, repository.session.value)
    }

    @Test
    fun cancelledSignInFailsAndKeepsNoSession() = runTest {
        val repository = AuthRepositoryImpl(FakeAuthGateway(behavior = FakeAuthGateway.Behavior.CANCEL))

        val result = repository.signIn(AuthProvider.GOOGLE)

        assertEquals(Outcome.Failure(DataError.Unknown), result)
        assertNull(repository.session.value)
    }

    @Test
    fun failedSignInFails() = runTest {
        val repository = AuthRepositoryImpl(FakeAuthGateway(behavior = FakeAuthGateway.Behavior.FAIL))

        val result = repository.signIn(AuthProvider.APPLE)

        assertIs<Outcome.Failure>(result)
        assertNull(repository.session.value)
    }

    @Test
    fun signOutClearsSession() = runTest {
        val gateway = FakeAuthGateway(initialSession = existing)
        val repository = AuthRepositoryImpl(gateway)

        repository.signOut()

        assertNull(repository.session.value)
        assertNull(gateway.currentSession())
    }

    @Test
    fun idTokenComesFromGateway() = runTest {
        val repository = AuthRepositoryImpl(FakeAuthGateway(initialSession = existing))

        assertEquals("fake-id-token-u1", repository.idToken())
    }

    @Test
    fun idTokenIsNullWithoutSession() = runTest {
        assertNull(AuthRepositoryImpl(FakeAuthGateway()).idToken())
    }
}
