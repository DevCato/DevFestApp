package pe.gdg.open.devfest.app.data.api

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.AuthSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class FakeDevFestApiTest {

    private val session = AuthSession("me", "Ana Pérez", "https://example.com/ana.png", AuthProvider.GITHUB)
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-21T17:00:00Z")
    }

    private fun api(
        scenario: FakeScenario = FakeScenario(),
        identity: AuthSession? = session,
    ) = FakeDevFestApi(identity = { identity }, clock = clock, scenario = scenario)

    @Test
    fun agendaHasTwelveSessionsWithTwoLiveTalks() = runTest {
        val agenda = api().getAgenda()

        assertEquals(12, agenda.sessions.size)
        assertEquals(9, agenda.sessions.count { it.type == "talk" })
        assertEquals(3, agenda.sessions.count { it.type == "break" })
        assertEquals(setOf("t4", "t5"), agenda.sessions.filter { it.isLive }.map { it.id }.toSet())
    }

    @Test
    fun meUsesSessionIdentityAndInitialBalance() = runTest {
        val me = api().getMe()

        assertEquals("Ana Pérez", me.fullName)
        assertEquals("https://example.com/ana.png", me.photoUrl)
        assertEquals("github", me.provider)
        assertEquals(120, me.gems)
        assertEquals(12, me.rank)
        assertEquals(14, me.totalParticipants)
    }

    @Test
    fun talkCodeAwardsTwentyThenIsAlreadyUsed() = runTest {
        val api = api()

        val first = api.postScan(FakeData.QR_TALK)
        val second = api.postScan(FakeData.QR_TALK)

        assertIs<ScanResponseDto.Awarded>(first)
        assertEquals(20, first.body.gemsAwarded)
        assertEquals(140, first.body.newBalance)
        assertEquals("talk", first.body.source.type)
        assertIs<ScanResponseDto.AlreadyUsed>(second)
        assertEquals(140, second.body.balance)
    }

    @Test
    fun standAndChallengeCodesAwardTheirGems() = runTest {
        val api = api()

        val stand = api.postScan(FakeData.QR_STAND)
        val challenge = api.postScan(FakeData.QR_CHALLENGE)

        assertIs<ScanResponseDto.Awarded>(stand)
        assertEquals(40, stand.body.gemsAwarded)
        assertEquals(160, stand.body.newBalance)
        assertIs<ScanResponseDto.Awarded>(challenge)
        assertEquals(80, challenge.body.gemsAwarded)
        assertEquals(240, challenge.body.newBalance)
    }

    @Test
    fun unknownCodeIsInvalidAndDoesNotChangeBalance() = runTest {
        val api = api()

        val result = api.postScan("https://example.com")

        assertIs<ScanResponseDto.Invalid>(result)
        assertEquals("invalid_code", result.body.error.code)
        assertEquals(120, api.getMe().gems)
    }

    @Test
    fun scanningUpdatesRankAndRanking() = runTest {
        val api = api()

        api.postScan(FakeData.QR_CHALLENGE) // 120 + 80 = 200 → pasa a Participante I (190)

        assertEquals(9, api.getMe().rank)
        assertEquals(9, api.getRanking().me.position)
    }

    @Test
    fun rankingReturnsTopTenAndMeOutsideTop() = runTest {
        val ranking = api().getRanking()

        assertEquals(14, ranking.totalParticipants)
        assertEquals((1..10).toList(), ranking.top.map { it.position })
        assertEquals("Participante A", ranking.top.first().fullName)
        assertEquals(12, ranking.me.position)
        assertTrue(ranking.me.isMe)
        assertEquals("2026-11-21T17:00:00Z", ranking.updatedAt)
    }

    @Test
    fun tieWithUserPlacesUserAfterParticipant() = runTest {
        val api = api()
        api.postScan(FakeData.QR_STAND) // 160, empata con Participante J

        val ranking = api.getRanking()

        assertEquals("Participante J", ranking.top[9].fullName)
        assertEquals(11, ranking.me.position)
    }

    @Test
    fun emptyAgendaScenarioReturnsNoSessions() = runTest {
        val agenda = api(FakeScenario(emptyAgenda = true)).getAgenda()

        assertTrue(agenda.sessions.isEmpty())
    }

    @Test
    fun networkFailureScenarioThrowsNetwork() = runTest {
        val api = api(FakeScenario(networkFailure = true))

        assertFailsWith<ApiException.Network> { api.getAgenda() }
        assertFailsWith<ApiException.Network> { api.postScan(FakeData.QR_TALK) }
    }

    @Test
    fun sessionExpiredScenarioThrowsUnauthorized() = runTest {
        assertFailsWith<ApiException.Unauthorized> { api(FakeScenario(sessionExpired = true)).getMe() }
    }

    @Test
    fun withoutSessionAuthenticatedCallsAreUnauthorized() = runTest {
        assertFailsWith<ApiException.Unauthorized> { api(identity = null).getMe() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun latencyIsApplied() = runTest {
        val api = api(FakeScenario(latency = 2.seconds))

        api.getEvent()

        assertEquals(2000, testScheduler.currentTime)
    }

    @Test
    fun scenarioCanChangeAtRuntime() = runTest {
        val api = api()
        api.scenario = FakeScenario(networkFailure = true)

        assertFailsWith<ApiException.Network> { api.getEvent() }
    }
}
