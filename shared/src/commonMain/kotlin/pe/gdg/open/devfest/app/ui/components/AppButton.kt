package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens

enum class AppButtonStyle {
    /** Relleno de tinta, sombra sólida de color. */
    Primary,

    /** Fondo blanco con borde de tinta, sin sombra. */
    Secondary,
}

/**
 * Botón del prototipo. Al presionarlo se "hunde" sobre su sombra en 120 ms; con "reducir
 * movimiento" no se desplaza.
 *
 * @param loadingText texto mientras [loading] (p. ej. "Conectando…"); el botón no responde.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingText: String? = null,
    shadowColor: Color = DevFestColors.Sky,
    containerColor: Color? = null,
    contentColor: Color? = null,
    height: Dp? = null,
    textStyle: TextStyle? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val clickable = enabled && !loading
    val isPrimary = style == AppButtonStyle.Primary

    val shadow = if (isPrimary) DevFestDimens.ShadowM else 0.dp
    val press by rememberPressProgress(interactionSource, clickable)
    val shape = RoundedCornerShape(percent = 50)
    val background = containerColor ?: if (isPrimary) DevFestColors.Ink else DevFestColors.Card
    val foreground = contentColor ?: if (isPrimary) DevFestColors.Card else DevFestColors.Ink

    Box(
        modifier = modifier
            .graphicsLayer {
                val offset = shadow.toPx() * press
                translationX = offset
                translationY = offset
                alpha = if (enabled) 1f else 0.55f
            }
            // La sombra reserva su espacio fijo; al presionar solo se acorta lo dibujado.
            .solidShadow(shadowColor, shadow * (1f - press), shape, reserve = shadow)
            .defaultMinSize(minHeight = height ?: if (isPrimary) 52.dp else 48.dp)
            .clip(shape)
            .background(background)
            .border(if (isPrimary) DevFestDimens.Border else DevFestDimens.BorderThin, DevFestColors.Ink, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = clickable,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            leadingIcon?.invoke()
            Text(
                text = if (loading && loadingText != null) loadingText else text,
                style = textStyle ?: MaterialTheme.typography.labelLarge.copy(
                    fontSize = if (isPrimary) 16.sp else 15.sp,
                    fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.SemiBold,
                ),
                color = foreground,
                textAlign = TextAlign.Center,
            )
        }
    }
}
