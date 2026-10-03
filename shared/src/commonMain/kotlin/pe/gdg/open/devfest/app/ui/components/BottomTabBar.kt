package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.tab_agenda
import pe.gdg.open.devfest.app.resources.tab_my_agenda
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens

enum class MainTab { AGENDA, MY_AGENDA }

/** Alto que ocupa la barra inferior: las listas dejan este espacio al final. */
val BottomBarContentPadding = 130.dp

/**
 * Navegación principal (FR-055): "Agenda" y "Mi agenda" con el contador de charlas guardadas.
 * Flota sobre el contenido con un degradado del fondo detrás.
 */
@Composable
fun BottomTabBar(
    selected: MainTab,
    savedCount: Int,
    onSelect: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(0f to Color.Transparent, 0.42f to DevFestColors.Background))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 6.dp),
    ) {
        val shape = RoundedCornerShape(percent = 50)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Alto intrínseco: las pestañas ocupan todo el alto de la barra, no de la pantalla.
                .solidShadow(DevFestColors.Ink, DevFestDimens.ShadowM, shape)
                .height(IntrinsicSize.Min)
                .heightIn(min = 66.dp)
                .clip(shape)
                .background(DevFestColors.Card)
                .border(DevFestDimens.Border, DevFestColors.Ink, shape)
                .padding(6.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Tab(
                label = stringResource(Res.string.tab_agenda),
                icon = AppIcons.Calendar,
                selected = selected == MainTab.AGENDA,
                onClick = { onSelect(MainTab.AGENDA) },
            )
            Tab(
                label = stringResource(Res.string.tab_my_agenda),
                icon = AppIcons.Bookmark,
                selected = selected == MainTab.MY_AGENDA,
                onClick = { onSelect(MainTab.MY_AGENDA) },
                badge = savedCount,
            )
        }
    }
}

@Composable
private fun RowScope.Tab(
    label: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
    badge: Int? = null,
) {
    val foreground = if (selected) DevFestColors.Card else DevFestColors.Ink
    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) DevFestColors.Ink else Color.Transparent)
            // Tocar la pestaña en la que ya se está no hace nada (ni vuelve a navegar).
            .selectable(selected = selected, role = Role.Tab, onClick = { if (!selected) onClick() }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = foreground, modifier = Modifier.size(19.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = foreground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (badge != null) {
            Box(
                modifier = Modifier
                    .widthIn(min = 22.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(DevFestColors.Link)
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = badge.toString(), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = DevFestColors.Card)
            }
        }
    }
}
