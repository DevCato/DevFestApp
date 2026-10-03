package pe.gdg.open.devfest.app.ui.screens.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.gdg.open.devfest.app.data.repository.AgendaRepositoryImpl
import pe.gdg.open.devfest.app.data.repository.AuthRepositoryImpl
import pe.gdg.open.devfest.app.data.repository.UserRepositoryImpl
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.AuthSession
import pe.gdg.open.devfest.app.platform.FakeAuthGateway
import pe.gdg.open.devfest.app.testutil.DataFixture
import pe.gdg.open.devfest.app.ui.modal.ModalRequest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val fixture = DataFixture()
    private val session = AuthSession("me", "Ana Pérez", null, AuthProvider.GITHUB)
    private val auth = AuthRepositoryImpl(FakeAuthGateway(initialSession = session))
    private val agenda = AgendaRepositoryImpl(fixture.api, fixture.store, fixture.sessionEvents)
    private val users = UserRepositoryImpl(fixture.api, fixture.sessionEvents)
    private val viewModel by lazy { ProfileViewModel(auth, users, agenda) }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.collectModals(): MutableList<ModalRequest> {
        val modals = mutableListOf<ModalRequest>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.modals.toList(modals) }
        return modals
    }

    @Test
    fun showsAccountAndMenuData() = runTest {
        agenda.refresh()
        agenda.toggleSaved("t2")
        agenda.toggleSaved("t5")

        viewModel.onEnter()

        val state = viewModel.state.value
        assertEquals("Ana Pérez", state.fullName)
        assertEquals(AuthProvider.GITHUB, state.provider)
        assertEquals(2, state.savedCount)
        assertEquals(120, state.gems)
        assertEquals(12, state.rank)
    }

    @Test
    fun logoutAsksConfirmationWithSavedCount() = runTest {
        val modals = collectModals()
        agenda.refresh()
        agenda.toggleSaved("t2")
        agenda.toggleSaved("t5")
        agenda.toggleSaved("t6")

        viewModel.onLogoutClick()

        assertEquals(3, assertIs<ModalRequest.ConfirmLogout>(modals.single()).savedTalks)
    }

    @Test
    fun logoutWithoutSavedTalksAsksWithZero() = runTest {
        val modals = collectModals()

        viewModel.onLogoutClick()

        assertEquals(0, assertIs<ModalRequest.ConfirmLogout>(modals.single()).savedTalks)
    }

    @Test
    fun confirmingClearsSavedTalksSignsOutAndLeaves() = runTest {
        val modals = collectModals()
        val events = mutableListOf<ProfileEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.toList(events) }
        agenda.refresh()
        agenda.toggleSaved("t2")
        viewModel.onLogoutClick()

        (modals.single() as ModalRequest.ConfirmLogout).onConfirm()

        assertTrue(agenda.savedTalkIds.first().isEmpty())
        assertNull(auth.session.value)
        assertEquals(listOf<ProfileEvent>(ProfileEvent.LoggedOut), events)
    }

    @Test
    fun cancellingChangesNothing() = runTest {
        collectModals()
        agenda.refresh()
        agenda.toggleSaved("t2")

        viewModel.onLogoutClick() // "Cancelar" solo cierra el modal

        assertEquals(setOf("t2"), agenda.savedTalkIds.first())
        assertEquals(session, auth.session.value)
    }
}
