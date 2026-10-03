package pe.gdg.open.devfest.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** Ignora un segundo toque dentro de [minInterval]: un doble toque no duplica la acción. */
class TapGuard(private val minInterval: Duration = 400.milliseconds) {
    private var last: TimeMark? = null

    fun allow(): Boolean {
        val previous = last
        if (previous != null && previous.elapsedNow() < minInterval) return false
        last = TimeSource.Monotonic.markNow()
        return true
    }
}

/** Envuelve [action] para que un doble toque rápido la ejecute una sola vez. */
@Composable
fun rememberSingleTap(action: () -> Unit): () -> Unit {
    val guard = remember { TapGuard() }
    return { if (guard.allow()) action() }
}
