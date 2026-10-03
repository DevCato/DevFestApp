package pe.gdg.open.devfest.app.ui.screens.myagenda

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.domain.logic.FeaturedKind
import pe.gdg.open.devfest.app.domain.logic.FeaturedTalk
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.my_agenda_in_progress
import pe.gdg.open.devfest.app.resources.my_agenda_next
import pe.gdg.open.devfest.app.ui.components.LiveBadge
import pe.gdg.open.devfest.app.ui.components.PressableSurface
import pe.gdg.open.devfest.app.ui.components.composeColor
import pe.gdg.open.devfest.app.ui.components.timeRange
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts

/** Tarjeta destacada de Mi agenda (FR-023), del color del track de la charla. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeaturedTalkCard(
    featured: FeaturedTalk,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val talk = featured.talk
    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(26.dp),
        modifier = modifier.fillMaxWidth(),
        background = talk.track.composeColor,
        shadowOffset = 6.dp,
        contentAlignment = Alignment.TopStart,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        if (featured.kind == FeaturedKind.IN_PROGRESS) Res.string.my_agenda_in_progress else Res.string.my_agenda_next,
                    ),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.06.em,
                    color = DevFestColors.Ink,
                    modifier = Modifier.weight(1f),
                )
                if (featured.kind == FeaturedKind.IN_PROGRESS) LiveBadge(bordered = true)
            }
            Text(
                text = talk.title,
                fontSize = 23.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.017).em,
                color = DevFestColors.Ink,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoPill(timeRange(talk), mono = true)
                if (talk.room.isNotBlank()) InfoPill(talk.room, mono = false)
            }
        }
    }
}

@Composable
private fun InfoPill(text: String, mono: Boolean) {
    val shape = RoundedCornerShape(percent = 50)
    Box(
        modifier = Modifier
            .heightIn(min = 30.dp)
            .clip(shape)
            .background(DevFestColors.Card)
            .border(DevFestDimens.BorderThin, DevFestColors.Ink, shape)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontFamily = if (mono) LocalDevFestFonts.current.mono else LocalDevFestFonts.current.outfit,
            fontSize = 13.sp,
            fontWeight = if (mono) FontWeight.SemiBold else FontWeight.Bold,
            color = DevFestColors.Ink,
        )
    }
}
