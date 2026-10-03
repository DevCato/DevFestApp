package pe.gdg.open.devfest.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.gems_count_cd
import pe.gdg.open.devfest.app.ui.theme.EaseOutExpo
import pe.gdg.open.devfest.app.ui.theme.LocalReduceMotion
import kotlin.math.roundToInt

/**
 * Contador de gemas: cuenta de 0 al valor en ~900 ms (easeOutExpo) tras la entrada de la
 * pantalla; si el valor cambia después, cuenta desde el anterior. Con "reducir movimiento"
 * muestra el valor directamente (FR-060). Los lectores de pantalla leen el valor final.
 */
@Composable
fun AnimatedCount(
    value: Int,
    style: TextStyle,
    modifier: Modifier = Modifier,
    delayMillis: Int = 200,
    durationMillis: Int = 900,
) {
    val reduceMotion = LocalReduceMotion.current
    val animated = remember { Animatable(if (reduceMotion) value.toFloat() else 0f) }
    val firstRun = remember { FirstRun() }
    LaunchedEffect(value, reduceMotion) {
        if (reduceMotion) {
            animated.snapTo(value.toFloat())
        } else {
            if (firstRun.value) delay(delayMillis.toLong())
            animated.animateTo(value.toFloat(), tween(durationMillis, easing = EaseOutExpo))
        }
        firstRun.value = false
    }
    val description = stringResource(Res.string.gems_count_cd, value)
    Text(
        text = animated.value.roundToInt().toString(),
        style = style,
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
    )
}

private class FirstRun(var value: Boolean = true)
