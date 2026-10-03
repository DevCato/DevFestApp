package pe.gdg.open.devfest.app.testutil

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pe.gdg.open.devfest.app.ui.util.NowSource
import pe.gdg.open.devfest.app.data.SessionEvents
import pe.gdg.open.devfest.app.data.api.FakeDevFestApi
import pe.gdg.open.devfest.app.data.api.FakeScenario
import pe.gdg.open.devfest.app.data.local.AgendaLocalStore
import pe.gdg.open.devfest.app.data.local.InMemoryKeyValueStore
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.AuthSession
import pe.gdg.open.devfest.app.domain.model.Break
import pe.gdg.open.devfest.app.domain.model.DefaultTracks
import pe.gdg.open.devfest.app.domain.model.Level
import pe.gdg.open.devfest.app.domain.model.Talk
import pe.gdg.open.devfest.app.domain.model.Track
import kotlin.time.Clock
import kotlin.time.Instant

/** Reloj que el test mueve a mano. */
class TestClock(var now: Instant) : Clock, NowSource {
    override fun now(): Instant = now

    /** Emite la hora actual una sola vez (sin el bucle de producción). */
    override fun ticks(): Flow<Instant> = flowOf(now)
}

/** Instante del 21 de noviembre de 2026 en hora de Lima: `lima("11:40")`. */
fun lima(time: String, date: String = "2026-11-21"): Instant = Instant.parse("${date}T$time:00-05:00")

fun talk(
    id: String,
    start: String,
    end: String,
    track: Track = DefaultTracks.IA,
    isLive: Boolean = false,
) = Talk(
    id = id,
    title = "Charla $id",
    startsAt = lima(start),
    endsAt = lima(end),
    room = "Sala 1",
    track = track,
    level = Level.INTERMEDIO,
    description = "",
    topics = emptyList(),
    speakers = emptyList(),
    isLive = isLive,
)

fun pause(id: String, start: String, end: String) =
    Break(id = id, title = "Pausa $id", startsAt = lima(start), endsAt = lima(end))

val testSession = AuthSession("me", "Ana Pérez", null, AuthProvider.GOOGLE)

/** Backend falso, almacenamiento en memoria y eventos de sesión, listos para los repositorios. */
class DataFixture(scenario: FakeScenario = FakeScenario()) {
    val api = FakeDevFestApi(identity = { testSession }, scenario = scenario)
    val kv = InMemoryKeyValueStore()
    val store = AgendaLocalStore(kv)
    val sessionEvents = SessionEvents()
}
