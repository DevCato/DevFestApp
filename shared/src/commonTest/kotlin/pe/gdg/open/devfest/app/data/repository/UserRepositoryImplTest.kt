package pe.gdg.open.devfest.app.data.repository

import kotlinx.coroutines.test.runTest
import pe.gdg.open.devfest.app.data.api.FakeScenario
import pe.gdg.open.devfest.app.domain.repository.DataError
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.testutil.DataFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserRepositoryImplTest {

    private val fixture = DataFixture()
    private val repository = UserRepositoryImpl(fixture.api, fixture.sessionEvents)

    @Test
    fun userIsNullUntilRefreshed() {
        assertNull(repository.user.value)
    }

    @Test
    fun refreshLoadsUserWithBalanceAndRank() = runTest {
        assertEquals(Outcome.Success(Unit), repository.refresh())

        val user = assertNotNull(repository.user.value)
        assertEquals("Ana Pérez", user.fullName)
        assertEquals(120, user.gems)
        assertEquals(12, user.rank)
        assertEquals(14, user.totalParticipants)
    }

    @Test
    fun setBalanceUsesExactValue() = runTest {
        repository.refresh()

        repository.setBalance(240)

        assertEquals(240, repository.user.value?.gems)
    }

    @Test
    fun failedRefreshKeepsLastUser() = runTest {
        repository.refresh()
        fixture.api.scenario = FakeScenario(networkFailure = true)

        assertEquals(Outcome.Failure(DataError.Offline), repository.refresh())
        assertEquals(120, repository.user.value?.gems)
    }
}
