package pe.gdg.open.devfest.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pe.gdg.open.devfest.app.ui.theme.EaseOutStrong
import pe.gdg.open.devfest.app.ui.theme.LocalReduceMotion

/**
 * Entrada escalonada del prototipo: sube 14 dp y aparece en 420 ms tras [delayMillis].
 * Con "reducir movimiento", solo fundido.
 */
@Composable
fun Modifier.riseIn(delayMillis: Int = 0): Modifier {
    val reduceMotion = LocalReduceMotion.current
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        progress.animateTo(1f, tween(if (reduceMotion) 260 else 420, easing = EaseOutStrong))
    }
    return graphicsLayer {
        alpha = progress.value
        translationY = if (reduceMotion) 0f else (1f - progress.value) * 14.dp.toPx()
    }
}
