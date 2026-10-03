package pe.gdg.open.devfest.app.ui.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pe.gdg.open.devfest.app.ui.theme.EaseOutStrong
import pe.gdg.open.devfest.app.ui.theme.LocalReduceMotion

/**
 * Parte de una cabecera que se esconde al bajar por la lista y vuelve al subir, para dejar sitio a las charlas.
 * Sigue al dedo 1:1 (no es una animación con tiempo); solo al soltar se asienta, abierta o cerrada,
 * para que nunca quede a medias.
 */
@Stable
class CollapsingHeaderState internal constructor(private val scope: CoroutineScope) {
    /** Alto medido de la parte que se esconde, en px. */
    var heightPx by mutableFloatStateOf(0f)
        private set

    /** Desplazamiento actual: 0 = visible, -[heightPx] = escondida. */
    var offsetPx by mutableFloatStateOf(0f)
        private set

    /** 0 = visible, 1 = escondida. */
    val collapsedFraction: Float
        get() = if (heightPx == 0f) 0f else -offsetPx / heightPx

    internal var reduceMotion = false
    private var settleJob: Job? = null

    internal fun updateHeight(height: Float) {
        heightPx = height
        offsetPx = offsetPx.coerceIn(-height, 0f)
    }

    private fun moveBy(delta: Float) {
        offsetPx = (offsetPx + delta).coerceIn(-heightPx, 0f)
    }

    private fun settle() {
        if (offsetPx == 0f || offsetPx == -heightPx) return
        animateTo(if (offsetPx > -heightPx / 2) 0f else -heightPx)
    }

    /** Vuelve a mostrarla entera, p. ej. cuando la lista cambia y empieza otra vez desde arriba. */
    fun expand() {
        if (offsetPx != 0f) animateTo(0f)
    }

    private fun animateTo(target: Float) {
        settleJob?.cancel()
        if (reduceMotion) {
            offsetPx = target
            return
        }
        settleJob = scope.launch {
            animate(offsetPx, target, animationSpec = tween(SETTLE_MILLIS, easing = EaseOutStrong)) { value, _ ->
                offsetPx = value
            }
        }
    }

    /** Se coloca entre la lista y su pull-to-refresh, así solo ve lo que la lista desplazó de verdad. */
    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            // Un nuevo arrastre manda sobre el asentamiento en curso.
            if (source == NestedScrollSource.UserInput) settleJob?.cancel()
            // Se mueve con el contenido; si la lista ya llegó arriba, el resto termina de mostrarla.
            moveBy(consumed.y + available.y.coerceAtLeast(0f))
            return Offset.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            settle()
            return Velocity.Zero
        }
    }

    private companion object {
        const val SETTLE_MILLIS = 220
    }
}

@Composable
fun rememberCollapsingHeaderState(): CollapsingHeaderState {
    val scope = rememberCoroutineScope()
    val state = remember(scope) { CollapsingHeaderState(scope) }
    val reduceMotion = LocalReduceMotion.current
    SideEffect { state.reduceMotion = reduceMotion }
    return state
}

/** Marca la parte que se esconde: su alto es cuánto puede subir la cabecera. */
fun Modifier.collapsible(state: CollapsingHeaderState): Modifier =
    onSizeChanged { state.updateHeight(it.height.toFloat()) }

/** Sube con transform lo que se esconde y lo que va debajo: nada se vuelve a medir al hacer scroll. */
fun Modifier.collapsingOffset(state: CollapsingHeaderState): Modifier =
    graphicsLayer { translationY = state.offsetPx }

/**
 * Para lo que se esconde: se desvanece, se encoge un poco y sube más lento que el resto, así se
 * va antes de llegar a lo que queda fijo en vez de cortarse contra ello. Va ligado al scroll, así
 * que entra por el mismo recorrido a la inversa.
 *
 * [fadeEnd] es la fracción del recorrido en la que ya es invisible: con valores distintos, unos
 * elementos se van antes que otros. Con "reducir movimiento" solo queda el fundido.
 */
fun Modifier.collapseFade(
    state: CollapsingHeaderState,
    fadeEnd: Float,
    minScale: Float,
    origin: TransformOrigin = TransformOrigin.Center,
): Modifier = graphicsLayer {
    val visible = 1f - (state.collapsedFraction / fadeEnd).coerceIn(0f, 1f)
    alpha = visible
    if (!state.reduceMotion) {
        val scale = minScale + (1f - minScale) * visible
        scaleX = scale
        scaleY = scale
        transformOrigin = origin
        // Contrarresta parte de la subida del contenedor: efecto parallax.
        translationY = -state.offsetPx * PARALLAX
    }
}

private const val PARALLAX = 0.5f
