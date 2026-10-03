package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.gdg.open.devfest.app.domain.logic.formatHour
import pe.gdg.open.devfest.app.domain.model.Break
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts

/** Pausa (registro, almuerzo, cierre): fila punteada, no tocable, con su duración. */
@Composable
fun BreakRow(
    pause: Break,
    showTime: Boolean,
    isPast: Boolean,
    modifier: Modifier = Modifier,
) {
    TimelineRow(
        time = if (showTime) formatHour(pause.startsAt) else null,
        modifier = modifier,
        dimmed = isPast,
        timeColor = DevFestColors.Muted,
        timeTopPadding = 0.dp,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .dashedBorder(DevFestDimens.BorderThin, DevFestColors.Muted, 16.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = pause.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = DevFestColors.Muted,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = durationLabel(pause),
                fontFamily = LocalDevFestFonts.current.mono,
                fontSize = 12.sp,
                color = DevFestColors.Muted,
            )
        }
    }
}
