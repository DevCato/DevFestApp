package pe.gdg.open.devfest.app.ui.screens.gems

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pe.gdg.open.devfest.app.domain.model.EarnWay
import pe.gdg.open.devfest.app.domain.model.GemSourceType
import pe.gdg.open.devfest.app.domain.model.Prize
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.gems_balance
import pe.gdg.open.devfest.app.resources.gems_collected
import pe.gdg.open.devfest.app.resources.gems_how_to
import pe.gdg.open.devfest.app.resources.gems_points
import pe.gdg.open.devfest.app.resources.gems_prizes
import pe.gdg.open.devfest.app.resources.gems_prizes_text
import pe.gdg.open.devfest.app.resources.gems_ranking
import pe.gdg.open.devfest.app.resources.gems_ranking_position
import pe.gdg.open.devfest.app.resources.gems_scan
import pe.gdg.open.devfest.app.resources.gems_scan_subtitle
import pe.gdg.open.devfest.app.resources.gems_title
import pe.gdg.open.devfest.app.resources.sticker_asterisk
import pe.gdg.open.devfest.app.ui.components.AnimatedCount
import pe.gdg.open.devfest.app.ui.components.AppIcons
import pe.gdg.open.devfest.app.ui.components.NavCardButton
import pe.gdg.open.devfest.app.ui.components.ScreenTopBar
import pe.gdg.open.devfest.app.ui.components.SkeletonBlock
import pe.gdg.open.devfest.app.ui.components.dotBackground
import pe.gdg.open.devfest.app.ui.components.riseIn
import pe.gdg.open.devfest.app.ui.components.solidShadow
import pe.gdg.open.devfest.app.ui.modal.ModalRequestEffect
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts
import pe.gdg.open.devfest.app.ui.util.navigationBarsBottom

/** Pantalla 06 del prototipo (US6). */
@Composable
fun GemsScreen(
    onBack: () -> Unit,
    onScanClick: () -> Unit,
    onRankingClick: () -> Unit,
    viewModel: GemsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ModalRequestEffect(viewModel.modals)
    LaunchedEffect(Unit) { viewModel.onEnter() }

    Column(Modifier.fillMaxSize().dotBackground().windowInsetsPadding(WindowInsets.statusBars)) {
        ScreenTopBar(onBack = onBack)
        // El contenido pasa por detrás de la barra de navegación y deja su espacio al final.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 40.dp + navigationBarsBottom()),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Text(
                text = stringResource(Res.string.gems_title),
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.semantics { heading() },
            )
            val user = state.user
            if (user == null) {
                SkeletonBlock(Modifier.fillMaxWidth().height(120.dp), RoundedCornerShape(26.dp))
            } else {
                BalanceCard(user.gems)
            }
            NavCardButton(
                title = stringResource(Res.string.gems_scan),
                subtitle = stringResource(Res.string.gems_scan_subtitle),
                onClick = onScanClick,
                background = DevFestColors.Link,
                contentColor = DevFestColors.Card,
                subtitleColor = DevFestColors.Card,
                iconBackground = DevFestColors.Card,
                iconBorder = true,
                minHeight = 72.dp,
                icon = { Icon(painterResource(AppIcons.Qr), null, tint = DevFestColors.Ink, modifier = Modifier.size(22.dp)) },
            )
            NavCardButton(
                title = stringResource(Res.string.gems_ranking),
                subtitle = user?.let { stringResource(Res.string.gems_ranking_position, it.rank, it.totalParticipants) }.orEmpty(),
                onClick = onRankingClick,
                background = DevFestColors.Ink,
                contentColor = DevFestColors.Card,
                subtitleColor = DevFestColors.Card.copy(alpha = 0.8f),
                shadowColor = DevFestColors.Purple,
                iconBackground = DevFestColors.Purple,
                minHeight = 72.dp,
                icon = { Icon(painterResource(AppIcons.Trophy), null, tint = DevFestColors.Card, modifier = Modifier.size(22.dp)) },
            )
            val info = state.info
            when {
                info != null -> {
                    if (info.earnWays.isNotEmpty()) EarnWays(info.earnWays)
                    if (info.prizes.isNotEmpty()) Prizes(info.prizes)
                }
                state.loading -> SkeletonBlock(Modifier.fillMaxWidth().height(220.dp), RoundedCornerShape(22.dp))
            }
        }
    }
}

@Composable
private fun BalanceCard(gems: Int) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .solidShadow(DevFestColors.Ink, 6.dp, shape)
            .clip(shape)
            .background(DevFestColors.Yellow)
            .border(DevFestDimens.Border, DevFestColors.Ink, shape),
    ) {
        Image(
            painter = painterResource(Res.drawable.sticker_asterisk),
            contentDescription = null,
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-12).dp).size(54.dp),
        )
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            val tileShape = RoundedCornerShape(DevFestDimens.RadiusIcon)
            Box(
                modifier = Modifier
                    .riseIn(60)
                    .rotate(-6f)
                    .solidShadow(DevFestColors.Ink, DevFestDimens.ShadowS, tileShape)
                    .size(76.dp)
                    .clip(tileShape)
                    .background(DevFestColors.Card)
                    .border(DevFestDimens.Border, DevFestColors.Ink, tileShape)
                    .clearAndSetSemantics { },
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(AppIcons.GemFilled), contentDescription = null, modifier = Modifier.size(50.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(Res.string.gems_balance),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.08.em,
                    color = DevFestColors.Ink,
                )
                AnimatedCount(
                    value = gems,
                    style = TextStyle(
                        fontFamily = LocalDevFestFonts.current.mono,
                        fontSize = 48.sp,
                        lineHeight = 50.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.04).em,
                        color = DevFestColors.Ink,
                    ),
                )
                Text(
                    text = stringResource(Res.string.gems_collected),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DevFestColors.Ink,
                )
            }
        }
    }
}

@Composable
private fun EarnWays(earnWays: List<EarnWay>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(stringResource(Res.string.gems_how_to))
        earnWays.forEach { way ->
            val (color, icon) = when (way.type) {
                GemSourceType.TALK -> DevFestColors.Blue to AppIcons.Calendar
                GemSourceType.STAND -> DevFestColors.Green to AppIcons.Qr
                GemSourceType.CHALLENGE -> DevFestColors.Purple to AppIcons.Flag
            }
            EarnWayCard(way, color, icon)
        }
    }
}

@Composable
private fun EarnWayCard(way: EarnWay, color: Color, icon: DrawableResource) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .solidShadow(color, DevFestDimens.ShadowM, shape)
            .clip(shape)
            .background(DevFestColors.Card)
            .border(DevFestDimens.Border, DevFestColors.Ink, shape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(46.dp).clip(RoundedCornerShape(DevFestDimens.RadiusS)).background(color.copy(alpha = 0x2B / 255f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = DevFestColors.Ink, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = way.title, fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold, color = DevFestColors.Ink)
            Text(text = way.description, fontSize = 14.sp, color = DevFestColors.Muted)
        }
        GemsPill(way.gems)
    }
}

/** Cantidad de gemas que entrega el backend para esa forma de ganar (informativa). */
@Composable
private fun GemsPill(gems: Int) {
    val shape = RoundedCornerShape(percent = 50)
    Row(
        modifier = Modifier
            .heightIn(min = 34.dp)
            .clip(shape)
            .background(DevFestColors.Yellow)
            .border(DevFestDimens.BorderThin, DevFestColors.Ink, shape)
            .padding(start = 9.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Image(painterResource(AppIcons.GemFilled), contentDescription = null, modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(Res.string.gems_points, gems),
            fontFamily = LocalDevFestFonts.current.mono,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = DevFestColors.Ink,
        )
    }
}

private val prizeTints = listOf(DevFestColors.SkyTint, DevFestColors.YellowTint, DevFestColors.GreenTint, DevFestColors.PurpleTint)

private fun prizeIcon(prize: Prize): DrawableResource = when (prize.id) {
    "stickers" -> AppIcons.PrizeStickers
    "polos" -> AppIcons.PrizeShirt
    "certificaciones" -> AppIcons.PrizeMedal
    "cursos" -> AppIcons.PrizeCourse
    else -> AppIcons.Trophy
}

/** Premios de los primeros puestos: sin precios en gemas ni botón de canje (FR-044). */
@Composable
private fun Prizes(prizes: List<Prize>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(stringResource(Res.string.gems_prizes))
        Text(
            text = stringResource(Res.string.gems_prizes_text),
            style = MaterialTheme.typography.bodyMedium,
            color = DevFestColors.Muted,
        )
        prizes.withIndex().chunked(2).forEach { row ->
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (index, prize) ->
                    PrizeCard(prize, prizeTints[index % prizeTints.size], Modifier.weight(1f).fillMaxHeight())
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PrizeCard(prize: Prize, tint: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(DevFestColors.Card)
            .border(DevFestDimens.Border, DevFestColors.Ink, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val tileShape = RoundedCornerShape(16.dp)
        Box(
            Modifier.size(48.dp).clip(tileShape).background(tint).border(DevFestDimens.BorderThin, DevFestColors.Ink, tileShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(prizeIcon(prize)), contentDescription = null, tint = DevFestColors.Ink, modifier = Modifier.size(24.dp))
        }
        Text(text = prize.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DevFestColors.Ink)
        prize.description?.let { Text(text = it, fontSize = 14.sp, color = DevFestColors.Muted) }
        prize.forPositions?.let { positions ->
            Row(
                modifier = Modifier
                    .heightIn(min = 28.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(DevFestColors.Soft)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(painterResource(AppIcons.Trophy), contentDescription = null, tint = DevFestColors.Ink, modifier = Modifier.size(14.dp))
                Text(text = positions, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DevFestColors.Ink)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
}
