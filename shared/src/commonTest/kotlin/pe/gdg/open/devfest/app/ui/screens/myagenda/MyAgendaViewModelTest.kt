package pe.gdg.open.devfest.app.ui.screens.myagenda

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.gdg.open.devfest.app.data.repository.AgendaRepositoryImpl
import pe.gdg.open.devfest.app.data.repository.UserRepositoryImpl
import pe.gdg.open.devfest.app.domain.logic.FeaturedKind
import pe.gdg.open.devfest.app.testutil.DataFixture
import pe.gdg.open.devfest.app.testutil.TestClock
import pe.gdg.open.devfest.app.testutil.lima
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MyAgendaViewModelTest {

    private val fixture = DataFixture()
    private val agenda = AgendaRepositoryImpl(fixture.api, fixture.store, fixture.sessionEvents)
    private val clock = TestClock(lima("09:00"))

    private fun viewModel() = MyAgendaViewModel(
        agenda = agenda,
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
    fun emptyWhenNothingSaved() = runTest {
        agenda.refresh()
        val vm = viewModel()

        assertTrue(vm.state.value.isEmpty)
        assertEquals(0, vm.state.value.savedCount)
        assertNull(vm.state.value.featured)
    }

    @Test
    fun savedTalksAreListedByStartTimeWithConflicts() = runTest {
        agenda.refresh()
        agenda.toggleSaved("t6")
        agenda.toggleSaved("t3")
        agenda.toggleSaved("t2")
        val vm = viewModel()

        val items = vm.state.value.items
        assertEquals(listOf("t2", "t3", "t6"), items.map { it.talk.id })
        assertEquals(listOf(true, true, false), items.map { it.conflict })
        assertEquals(listOf(true, false, true), items.map { it.showTime })
        assertEquals(3, vm.state.value.savedCount)
    }

    @Test
    fun featuredIsLiveTalkWhenAnySavedIsLive() = runTest {
        agenda.refresh()
        agenda.toggleSaved("t2")
        agenda.toggleSaved("t5") // en vivo según el backend
        val vm = viewModel()

        assertEquals("t5", vm.state.value.featured?.talk?.id)
        assertEquals(FeaturedKind.IN_PROGRESS, vm.state.value.featured?.kind)
    }

    @Test
    fun featuredIsNextTalkWhenNoneIsLive() = runTest {
        agenda.refresh()
        agenda.toggleSaved("t2")
        agenda.toggleSaved("t6")
        val vm = viewModel()

        assertEquals("t2", vm.state.value.featured?.talk?.id)
        assertEquals(FeaturedKind.NEXT, vm.state.value.featured?.kind)
    }

    @Test
    fun removingFromMyAgendaUpdatesList() = runTest {
        agenda.refresh()
        agenda.toggleSaved("t2")
        val vm = viewModel()

        vm.toggleSaved("t2")

        assertTrue(vm.state.value.isEmpty)
    }

    @Test
    fun enteringRefreshesAgenda() {
        val vm = viewModel()

        vm.onEnter()

        assertFalse(vm.state.value.loading)
    }
}
