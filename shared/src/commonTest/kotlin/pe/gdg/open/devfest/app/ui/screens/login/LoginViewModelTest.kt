package pe.gdg.open.devfest.app.ui.screens.login

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.gdg.open.devfest.app.data.repository.AuthRepositoryImpl
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.AuthSession
import pe.gdg.open.devfest.app.platform.AuthSignInResult
import pe.gdg.open.devfest.app.platform.ControllableAuthGateway
import pe.gdg.open.devfest.app.ui.modal.ModalRequest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val gateway = ControllableAuthGateway()
    private val viewModel by lazy { LoginViewModel(AuthRepositoryImpl(gateway)) }
    private val session = AuthSession("u1", "Ana Pérez", null, AuthProvider.GOOGLE)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun idleAtStart() {
        val state = viewModel.state.value

        assertNull(state.connecting)
        assertFalse(state.busy)
    }

    @Test
    fun tappedProviderShowsConnectingAndOthersAreDisabled() {
        viewModel.signIn(AuthProvider.APPLE)

        val state = viewModel.state.value
        assertEquals(AuthProvider.APPLE, state.connecting)
        assertTrue(state.isConnecting(AuthProvider.APPLE))
        assertFalse(state.isEnabled(AuthProvider.GOOGLE))
        assertFalse(state.isEnabled(AuthProvider.GITHUB))
    }

    @Test
    fun secondTapWhileConnectingIsIgnored() {
        viewModel.signIn(AuthProvider.GOOGLE)
        viewModel.signIn(AuthProvider.GOOGLE)
        viewModel.signIn(AuthProvider.GITHUB)

        assertEquals(1, gateway.signInCalls)
        assertEquals(AuthProvider.GOOGLE, viewModel.state.value.connecting)
    }

    @Test
    fun successNavigatesToAgenda() = runTest {
        val events = mutableListOf<LoginEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.toList(events) }

        viewModel.signIn(AuthProvider.GOOGLE)
        gateway.complete(AuthSignInResult.Success(session))

        assertEquals(listOf<LoginEvent>(LoginEvent.SignedIn), events)
        assertNull(viewModel.state.value.connecting)
    }

    @Test
    fun cancellationShowsLoginFailedModalAndReenablesButtons() = runTest {
        val modals = mutableListOf<ModalRequest>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.modals.toList(modals) }

        viewModel.signIn(AuthProvider.GITHUB)
        gateway.complete(AuthSignInResult.Cancelled)

        assertIs<ModalRequest.LoginFailed>(modals.single())
        assertFalse(viewModel.state.value.busy)
        assertTrue(viewModel.state.value.isEnabled(AuthProvider.GOOGLE))
    }

    @Test
    fun failureShowsLoginFailedModal() = runTest {
        val modals = mutableListOf<ModalRequest>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.modals.toList(modals) }

        viewModel.signIn(AuthProvider.APPLE)
        gateway.complete(AuthSignInResult.Failed("boom"))

        assertIs<ModalRequest.LoginFailed>(modals.single())
    }

    @Test
    fun retryFromModalSignsInAgainWithSameProvider() = runTest {
        val modals = mutableListOf<ModalRequest>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.modals.toList(modals) }
        viewModel.signIn(AuthProvider.GITHUB)
        gateway.complete(AuthSignInResult.Cancelled)

        (modals.single() as ModalRequest.LoginFailed).onRetry()

        assertEquals(2, gateway.signInCalls)
        assertEquals(AuthProvider.GITHUB, viewModel.state.value.connecting)
    }

    @Test
    fun useOtherAccountSignsOutOfProviderAndStaysIdle() = runTest {
        val modals = mutableListOf<ModalRequest>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.modals.toList(modals) }
        viewModel.signIn(AuthProvider.GOOGLE)
        gateway.complete(AuthSignInResult.Failed(null))

        (modals.single() as ModalRequest.LoginFailed).onUseOtherAccount()

        assertEquals(1, gateway.signOutCalls)
        assertFalse(viewModel.state.value.busy)
    }
}
