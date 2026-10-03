package pe.gdg.open.devfest.app.data.repository

import kotlinx.coroutines.test.runTest
import pe.gdg.open.devfest.app.data.api.FakeData
import pe.gdg.open.devfest.app.data.api.FakeScenario
import pe.gdg.open.devfest.app.domain.model.GemSourceType
import pe.gdg.open.devfest.app.domain.model.ScanResult
import pe.gdg.open.devfest.app.domain.repository.DataError
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.testutil.DataFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GemsRepositoryScanTest {

    private val fixture = DataFixture()
    private val users = UserRepositoryImpl(fixture.api, fixture.sessionEvents)
    private val repository = GemsRepositoryImpl(fixture.api, users, fixture.sessionEvents)

    @Test
    fun awardedSetsBalanceFromBackendAndRefreshesRank() = runTest {
        users.refresh()

        val result = assertIs<Outcome.Success<ScanResult>>(repository.scan(FakeData.QR_CHALLENGE)).value

        val awarded = assertIs<ScanResult.Awarded>(result)
        assertEquals(80, awarded.gemsAwarded)
        assertEquals(200, awarded.newBalance)
        assertEquals(GemSourceType.CHALLENGE, awarded.source.type)
        assertEquals(200, users.user.value?.gems)
        assertEquals(9, users.user.value?.rank)
    }

    @Test
    fun appNeverAddsOnItsOwn() = runTest {
        users.refresh()
        users.setBalance(999) // valor local desactualizado

        repository.scan(FakeData.QR_TALK)

        // Se muestra el saldo del backend (140), no 999 + 20.
        assertEquals(140, users.user.value?.gems)
    }

    @Test
    fun alreadyUsedIsAResultNotAFailure() = runTest {
        repository.scan(FakeData.QR_TALK)

        val result = assertIs<Outcome.Success<ScanResult>>(repository.scan(FakeData.QR_TALK)).value

        assertEquals(140, assertIs<ScanResult.AlreadyUsed>(result).balance)
    }

    @Test
    fun invalidIsAResultNotAFailure() = runTest {
        assertEquals(Outcome.Success(ScanResult.Invalid), repository.scan("https://example.com"))
    }

    @Test
    fun offlineDoesNotChangeBalance() = runTest {
        users.refresh()
        fixture.api.scenario = FakeScenario(networkFailure = true)

        assertEquals(Outcome.Failure(DataError.Offline), repository.scan(FakeData.QR_TALK))
        assertEquals(120, users.user.value?.gems)
    }
}
