package pe.gdg.open.devfest.app

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object AppConfig {
    /** URL de los Términos. La define el equipo; mientras esté vacía el enlace no abre nada. */
    const val TERMS_URL: String = ""

    /** URL de la Política de privacidad. La define el equipo. */
    const val PRIVACY_URL: String = ""

    /** Espera de las respuestas de los datos falsos, para ver los estados de carga. */
    val fakeLatency: Duration = 600.milliseconds
}
