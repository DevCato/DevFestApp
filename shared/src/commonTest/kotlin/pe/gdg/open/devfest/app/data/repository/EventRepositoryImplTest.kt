package pe.gdg.open.devfest.app.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import pe.gdg.open.devfest.app.data.api.FakeScenario
import pe.gdg.open.devfest.app.domain.repository.DataError
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.testutil.DataFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EventRepositoryImplTest {

    private val fixture = DataFixture()
    private val repository = EventRepositoryImpl(fixture.api, fixture.store, fixture.sessionEvents)

    @Test
    fun eventIsNullWithoutCache() = runTest {
        assertNull(repository.event.first())
    }

    @Test
    fun refreshSavesEvent() = runTest {
        assertEquals(Outcome.Success(Unit), repository.refresh())

        assertEquals(LocalDate(2026, 11, 21), repository.event.first()?.date)
    }

    @Test
    fun offlineRefreshFails() = runTest {
        fixture.api.scenario = FakeScenario(networkFailure = true)

        assertEquals(Outcome.Failure(DataError.Offline), repository.refresh())
    }
}
