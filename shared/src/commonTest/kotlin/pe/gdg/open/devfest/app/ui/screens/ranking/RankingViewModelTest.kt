package pe.gdg.open.devfest.app.ui.screens.ranking

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.gdg.open.devfest.app.data.api.FakeScenario
import pe.gdg.open.devfest.app.data.repository.RankingRepositoryImpl
import pe.gdg.open.devfest.app.domain.model.Ranking
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.domain.repository.RankingRepository
import pe.gdg.open.devfest.app.testutil.DataFixture
import pe.gdg.open.devfest.app.ui.modal.ModalRequest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RankingViewModelTest {

    private val fixture = DataFixture()
    private val viewModel by lazy { RankingViewModel(RankingRepositoryImpl(fixture.api, fixture.sessionEvents)) }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun skeletonBeforeLoading() {
        assertNull(viewModel.state.value.view)
    }

    @Test
    fun enteringLoadsRankingWithoutJustRefreshedLabel() {
        viewModel.onEnter()

        val state = viewModel.state.value
        assertEquals(12, assertNotNull(state.view).me.position)
        assertFalse(state.justRefreshed)
        assertFalse(state.refreshing)
    }

    @Test
    fun refreshShowsUpdatedMoment() {
        viewModel.onEnter()

        viewModel.refresh()

        assertTrue(viewModel.state.value.justRefreshed)
        assertFalse(viewModel.state.value.refreshing)
    }

    @Test
    fun refreshingDisablesButtonAndIgnoresSecondTap() = runTest {
        val gate = CompletableDeferred<Unit>()
        var calls = 0
        val real = RankingRepositoryImpl(fixture.api, fixture.sessionEvents)
        val slow = object : RankingRepository {
            override suspend fun ranking(): Outcome<Ranking> {
                calls++
                gate.await()
                return real.ranking()
            }
        }
        val vm = RankingViewModel(slow)

        vm.refresh()
        vm.refresh()

        assertTrue(vm.state.value.refreshing)
        assertEquals(1, calls)
        gate.complete(Unit)
    }

    @Test
    fun failedRefreshKeepsRankingAndShowsModal() = runTest {
        val modals = mutableListOf<ModalRequest>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.modals.toList(modals) }
        viewModel.onEnter()
        fixture.api.scenario = FakeScenario(networkFailure = true)

        viewModel.refresh()

        assertIs<ModalRequest.UpdateFailed>(modals.single())
        assertNotNull(viewModel.state.value.view)
        assertFalse(viewModel.state.value.justRefreshed)
    }
}
