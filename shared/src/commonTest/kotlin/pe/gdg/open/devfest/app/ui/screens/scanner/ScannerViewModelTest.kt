package pe.gdg.open.devfest.app.ui.screens.scanner

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.gdg.open.devfest.app.data.api.FakeData
import pe.gdg.open.devfest.app.data.api.FakeScenario
import pe.gdg.open.devfest.app.data.repository.GemsRepositoryImpl
import pe.gdg.open.devfest.app.data.repository.UserRepositoryImpl
import pe.gdg.open.devfest.app.domain.model.GemSourceType
import pe.gdg.open.devfest.app.domain.model.GemsInfo
import pe.gdg.open.devfest.app.domain.model.ScanResult
import pe.gdg.open.devfest.app.domain.repository.GemsRepository
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.testutil.DataFixture
import pe.gdg.open.devfest.app.ui.modal.ModalRequest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ScannerViewModelTest {

    private val fixture = DataFixture()
    private val users = UserRepositoryImpl(fixture.api, fixture.sessionEvents)
    private val viewModel by lazy { ScannerViewModel(GemsRepositoryImpl(fixture.api, users, fixture.sessionEvents)) }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.collectModals(vm: ScannerViewModel): MutableList<ModalRequest> {
        val modals = mutableListOf<ModalRequest>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.modals.toList(modals) }
        return modals
    }

    private fun TestScope.collectEvents(vm: ScannerViewModel): MutableList<ScannerEvent> {
        val events = mutableListOf<ScannerEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(events) }
        return events
    }

    @Test
    fun validCodeShowsAwardedModalWithBackendValues() = runTest {
        val modals = collectModals(viewModel)

        viewModel.onCodeScanned(FakeData.QR_STAND)

        val modal = assertIs<ModalRequest.GemsAwarded>(modals.single())
        assertEquals(40, modal.gemsAwarded)
        assertEquals(160, modal.newBalance)
        assertEquals(GemSourceType.STAND, modal.sourceType)
    }

    @Test
    fun usedCodeShowsAlreadyUsedModal() = runTest {
        val modals = collectModals(viewModel)
        viewModel.onCodeScanned(FakeData.QR_TALK)
        (modals.single() as ModalRequest.GemsAwarded).onKeepScanning()

        viewModel.onCodeScanned(FakeData.QR_TALK)

        assertIs<ModalRequest.QrAlreadyUsed>(modals.last())
    }

    @Test
    fun invalidCodeShowsInvalidModal() = runTest {
        val modals = collectModals(viewModel)

        viewModel.onCodeScanned("otro-qr")

        assertIs<ModalRequest.QrInvalid>(modals.single())
    }

    @Test
    fun offlineShowsNoConnectionModalAndRetryScansSameCode() = runTest {
        val modals = collectModals(viewModel)
        fixture.api.scenario = FakeScenario(networkFailure = true)
        viewModel.onCodeScanned(FakeData.QR_TALK)
        val offline = assertIs<ModalRequest.NoConnection>(modals.single())
        fixture.api.scenario = FakeScenario()

        offline.onRetry()

        assertIs<ModalRequest.GemsAwarded>(modals.last())
    }

    @Test
    fun readingsAreIgnoredUntilUserResumes() = runTest {
        val modals = collectModals(viewModel)
        viewModel.onCodeScanned("otro-qr")

        viewModel.onCodeScanned("otro-qr")
        viewModel.onCodeScanned(FakeData.QR_TALK)

        assertEquals(1, modals.size)
        assertFalse(viewModel.state.value.acceptingCodes)
    }

    @Test
    fun keepScanningResumes() = runTest {
        val modals = collectModals(viewModel)
        viewModel.onCodeScanned(FakeData.QR_TALK)

        (modals.single() as ModalRequest.GemsAwarded).onKeepScanning()

        assertTrue(viewModel.state.value.acceptingCodes)
    }

    @Test
    fun doneClosesScanner() = runTest {
        val modals = collectModals(viewModel)
        val events = collectEvents(viewModel)
        viewModel.onCodeScanned(FakeData.QR_TALK)

        (modals.single() as ModalRequest.GemsAwarded).onDone()

        assertEquals(listOf<ScannerEvent>(ScannerEvent.Close), events)
    }

    @Test
    fun readingsWhileValidatingAreIgnored() = runTest {
        val gate = CompletableDeferred<Unit>()
        var calls = 0
        val slow = object : GemsRepository {
            override suspend fun info(): Outcome<GemsInfo> = error("unused")
            override suspend fun scan(code: String): Outcome<ScanResult> {
                calls++
                gate.await()
                return Outcome.Success(ScanResult.Invalid)
            }
        }
        val vm = ScannerViewModel(slow)

        vm.onCodeScanned("a")
        vm.onCodeScanned("a")
        vm.onCodeScanned("b")

        assertTrue(vm.state.value.validating)
        assertEquals(1, calls)
        gate.complete(Unit)
    }

    @Test
    fun deniedPermissionShowsCameraModal() = runTest {
        val modals = collectModals(viewModel)
        val events = collectEvents(viewModel)

        viewModel.onPermissionDenied()
        val modal = assertIs<ModalRequest.CameraPermission>(modals.single())
        modal.onOpenSettings()
        modal.onNotNow()

        assertEquals(listOf(ScannerEvent.OpenSettings, ScannerEvent.Close), events)
    }
}
