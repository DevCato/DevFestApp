package pe.gdg.open.devfest.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.sticker_arc
import pe.gdg.open.devfest.app.resources.sticker_asterisk
import pe.gdg.open.devfest.app.resources.sticker_cross
import pe.gdg.open.devfest.app.resources.sticker_plus
import pe.gdg.open.devfest.app.resources.sticker_ring
import pe.gdg.open.devfest.app.resources.sticker_slashes
import pe.gdg.open.devfest.app.ui.theme.EaseFloat
import pe.gdg.open.devfest.app.ui.theme.EaseOutStrong
import pe.gdg.open.devfest.app.ui.theme.LocalReduceMotion

/** Un sticker del prototipo con su tamaño y su forma de flotar. */
data class StickerSpec(
    val drawable: DrawableResource,
    val size: DpSize,
    val floatX: Dp,
    val floatY: Dp,
    val floatRotation: Float,
    val durationMillis: Int,
    val baseRotation: Float = 0f,
)

/** Salida más corta que la entrada: se va antes de que lo tape lo que tiene encima. */
private const val EXIT_MILLIS = 220

/** Los seis stickers del Login, con los ritmos del prototipo. */
object Stickers {
    val Slashes = StickerSpec(Res.drawable.sticker_slashes, DpSize(64.dp, 50.dp), 8.dp, (-22).dp, 12f, 3600)
    val Ring = StickerSpec(Res.drawable.sticker_ring, DpSize(120.dp, 120.dp), (-14).dp, 22.dp, -24f, 4800)
    val Asterisk = StickerSpec(Res.drawable.sticker_asterisk, DpSize(34.dp, 34.dp), 6.dp, (-16).dp, 90f, 4200)
    val Plus = StickerSpec(Res.drawable.sticker_plus, DpSize(44.dp, 44.dp), 12.dp, (-26).dp, 30f, 3300, baseRotation = 18f)
    val Arc = StickerSpec(Res.drawable.sticker_arc, DpSize(150.dp, 150.dp), 22.dp, (-16).dp, 14f, 5400)
    val Cross = StickerSpec(Res.drawable.sticker_cross, DpSize(50.dp, 50.dp), (-12).dp, (-22).dp, -32f, 3900, baseRotation = -12f)
}

/**
 * Sticker decorativo: entra con escala 0.7 → 1 (520 ms) y luego flota en bucle, ida y vuelta.
 * Con "reducir movimiento" solo aparece con un fundido y queda quieto (FR-060).
 * Con [visible] en false sale por el mismo camino a la inversa (más rápido), y al volver a true
 * repite la entrada. Es decorativo: los lectores de pantalla lo ignoran.
 */
@Composable
fun FloatingSticker(
    spec: StickerSpec,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    visible: Boolean = true,
) {
    val reduceMotion = LocalReduceMotion.current
    val appear = remember { Animatable(0f) }
    LaunchedEffect(reduceMotion, visible) {
        if (visible) {
            delay(delayMillis.toLong())
            appear.animateTo(1f, tween(if (reduceMotion) 260 else 520, easing = EaseOutStrong))
        } else {
            // Parte del valor actual: si se interrumpe la entrada, sale desde donde iba.
            appear.animateTo(0f, tween(EXIT_MILLIS, easing = EaseOutStrong))
        }
    }

    val float = if (reduceMotion) {
        null
    } else {
        rememberInfiniteTransition(label = "sticker").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(spec.durationMillis, easing = EaseFloat),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(delayMillis + 520),
            ),
            label = "float",
        )
    }

    Image(
        painter = painterResource(spec.drawable),
        contentDescription = null,
        modifier = modifier
            .clearAndSetSemantics { }
            .size(spec.size)
            .graphicsLayer {
                val progress = float?.value ?: 0f
                val scale = if (reduceMotion) 1f else 0.7f + 0.3f * appear.value
                alpha = appear.value
                scaleX = scale
                scaleY = scale
                translationX = spec.floatX.toPx() * progress
                translationY = spec.floatY.toPx() * progress
                rotationZ = spec.baseRotation + spec.floatRotation * progress
            },
    )
}
