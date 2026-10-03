package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts

/** Opacidad de las sesiones pasadas: atenuadas pero legibles. */
const val PAST_ALPHA = 0.62f

/**
 * Fila de la línea de tiempo: columna de hora de 50 dp y el contenido. Sin [time] la columna
 * queda vacía (la hora se muestra solo en la primera sesión de cada grupo).
 */
@Composable
fun TimelineRow(
    time: String?,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    timeColor: Color = DevFestColors.Ink,
    timeTopPadding: Dp = 20.dp,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit,
) {
    // La columna de hora crece con la letra del sistema para que "08:30" no se corte (FR-062).
    val timeWidth = 50.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (dimmed) PAST_ALPHA else 1f },
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = verticalAlignment,
    ) {
        Box(Modifier.width(timeWidth).padding(top = timeTopPadding)) {
            if (time != null) {
                Text(
                    text = time,
                    fontFamily = LocalDevFestFonts.current.mono,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = timeColor,
                    maxLines = 1,
                )
            }
        }
        content()
    }
}
