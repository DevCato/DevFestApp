package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.conflict_badge
import pe.gdg.open.devfest.app.ui.theme.DevFestColors

/** Etiqueta amarilla "Cruce de horario" (FR-022). */
@Composable
fun ConflictBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .heightIn(min = 24.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(DevFestColors.Yellow)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(painterResource(AppIcons.Alert), contentDescription = null, tint = DevFestColors.Ink, modifier = Modifier.size(13.dp))
        Text(
            text = stringResource(Res.string.conflict_badge),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = DevFestColors.Ink,
        )
    }
}
