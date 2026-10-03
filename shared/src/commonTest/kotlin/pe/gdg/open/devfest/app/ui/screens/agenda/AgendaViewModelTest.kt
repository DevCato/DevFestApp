package pe.gdg.open.devfest.app.ui.screens.agenda

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.gdg.open.devfest.app.data.api.FakeScenario
import pe.gdg.open.devfest.app.data.repository.AgendaRepositoryImpl
import pe.gdg.open.devfest.app.data.repository.EventRepositoryImpl
import pe.gdg.open.devfest.app.data.repository.UserRepositoryImpl
import pe.gdg.open.devfest.app.domain.logic.AgendaFilter
import pe.gdg.open.devfest.app.domain.logic.AgendaRow
import pe.gdg.open.devfest.app.testutil.DataFixture
import pe.gdg.open.devfest.app.testutil.TestClock
import pe.gdg.open.devfest.app.testutil.lima
import pe.gdg.open.devfest.app.ui.modal.ModalRequest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModelTest {

    private val fixture = DataFixture()
    private val agendaRepository = AgendaRepositoryImpl(fixture.api, fixture.store, fixture.sessionEvents)
    private val clock = TestClock(lima("11:40"))

    private fun viewModel() = AgendaViewModel(
        agenda = agendaRepository,
        events = EventRepositoryImpl(fixture.api, fixture.store, fixture.sessionEvents),
        users = UserRepositoryImpl(fixture.api, fixture.sessionEvents),
        now = clock,
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun skeletonBeforeFirstLoad() {
        assertEquals(AgendaStatus.SKELETON, viewModel().state.value.status)
    }

    @Test
    fun enteringLoadsAgendaAndUser() {
        val vm = viewModel()

        vm.onEnter()

        val state = vm.state.value
        assertEquals(AgendaStatus.CONTENT, state.status)
        assertTrue(state.rows.any { it is AgendaRow.NowRow })
        assertEquals(120, state.user?.gems)
    }

    @Test
    fun offlineWithoutCacheShowsOfflineState() {
        fixture.api.scenario = FakeScenario(networkFailure = true)
        val vm = viewModel()

        vm.onEnter()

        assertEquals(AgendaStatus.OFFLINE, vm.state.value.status)
    }

    @Test
    fun retryFromOfflineLoadsAgenda() {
        fixture.api.scenario = FakeScenario(networkFailure = true)
        val vm = viewModel()
        vm.onEnter()
        fixture.api.scenario = FakeScenario()

        vm.retry()

        assertEquals(AgendaStatus.CONTENT, vm.state.value.status)
        assertFalse(vm.state.value.retrying)
    }

    @Test
    fun failedRefreshWithCacheKeepsContentAndShowsUpdateFailedModal() = runTest {
        val vm = viewModel()
        vm.onEnter()
        val modals = mutableListOf<ModalRequest>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.modals.toList(modals) }
        fixture.api.scenario = FakeScenario(networkFailure = true)

        vm.refresh()

        assertEquals(AgendaStatus.CONTENT, vm.state.value.status)
        assertIs<ModalRequest.UpdateFailed>(modals.single())
        assertFalse(vm.state.value.refreshing)
    }

    @Test
    fun cachedAgendaIsShownWhileRefreshing() = runTest {
        viewModel().onEnter() // deja la agenda en caché
        fixture.api.scenario = FakeScenario(networkFailure = true)

        val vm = viewModel()

        assertEquals(AgendaStatus.CONTENT, vm.state.value.status)
    }

    @Test
    fun emptyAgendaShowsNoTalksState() {
        fixture.api.scenario = FakeScenario(emptyAgenda = true)
        val vm = viewModel()

        vm.onEnter()

        assertEquals(AgendaStatus.NO_TALKS, vm.state.value.status)
        assertFalse(vm.state.value.showFilters)
    }

    @Test
    fun filterShowsOnlyThatTrack() {
        val vm = viewModel()
        vm.onEnter()

        vm.selectFilter(AgendaFilter.CLOUD)

        val talks = vm.state.value.rows.filterIsInstance<AgendaRow.TalkRow>()
        assertEquals(setOf("t4", "t8"), talks.map { it.talk.id }.toSet())
        assertTrue(vm.state.value.rows.none { it is AgendaRow.NowRow || it is AgendaRow.BreakRow })
    }

    @Test
    fun showAllResetsFilter() {
        val vm = viewModel()
        vm.onEnter()
        vm.selectFilter(AgendaFilter.WEB)

        vm.selectFilter(AgendaFilter.ALL)

        assertEquals(AgendaFilter.ALL, vm.state.value.filter)
    }

    @Test
    fun liveTalksComeFromBackendFlag() {
        clock.now = lima("20:00", date = "2026-11-19") // lejos del evento
        val vm = viewModel()
        vm.onEnter()

        val live = vm.state.value.rows.filterIsInstance<AgendaRow.TalkRow>().filter { it.talk.isLive }
        assertEquals(setOf("t4", "t5"), live.map { it.talk.id }.toSet())
    }

    @Test
    fun pullToRefreshWhileRefreshingIsIgnored() {
        fixture.api.scenario = FakeScenario()
        val vm = viewModel()
        vm.onEnter()

        vm.refresh()
        vm.refresh()

        assertFalse(vm.state.value.refreshing)
        assertEquals(AgendaStatus.CONTENT, vm.state.value.status)
    }
}
