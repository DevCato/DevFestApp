package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens

/**
 * Superficie tocable del prototipo: borde de tinta y sombra sólida; al presionarla se "hunde"
 * sobre su sombra en 120 ms (sin desplazamiento con "reducir movimiento").
 */
@Composable
fun PressableSurface(
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    background: Color = DevFestColors.Card,
    shadowColor: Color = DevFestColors.Ink,
    shadowOffset: Dp = DevFestDimens.ShadowS,
    borderWidth: Dp = DevFestDimens.Border,
    borderColor: Color = DevFestColors.Ink,
    enabled: Boolean = true,
    role: Role = Role.Button,
    contentDescription: String? = null,
    contentAlignment: Alignment = Alignment.Center,
    /** Tamaño visible de la superficie (sin la sombra): `size`, `heightIn`… */
    surfaceModifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val press by rememberPressProgress(interactionSource, enabled)
    val descriptionModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .graphicsLayer {
                val offset = shadowOffset.toPx() * press
                translationX = offset
                translationY = offset
                alpha = if (enabled) 1f else 0.55f
            }
            // La sombra reserva su espacio fijo; al presionar solo se acorta lo dibujado.
            .solidShadow(shadowColor, shadowOffset * (1f - press), shape, reserve = shadowOffset)
            .then(surfaceModifier)
            .clip(shape)
            .background(background)
            .border(borderWidth, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = role,
                onClick = onClick,
            )
            .then(descriptionModifier),
        contentAlignment = contentAlignment,
        content = content,
    )
}

/** Botón circular de 46 dp con un ícono (volver, marcador, refresh). */
@Composable
fun CircleIconButton(
    icon: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconSize: Dp = 22.dp,
    iconModifier: Modifier = Modifier,
    background: Color = DevFestColors.Card,
    content: (@Composable BoxScope.() -> Unit)? = null,
) {
    PressableSurface(
        onClick = onClick,
        shape = CircleShape,
        modifier = modifier,
        surfaceModifier = Modifier.size(46.dp),
        background = background,
        enabled = enabled,
        contentDescription = contentDescription,
    ) {
        if (content != null) {
            content()
        } else {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = DevFestColors.Ink,
                modifier = iconModifier.size(iconSize),
            )
        }
    }
}
