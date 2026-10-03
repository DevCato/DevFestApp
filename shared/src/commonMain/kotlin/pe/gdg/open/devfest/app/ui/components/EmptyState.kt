package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.sticker_asterisk
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens

/**
 * Estado vacío del prototipo: tarjeta punteada con un ícono en recuadro, título, texto y un
 * botón opcional (sin charlas, track vacío, sin conexión, Mi agenda vacía).
 */
@Composable
fun EmptyState(
    icon: DrawableResource,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    iconTint: Color = DevFestColors.Ink,
    iconBackground: Color = DevFestColors.SkyTint,
    actionLabel: String? = null,
    actionLoading: Boolean = false,
    actionLoadingLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DevFestDimens.RadiusL))
            .background(DevFestColors.Card)
            .dashedBorder(DevFestDimens.Border, DevFestColors.Ink, DevFestDimens.RadiusL)
            .padding(start = 26.dp, end = 26.dp, top = 40.dp, bottom = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.padding(bottom = 6.dp).riseIn(80).clearAndSetSemantics { }) {
            val shape = RoundedCornerShape(24.dp)
            Box(
                modifier = Modifier
                    .solidShadow(DevFestColors.Ink, 5.dp, shape)
                    .size(88.dp)
                    .clip(shape)
                    .background(iconBackground)
                    .border(DevFestDimens.Border, DevFestColors.Ink, shape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(icon), contentDescription = null, tint = iconTint, modifier = Modifier.size(44.dp))
            }
            Image(
                painter = painterResource(Res.drawable.sticker_asterisk),
                contentDescription = null,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 18.dp, y = (-14).dp).size(30.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 23.sp),
            color = DevFestColors.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 260.dp),
        )
        if (actionLabel != null && onAction != null) {
            AppButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.padding(top = 6.dp),
                loading = actionLoading,
                loadingText = actionLoadingLabel,
                shadowColor = DevFestColors.Sky,
                height = 48.dp,
            )
        }
    }
}
