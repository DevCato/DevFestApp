package pe.gdg.open.devfest.app.ui.screens.ranking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.domain.model.RankingEntry
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.cd_ranking_entry
import pe.gdg.open.devfest.app.resources.ranking_you
import pe.gdg.open.devfest.app.ui.components.AnimatedCount
import pe.gdg.open.devfest.app.ui.components.AppIcons
import pe.gdg.open.devfest.app.ui.components.Avatar
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts

private val avatarTints = listOf(
    DevFestColors.SkyTint,
    DevFestColors.YellowTint,
    DevFestColors.GreenTint,
    DevFestColors.PurpleTint,
    DevFestColors.RedTint,
)

/** Fondo del avatar según la posición, como en el prototipo. */
fun avatarTint(position: Int): Color = avatarTints[(position - 1).mod(avatarTints.size)]

/** Nombre a mostrar: el del usuario actual es "Tú". */
@Composable
fun displayName(entry: RankingEntry): String =
    if (entry.isMe) stringResource(Res.string.ranking_you) else entry.fullName

/** Descripción para lectores de pantalla: "Puesto 4, Participante D, 365 gemas". */
@Composable
fun entryDescription(entry: RankingEntry): String =
    stringResource(Res.string.cd_ranking_entry, entry.position, displayName(entry), entry.gems)

/** Fila del ranking (puestos 4–10 y la propia). El usuario actual va resaltado. */
@Composable
fun RankingRow(entry: RankingEntry, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(18.dp)
    val description = entryDescription(entry)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 62.dp)
            .clip(shape)
            .background(if (entry.isMe) DevFestColors.SkyTint else DevFestColors.Card)
            .border(DevFestDimens.BorderThin, DevFestColors.Ink, shape)
            .clearAndSetSemantics { contentDescription = description }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = entry.position.toString(),
            fontFamily = LocalDevFestFonts.current.mono,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = DevFestColors.Ink,
            modifier = Modifier.width(30.dp),
        )
        Avatar(
            name = entry.fullName,
            photoUrl = entry.photoUrl,
            size = 38.dp,
            background = avatarTint(entry.position),
        )
        Text(
            text = displayName(entry),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = DevFestColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        GemCount(entry.gems, fontSize = 15, iconSize = 16, delayMillis = 260, durationMillis = 700)
    }
}

/** Fila "• • •" entre el puesto 10 y el del usuario (decorativa). */
@Composable
fun RankingGapRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().height(22.dp).clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { Box(Modifier.size(6.dp).clip(CircleShape).background(DevFestColors.Muted)) }
    }
}

/** Gema + contador animado. */
@Composable
fun GemCount(
    gems: Int,
    fontSize: Int,
    iconSize: Int,
    modifier: Modifier = Modifier,
    delayMillis: Int = 200,
    durationMillis: Int = 900,
    color: Color = DevFestColors.Ink,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Image(painterResource(AppIcons.GemFilled), contentDescription = null, modifier = Modifier.size(iconSize.dp))
        AnimatedCount(
            value = gems,
            delayMillis = delayMillis,
            durationMillis = durationMillis,
            style = TextStyle(
                fontFamily = LocalDevFestFonts.current.mono,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
            ),
        )
    }
}
