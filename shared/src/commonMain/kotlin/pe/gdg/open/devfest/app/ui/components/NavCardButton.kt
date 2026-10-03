package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens

/**
 * Botón-tarjeta con ícono en recuadro, título, subtítulo y flecha (Mis gemas, Perfil).
 *
 * @param icon el ícono dentro del recuadro de 44 dp.
 */
@Composable
fun NavCardButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = DevFestColors.Card,
    contentColor: Color = DevFestColors.Ink,
    subtitleColor: Color = DevFestColors.Muted,
    shadowColor: Color = DevFestColors.Ink,
    iconBackground: Color = DevFestColors.Soft,
    iconBorder: Boolean = false,
    minHeight: Dp = 68.dp,
) {
    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        modifier = modifier.fillMaxWidth(),
        surfaceModifier = Modifier.heightIn(min = minHeight),
        background = background,
        shadowColor = shadowColor,
        shadowOffset = 5.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val tileShape = RoundedCornerShape(DevFestDimens.RadiusS)
            val borderModifier = if (iconBorder) Modifier.border(DevFestDimens.BorderThin, DevFestColors.Ink, tileShape) else Modifier
            Box(
                modifier = Modifier.size(44.dp).clip(tileShape).background(iconBackground).then(borderModifier),
                contentAlignment = Alignment.Center,
            ) { icon() }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = contentColor)
                Text(text = subtitle, fontSize = 14.sp, color = subtitleColor)
            }
            Icon(painterResource(AppIcons.Chevron), contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
        }
    }
}
