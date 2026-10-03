package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.domain.logic.AgendaFilter
import pe.gdg.open.devfest.app.domain.model.DefaultTracks
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.filter_all
import pe.gdg.open.devfest.app.resources.filter_cloud
import pe.gdg.open.devfest.app.resources.filter_ia
import pe.gdg.open.devfest.app.resources.filter_mobile
import pe.gdg.open.devfest.app.resources.filter_web
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens

/**
 * Filtros de la Agenda. Si no caben, la fila se desplaza en horizontal y el borde derecho se
 * desvanece para indicar que hay más (US2, escenario 8).
 */
@Composable
fun FilterRow(
    selected: AgendaFilter,
    onSelect: (AgendaFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.horizontalGradient(0.82f to Color.Black, 1f to Color.Transparent),
                    blendMode = BlendMode.DstIn,
                )
            },
        contentPadding = PaddingValues(start = 24.dp, end = 32.dp, top = 2.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(AgendaFilter.entries.toList(), key = { it.name }) { filter ->
            // Tocar el filtro que ya está activo no hace nada.
            FilterChip(filter = filter, active = filter == selected, onClick = { if (filter != selected) onSelect(filter) })
        }
    }
}

@Composable
private fun FilterChip(filter: AgendaFilter, active: Boolean, onClick: () -> Unit) {
    val label = stringResource(
        when (filter) {
            AgendaFilter.ALL -> Res.string.filter_all
            AgendaFilter.IA -> Res.string.filter_ia
            AgendaFilter.WEB -> Res.string.filter_web
            AgendaFilter.MOBILE -> Res.string.filter_mobile
            AgendaFilter.CLOUD -> Res.string.filter_cloud
        },
    )
    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        modifier = Modifier.semantics { selected = active },
        surfaceModifier = Modifier.heightIn(min = DevFestDimens.MinTouch),
        background = if (active) DevFestColors.Ink else DevFestColors.Card,
        shadowOffset = 2.dp,
        borderWidth = DevFestDimens.BorderThin,
        role = Role.Tab,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            filter.trackId?.let { trackId ->
                Box(
                    Modifier
                        .padding(end = 8.dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(DefaultTracks.of(trackId).composeColor)
                        .border(1.5.dp, DevFestColors.Ink, CircleShape),
                )
            }
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (active) DevFestColors.Card else DevFestColors.Ink,
            )
        }
    }
}
