package pe.gdg.open.devfest.app.ui.screens.talk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.gdg.open.devfest.app.data.repository.AgendaRepositoryImpl
import pe.gdg.open.devfest.app.domain.logic.TalkPosition
import pe.gdg.open.devfest.app.testutil.DataFixture
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TalkDetailViewModelTest {

    private val fixture = DataFixture()
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
    fun showsTalkWithPosition() = runTest {
        agenda.refresh()

        val state = TalkDetailViewModel("t5", agenda).state.value

        assertEquals("Agentes con la Gemini API: del prototipo al producto", state.talk?.title)
        assertEquals(TalkPosition(5, 9), state.position)
        assertTrue(state.talk?.isLive == true)
        assertFalse(state.notFound)
    }

    @Test
    fun emptySpeakersMeansSpeakerToBeConfirmed() = runTest {
        agenda.refresh()

        val state = TalkDetailViewModel("t1", agenda).state.value

        assertTrue(state.speakerToBeConfirmed)
    }

    @Test
    fun toggleSavesAndRemoves() = runTest {
        agenda.refresh()
        val vm = TalkDetailViewModel("t2", agenda)

        vm.toggleSaved()
        assertTrue(vm.state.value.saved)
        assertEquals(setOf("t2"), agenda.savedTalkIds.first())

        vm.toggleSaved()
        assertFalse(vm.state.value.saved)
    }

    @Test
    fun missingTalkIsNotFound() = runTest {
        agenda.refresh()

        val state = TalkDetailViewModel("nope", agenda).state.value

        assertNull(state.talk)
        assertTrue(state.notFound)
    }

    @Test
    fun withoutCachedAgendaIsLoadingNotMissing() {
        val state = TalkDetailViewModel("t1", agenda).state.value

        assertNull(state.talk)
        assertFalse(state.notFound)
    }
}
