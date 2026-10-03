package pe.gdg.open.devfest.app.data.api

import kotlin.time.Duration

/**
 * Condiciones que simula [FakeDevFestApi]. Las usan los tests y la validación manual
 * (quickstart.md).
 *
 * @property latency espera antes de cada respuesta.
 * @property networkFailure todas las llamadas fallan como sin conexión.
 * @property emptyAgenda la agenda llega sin sesiones.
 * @property sessionExpired todas las llamadas responden 401.
 */
data class FakeScenario(
    val latency: Duration = Duration.ZERO,
    val networkFailure: Boolean = false,
    val emptyAgenda: Boolean = false,
    val sessionExpired: Boolean = false,
)
