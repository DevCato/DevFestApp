package pe.gdg.open.devfest.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.domain.model.Talk
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.cd_bookmark_add
import pe.gdg.open.devfest.app.resources.cd_bookmark_remove
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.EaseOutStrong
import pe.gdg.open.devfest.app.ui.theme.LocalReduceMotion
import pe.gdg.open.devfest.app.ui.util.rememberSingleTap

/**
 * Tarjeta de charla (Agenda y Mi agenda): etiquetas, título, datos y marcador. La sombra usa
 * siempre el color del track (FR-011). Al presionarla escala a 0.98.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TalkCard(
    talk: Talk,
    info: String,
    saved: Boolean,
    onClick: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier,
    extraBadges: @Composable FlowRowScope.() -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.98f else 1f,
        animationSpec = tween(120, easing = EaseOutStrong),
    )
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .solidShadow(talk.track.composeColor, 5.dp, shape)
            .clip(shape)
            .background(DevFestColors.Card)
            .border(DevFestDimens.Border, DevFestColors.Ink, shape),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
                .padding(start = 18.dp, top = 18.dp, bottom = 18.dp, end = 56.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (talk.isLive) LiveBadge()
                TrackChip(talk.track)
                extraBadges()
            }
            Text(
                text = talk.title,
                fontSize = 18.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Bold,
                color = DevFestColors.Ink,
            )
            Text(
                text = info,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = DevFestColors.Muted,
            )
        }
        BookmarkToggle(
            saved = saved,
            color = talk.track.composeColor,
            onToggle = onToggleSaved,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
        )
    }
}

/**
 * Marcador de 44 dp: al guardar se rellena con el color del track y "late" una vez (180 ms),
 * salvo con "reducir movimiento".
 */
@Composable
fun BookmarkToggle(
    saved: Boolean,
    color: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 22.dp,
) {
    val reduceMotion = LocalReduceMotion.current
    val pop = remember { Animatable(1f) }
    val last = remember { LastValue(saved) }
    LaunchedEffect(saved) {
        val justSaved = saved && !last.value
        last.value = saved
        if (justSaved && !reduceMotion) {
            pop.snapTo(0.8f)
            pop.animateTo(1f, tween(180, easing = EaseOutStrong))
        }
    }
    val description = stringResource(if (saved) Res.string.cd_bookmark_remove else Res.string.cd_bookmark_add)
    val toggle = rememberSingleTap(onToggle)
    Box(
        modifier = modifier
            .size(DevFestDimens.MinTouch)
            .clip(RoundedCornerShape(12.dp))
            .toggleable(value = saved, role = Role.Checkbox, onValueChange = { toggle() })
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        BookmarkIcon(
            saved = saved,
            color = color,
            modifier = Modifier.size(iconSize).graphicsLayer {
                scaleX = pop.value
                scaleY = pop.value
            },
        )
    }
}

/** Marcador vacío, o relleno del [color] con el contorno de tinta. */
@Composable
fun BookmarkIcon(saved: Boolean, color: Color, modifier: Modifier = Modifier, outline: Color = DevFestColors.Ink) {
    Box(modifier) {
        if (saved) {
            Icon(painterResource(AppIcons.BookmarkFill), contentDescription = null, tint = color, modifier = Modifier.matchParentSize())
        }
        Icon(painterResource(AppIcons.Bookmark), contentDescription = null, tint = outline, modifier = Modifier.matchParentSize())
    }
}

/** Último valor visto, sin provocar recomposición. */
private class LastValue(var value: Boolean)
