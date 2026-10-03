package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.domain.logic.formatHour
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.cd_now_line
import pe.gdg.open.devfest.app.resources.now_label
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import kotlin.time.Instant

/** Línea roja "AHORA" con la hora del teléfono en Lima (FR-014). */
@Composable
fun NowLineRow(now: Instant, modifier: Modifier = Modifier) {
    val time = formatHour(now)
    val description = stringResource(Res.string.cd_now_line, time)
    TimelineRow(
        time = time,
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        timeColor = DevFestColors.Red,
        timeTopPadding = 0.dp,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .offset(x = (-6).dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(DevFestColors.LiveRed)
                    .border(2.dp, DevFestColors.Background, CircleShape),
            )
            Box(
                Modifier
                    .weight(1f)
                    .offset(x = (-6).dp)
                    .height(2.5.dp)
                    .background(DevFestColors.LiveRed),
            )
            Text(
                text = stringResource(Res.string.now_label),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.07.em,
                color = DevFestColors.Red,
                modifier = Modifier.padding(start = 2.dp),
            )
        }
    }
}
