package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.gdg.open.devfest.app.domain.model.Track
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts

/** Color del track como `Color` de Compose. */
val Track.composeColor: Color get() = Color(color)

/** Fondo suave del track (el color al 17 %, como en el prototipo). */
val Track.tint: Color get() = composeColor.copy(alpha = 0x2B / 255f)

/**
 * Etiqueta del track con su color (FR-011).
 *
 * @param label texto a mostrar; por defecto el nombre del track.
 * @param large variante del Detalle ("Track IA").
 */
@Composable
fun TrackChip(
    track: Track,
    modifier: Modifier = Modifier,
    label: String = track.name,
    large: Boolean = false,
) {
    Row(
        modifier = modifier
            .heightIn(min = if (large) 28.dp else 24.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(track.tint)
            .padding(horizontal = if (large) 12.dp else 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(if (large) 9.dp else 8.dp)
                .clip(CircleShape)
                .background(track.composeColor),
        )
        Text(
            text = label,
            fontFamily = LocalDevFestFonts.current.outfit,
            fontSize = if (large) 13.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = DevFestColors.Ink,
        )
    }
}
