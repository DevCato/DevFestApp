package pe.gdg.open.devfest.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.EaseInOutStrong
import pe.gdg.open.devfest.app.ui.theme.LocalReduceMotion

/**
 * Bloque gris con la forma del contenido que está cargando (FR-018): radios del diseño, sin
 * bordes ni sombras, con un brillo suave que se apaga con "reducir movimiento".
 */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(DevFestDimens.RadiusS),
) {
    val reduceMotion = LocalReduceMotion.current
    val shimmer = if (reduceMotion) {
        null
    } else {
        rememberInfiniteTransition(label = "skeleton").animateFloat(
            initialValue = 1f,
            targetValue = 0.55f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = EaseInOutStrong),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmer",
        )
    }
    Box(
        modifier = modifier
            .clearAndSetSemantics { }
            .graphicsLayer { alpha = shimmer?.value ?: 1f }
            .clip(shape)
            .background(DevFestColors.Skeleton),
    )
}
