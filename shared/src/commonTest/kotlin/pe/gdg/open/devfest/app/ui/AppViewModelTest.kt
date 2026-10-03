package pe.gdg.open.devfest.app.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.gdg.open.devfest.app.data.api.FakeScenario
import pe.gdg.open.devfest.app.data.repository.AgendaRepositoryImpl
import pe.gdg.open.devfest.app.data.repository.AuthRepositoryImpl
import pe.gdg.open.devfest.app.data.repository.RankingRepositoryImpl
import pe.gdg.open.devfest.app.platform.FakeAuthGateway
import pe.gdg.open.devfest.app.testutil.DataFixture
import pe.gdg.open.devfest.app.testutil.testSession
import pe.gdg.open.devfest.app.ui.modal.ModalRequest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    private val fixture = DataFixture()
    private val auth = AuthRepositoryImpl(FakeAuthGateway(initialSession = testSession))
    private val agenda = AgendaRepositoryImpl(fixture.api, fixture.store, fixture.sessionEvents)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun expiredSessionShowsModalThenSignsOutKeepingSavedTalks() = runTest {
        val vm = AppViewModel(fixture.sessionEvents, auth)
        val modals = mutableListOf<ModalRequest>()
        val events = mutableListOf<AppEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.modals.toList(modals) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(events) }
        agenda.refresh()
        agenda.toggleSaved("t2")
        fixture.api.scenario = FakeScenario(sessionExpired = true)

        RankingRepositoryImpl(fixture.api, fixture.sessionEvents).ranking()
        val modal = assertIs<ModalRequest.SessionExpired>(modals.single())
        modal.onSignIn()

        assertNull(auth.session.value)
        assertEquals(listOf<AppEvent>(AppEvent.NavigateToLogin), events)
        assertEquals(setOf("t2"), agenda.savedTalkIds.first())
    }

    @Test
    fun severalExpiredCallsShowASingleModal() = runTest {
        val vm = AppViewModel(fixture.sessionEvents, auth)
        val modals = mutableListOf<ModalRequest>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.modals.toList(modals) }
        fixture.api.scenario = FakeScenario(sessionExpired = true)

        agenda.refresh()
        RankingRepositoryImpl(fixture.api, fixture.sessionEvents).ranking()

        assertEquals(1, modals.size)
    }
}
