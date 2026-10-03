@file:OptIn(ExperimentalCoroutinesApi::class)

package pe.gdg.open.devfest.app.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import pe.gdg.open.devfest.app.data.api.FakeScenario
import pe.gdg.open.devfest.app.domain.model.GemSourceType
import pe.gdg.open.devfest.app.domain.model.GemsInfo
import pe.gdg.open.devfest.app.domain.repository.DataError
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.testutil.DataFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GemsRepositoryInfoTest {

    private val fixture = DataFixture()
    private val users = UserRepositoryImpl(fixture.api, fixture.sessionEvents)
    private val repository = GemsRepositoryImpl(fixture.api, users, fixture.sessionEvents)

    @Test
    fun infoComesFromBackend() = runTest {
        val info = assertIs<Outcome.Success<GemsInfo>>(repository.info()).value

        assertEquals(listOf(GemSourceType.TALK, GemSourceType.STAND, GemSourceType.CHALLENGE), info.earnWays.map { it.type })
        assertEquals(listOf(20, 40, 80), info.earnWays.map { it.gems })
        assertEquals(listOf("Stickers", "Polos", "Certificaciones", "Cursos"), info.prizes.map { it.title })
    }

    @Test
    fun offlineInfoFails() = runTest {
        fixture.api.scenario = FakeScenario(networkFailure = true)

        assertEquals(Outcome.Failure(DataError.Offline), repository.info())
    }

    @Test
    fun sessionExpiredInfoIsPublished() = runTest {
        var published = 0
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { fixture.sessionEvents.expired.collect { published++ } }
        fixture.api.scenario = FakeScenario(sessionExpired = true)

        assertEquals(Outcome.Failure(DataError.SessionExpired), repository.info())
        assertEquals(1, published)
    }
}
