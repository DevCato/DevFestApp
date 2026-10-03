package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.domain.model.User
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.cd_gems
import pe.gdg.open.devfest.app.resources.cd_gems_loading
import pe.gdg.open.devfest.app.resources.cd_profile
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts

/**
 * Cabecera de Agenda y Mi agenda: logo, contador de gemas (abre Mis gemas) y foto (abre Perfil).
 * Mientras no hay usuario cargado, el contador muestra "–".
 */
@Composable
fun AppHeader(
    user: User?,
    onGemsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            DevFestLogo(Modifier.padding(top = 5.dp))
            BrandTags(modifier = Modifier.padding(top = 5.dp), brandTagsSize = BrandTagsSize.Small)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            GemCounterButton(gems = user?.gems, onClick = onGemsClick)
            PressableSurface(
                onClick = onProfileClick,
                shape = CircleShape,
                surfaceModifier = Modifier.size(48.dp),
                background = DevFestColors.SkyTint,
                contentDescription = stringResource(Res.string.cd_profile),
            ) {
                Avatar(
                    name = user?.fullName.orEmpty(),
                    photoUrl = user?.photoUrl,
                    size = 48.dp,
                    borderWidth = 0.dp,
                )
            }
        }
    }
}

@Composable
private fun GemCounterButton(gems: Int?, onClick: () -> Unit) {
    val description = if (gems != null) {
        stringResource(Res.string.cd_gems, gems)
    } else {
        stringResource(Res.string.cd_gems_loading)
    }
    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        surfaceModifier = Modifier.heightIn(min = DevFestDimens.MinTouch),
        shadowOffset = 2.dp,
        borderWidth = DevFestDimens.BorderThin,
        contentDescription = description,
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Image(painterResource(AppIcons.GemFilled), contentDescription = null, modifier = Modifier.size(22.dp))
            Text(
                text = gems?.toString() ?: "–",
                fontFamily = LocalDevFestFonts.current.mono,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = DevFestColors.Ink,
            )
        }
    }
}
