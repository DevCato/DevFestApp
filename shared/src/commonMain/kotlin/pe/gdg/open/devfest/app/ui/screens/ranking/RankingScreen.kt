package pe.gdg.open.devfest.app.ui.screens.ranking

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pe.gdg.open.devfest.app.domain.logic.RankingListItem
import pe.gdg.open.devfest.app.domain.logic.RankingView
import pe.gdg.open.devfest.app.domain.logic.formatHour
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.cd_refresh_ranking
import pe.gdg.open.devfest.app.resources.ranking_position_of
import pe.gdg.open.devfest.app.resources.ranking_subtitle
import pe.gdg.open.devfest.app.resources.ranking_title
import pe.gdg.open.devfest.app.resources.ranking_updated_at
import pe.gdg.open.devfest.app.resources.ranking_updated_now
import pe.gdg.open.devfest.app.resources.ranking_updating
import pe.gdg.open.devfest.app.resources.ranking_your_position
import pe.gdg.open.devfest.app.ui.components.AppIcons
import pe.gdg.open.devfest.app.ui.components.Avatar
import pe.gdg.open.devfest.app.ui.components.CircleIconButton
import pe.gdg.open.devfest.app.ui.components.ScreenTopBar
import pe.gdg.open.devfest.app.ui.components.SkeletonBlock
import pe.gdg.open.devfest.app.ui.components.dotBackground
import pe.gdg.open.devfest.app.ui.components.solidShadow
import pe.gdg.open.devfest.app.ui.modal.ModalRequestEffect
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts
import pe.gdg.open.devfest.app.ui.theme.LocalReduceMotion
import pe.gdg.open.devfest.app.ui.util.navigationBarsBottom

/** Pantalla 07 del prototipo (US7). */
@Composable
fun RankingScreen(
    onBack: () -> Unit,
    viewModel: RankingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ModalRequestEffect(viewModel.modals)
    LaunchedEffect(Unit) { viewModel.onEnter() }
    val view = state.view

    Box(Modifier.fillMaxSize().dotBackground()) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
            ScreenTopBar(
                onBack = onBack,
                action = {
                    CircleIconButton(
                        icon = AppIcons.Refresh,
                        contentDescription = stringResource(Res.string.cd_refresh_ranking),
                        onClick = viewModel::refresh,
                        enabled = !state.refreshing,
                        iconSize = 20.dp,
                        iconModifier = Modifier.spinning(state.refreshing),
                    )
                },
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 128.dp + navigationBarsBottom()),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(Res.string.ranking_title),
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(text = stringResource(Res.string.ranking_subtitle), fontSize = 16.sp, color = DevFestColors.Muted)
                    UpdatedLabel(state)
                }
                if (view == null) {
                    RankingSkeleton()
                } else {
                    Podium(view.podium)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        view.list.forEach { item ->
                            when (item) {
                                is RankingListItem.Row -> RankingRow(item.entry)
                                RankingListItem.Gap -> RankingGapRow()
                            }
                        }
                    }
                }
            }
        }
        if (view != null) {
            PositionBar(view, Modifier.align(Alignment.BottomCenter))
        }
    }
}

/** "Actualizado a las HH:MM" / "Actualizando…" / "Actualizado hace un momento". */
@Composable
private fun UpdatedLabel(state: RankingUiState) {
    val text = when {
        state.refreshing -> stringResource(Res.string.ranking_updating)
        state.justRefreshed -> stringResource(Res.string.ranking_updated_now)
        state.view != null -> stringResource(Res.string.ranking_updated_at, formatHour(state.view.updatedAt))
        else -> ""
    }
    Text(
        text = text,
        fontFamily = LocalDevFestFonts.current.mono,
        fontSize = 12.sp,
        color = DevFestColors.Muted,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
}

/** El ícono gira mientras se actualiza; con "reducir movimiento" solo se atenúa. */
@Composable
private fun Modifier.spinning(active: Boolean): Modifier {
    if (!active) return this
    if (LocalReduceMotion.current) return graphicsLayer { alpha = 0.5f }
    val rotation = rememberInfiniteTransition(label = "refresh").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation",
    )
    return graphicsLayer { rotationZ = rotation.value }
}

/** Barra fija inferior "Tu posición #N de M" con el saldo (US7, escenario 5). */
@Composable
private fun PositionBar(view: RankingView, modifier: Modifier = Modifier) {
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
                .solidShadow(DevFestColors.Sky, DevFestDimens.ShadowM, shape)
                .heightIn(min = 70.dp)
                .clip(shape)
                .background(DevFestColors.Ink)
                .padding(start = 12.dp, end = 18.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Avatar(
                name = view.me.fullName,
                photoUrl = view.me.photoUrl,
                size = 46.dp,
                background = DevFestColors.SkyTint,
                borderWidth = DevFestDimens.BorderThin,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.ranking_your_position),
                    fontSize = 13.sp,
                    color = DevFestColors.Card.copy(alpha = 0.75f),
                )
                Text(
                    text = stringResource(Res.string.ranking_position_of, view.me.position, view.totalParticipants),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DevFestColors.Card,
                )
            }
            Box(
                Modifier
                    .heightIn(min = 36.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(DevFestColors.Card)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                GemCount(view.me.gems, fontSize = 15, iconSize = 18, delayMillis = 320)
            }
        }
    }
}

@Composable
private fun RankingSkeleton() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
        listOf(150.dp, 190.dp, 130.dp).forEach { height ->
            SkeletonBlock(Modifier.weight(1f).height(height), RoundedCornerShape(18.dp))
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(5) { SkeletonBlock(Modifier.fillMaxWidth().height(62.dp), RoundedCornerShape(18.dp)) }
    }
}
