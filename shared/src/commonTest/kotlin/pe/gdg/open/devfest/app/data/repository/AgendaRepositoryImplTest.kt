@file:OptIn(ExperimentalCoroutinesApi::class)

package pe.gdg.open.devfest.app.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import pe.gdg.open.devfest.app.data.api.FakeScenario
import pe.gdg.open.devfest.app.domain.repository.DataError
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.testutil.DataFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AgendaRepositoryImplTest {

    private val fixture = DataFixture()
    private val repository = AgendaRepositoryImpl(fixture.api, fixture.store, fixture.sessionEvents)

    @Test
    fun agendaIsNullWithoutCache() = runTest {
        assertNull(repository.agenda.first())
    }

    @Test
    fun refreshSavesAgendaInCache() = runTest {
        val result = repository.refresh()

        assertEquals(Outcome.Success(Unit), result)
        assertEquals(12, assertNotNull(repository.agenda.first()).sessions.size)
        assertNotNull(fixture.store.agenda.first())
    }

    @Test
    fun failedRefreshKeepsSavedAgenda() = runTest {
        repository.refresh()
        fixture.api.scenario = FakeScenario(networkFailure = true)

        val result = repository.refresh()

        assertEquals(Outcome.Failure(DataError.Offline), result)
        assertEquals(12, assertNotNull(repository.agenda.first()).sessions.size)
    }

    @Test
    fun offlineWithoutCacheFailsAndAgendaStaysNull() = runTest {
        fixture.api.scenario = FakeScenario(networkFailure = true)

        assertEquals(Outcome.Failure(DataError.Offline), repository.refresh())
        assertNull(repository.agenda.first())
    }

    @Test
    fun sessionExpiredIsReportedAndPublished() = runTest {
        var published = 0
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            fixture.sessionEvents.expired.collect { published++ }
        }
        fixture.api.scenario = FakeScenario(sessionExpired = true)

        assertEquals(Outcome.Failure(DataError.SessionExpired), repository.refresh())
        assertEquals(1, published)
    }
}
