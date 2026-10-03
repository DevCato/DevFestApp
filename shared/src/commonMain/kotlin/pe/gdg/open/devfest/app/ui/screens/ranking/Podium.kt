package pe.gdg.open.devfest.app.ui.screens.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.gdg.open.devfest.app.domain.model.RankingEntry
import pe.gdg.open.devfest.app.ui.components.Avatar
import pe.gdg.open.devfest.app.ui.components.riseIn
import pe.gdg.open.devfest.app.ui.components.solidShadow
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts

private data class PodiumStyle(val position: Int, val height: Dp, val color: Color, val avatar: Dp, val delayMillis: Int)

/** 2.º a la izquierda (celeste), 1.º al centro y más alto (amarillo), 3.º a la derecha (verde). */
private val podiumStyles = listOf(
    PodiumStyle(2, 92.dp, DevFestColors.SkyTint, 56.dp, 120),
    PodiumStyle(1, 124.dp, DevFestColors.Yellow, 68.dp, 40),
    PodiumStyle(3, 72.dp, DevFestColors.GreenTint, 56.dp, 200),
)

/** Podio de los puestos 1–3 (US7, escenario 1). */
@Composable
fun Podium(entries: List<RankingEntry>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        podiumStyles.forEach { style ->
            val entry = entries.firstOrNull { it.position == style.position }
            if (entry == null) {
                Spacer(Modifier.weight(1f))
            } else {
                PodiumPlace(entry, style, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PodiumPlace(entry: RankingEntry, style: PodiumStyle, modifier: Modifier = Modifier) {
    val description = entryDescription(entry)
    Column(
        modifier = modifier.riseIn(style.delayMillis).clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Avatar(
            name = entry.fullName,
            photoUrl = entry.photoUrl,
            size = style.avatar,
            background = if (entry.isMe) DevFestColors.SkyTint else style.color,
            borderWidth = if (entry.isMe) 3.5.dp else DevFestDimens.Border,
        )
        Text(
            text = displayName(entry),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DevFestColors.Ink,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        GemCount(entry.gems, fontSize = 13, iconSize = 14, delayMillis = style.delayMillis)
        val shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .solidShadow(DevFestColors.Ink, DevFestDimens.ShadowM, shape)
                .height(style.height)
                .clip(shape)
                .background(style.color)
                .border(DevFestDimens.Border, DevFestColors.Ink, shape)
                .padding(top = 10.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Text(
                text = entry.position.toString(),
                fontFamily = LocalDevFestFonts.current.mono,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = DevFestColors.Ink,
            )
        }
    }
}
