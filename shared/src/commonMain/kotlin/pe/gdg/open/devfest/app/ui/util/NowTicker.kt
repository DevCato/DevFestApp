package pe.gdg.open.devfest.app.ui.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Hora del teléfono para las pantallas. Los tests usan una fuente que emite una sola vez.
 */
fun interface NowSource {
    fun ticks(): Flow<Instant>
}

/**
 * Reemite la hora cada [period] para que la línea AHORA, el atenuado de sesiones pasadas y
 * "Tu próxima charla" avancen solos mientras la pantalla está abierta.
 */
class TickingNowSource(
    private val clock: Clock,
    private val period: Duration = 30.seconds,
) : NowSource {
    override fun ticks(): Flow<Instant> = flow {
        while (true) {
            emit(clock.now())
            delay(period)
        }
    }
}
