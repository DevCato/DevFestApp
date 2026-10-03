package pe.gdg.open.devfest.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.live_now
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.EaseInOutStrong
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts
import pe.gdg.open.devfest.app.ui.theme.LocalReduceMotion

/**
 * Etiqueta roja "LIVE NOW" con un punto que late en bucle de 1.4 s (quieto con "reducir
 * movimiento"). Se muestra solo por el flag del backend (FR-013).
 */
@Composable
fun LiveBadge(
    modifier: Modifier = Modifier,
    large: Boolean = false,
    bordered: Boolean = false,
) {
    val reduceMotion = LocalReduceMotion.current
    val pulse = if (reduceMotion) {
        null
    } else {
        rememberInfiniteTransition(label = "live").animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(700, easing = EaseInOutStrong), RepeatMode.Reverse),
            label = "pulse",
        )
    }
    val shape = RoundedCornerShape(percent = 50)
    val borderModifier = if (bordered) Modifier.border(DevFestDimens.BorderThin, DevFestColors.Ink, shape) else Modifier
    Row(
        modifier = modifier
            .heightIn(min = if (large) 28.dp else 24.dp)
            .clip(shape)
            .background(DevFestColors.LiveRed)
            .then(borderModifier)
            .padding(horizontal = if (large) 12.dp else 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(if (large) 8.dp else 7.dp)
                .graphicsLayer {
                    val value = pulse?.value ?: 1f
                    alpha = 0.35f + 0.65f * value
                    val scale = 0.7f + 0.3f * value
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(DevFestColors.Card),
        )
        Text(
            text = stringResource(Res.string.live_now),
            fontFamily = LocalDevFestFonts.current.outfit,
            fontSize = if (large) 12.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.07.em,
            color = DevFestColors.Card,
        )
    }
}
