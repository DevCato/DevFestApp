package pe.gdg.open.devfest.app.ui.screens.gems

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.gdg.open.devfest.app.data.api.FakeScenario
import pe.gdg.open.devfest.app.data.repository.GemsRepositoryImpl
import pe.gdg.open.devfest.app.data.repository.UserRepositoryImpl
import pe.gdg.open.devfest.app.testutil.DataFixture
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
class GemsViewModelTest {

    private val fixture = DataFixture()
    private val users = UserRepositoryImpl(fixture.api, fixture.sessionEvents)
    private val viewModel by lazy { GemsViewModel(GemsRepositoryImpl(fixture.api, users, fixture.sessionEvents), users) }

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
        val state = viewModel.state.value

        assertNull(state.user)
        assertNull(state.info)
        assertTrue(state.showBalanceSkeleton)
    }

    @Test
    fun enteringLoadsBalanceRankAndInfo() {
        viewModel.onEnter()

        val state = viewModel.state.value
        assertEquals(120, state.user?.gems)
        assertEquals(12, state.user?.rank)
        assertEquals(14, state.user?.totalParticipants)
        assertEquals(3, state.info?.earnWays?.size)
        assertEquals(4, state.info?.prizes?.size)
        assertFalse(state.showBalanceSkeleton)
    }

    @Test
    fun failedLoadShowsUpdateFailedModal() = runTest {
        val modals = mutableListOf<ModalRequest>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.modals.toList(modals) }
        fixture.api.scenario = FakeScenario(networkFailure = true)

        viewModel.onEnter()

        assertIs<ModalRequest.UpdateFailed>(modals.single())
        assertFalse(viewModel.state.value.loading)
    }

    @Test
    fun balanceFollowsUserRepository() {
        viewModel.onEnter()

        users.setBalance(200)

        assertEquals(200, viewModel.state.value.user?.gems)
    }
}
