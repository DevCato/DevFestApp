package pe.gdg.open.devfest.app.data.repository

import kotlinx.coroutines.test.runTest
import pe.gdg.open.devfest.app.data.api.FakeScenario
import pe.gdg.open.devfest.app.domain.model.Ranking
import pe.gdg.open.devfest.app.domain.repository.DataError
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.testutil.DataFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RankingRepositoryImplTest {

    private val fixture = DataFixture()
    private val repository = RankingRepositoryImpl(fixture.api, fixture.sessionEvents)

    @Test
    fun rankingKeepsBackendOrder() = runTest {
        val ranking = assertIs<Outcome.Success<Ranking>>(repository.ranking()).value

        assertEquals((1..10).toList(), ranking.top.map { it.position })
        assertEquals("Participante A", ranking.top.first().fullName)
        assertEquals(12, ranking.me.position)
        assertEquals(14, ranking.totalParticipants)
    }

    @Test
    fun offlineFails() = runTest {
        fixture.api.scenario = FakeScenario(networkFailure = true)

        assertEquals(Outcome.Failure(DataError.Offline), repository.ranking())
    }

    @Test
    fun sessionExpiredFails() = runTest {
        fixture.api.scenario = FakeScenario(sessionExpired = true)

        assertEquals(Outcome.Failure(DataError.SessionExpired), repository.ranking())
    }
}
