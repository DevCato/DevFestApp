package pe.gdg.open.devfest.app.ui.screens.agenda

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pe.gdg.open.devfest.app.domain.logic.AgendaFilter
import pe.gdg.open.devfest.app.domain.logic.AgendaRow
import pe.gdg.open.devfest.app.domain.logic.formatHour
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.action_try_again
import pe.gdg.open.devfest.app.resources.agenda_empty_text
import pe.gdg.open.devfest.app.resources.agenda_empty_title
import pe.gdg.open.devfest.app.resources.agenda_title
import pe.gdg.open.devfest.app.resources.connecting
import pe.gdg.open.devfest.app.resources.offline_text
import pe.gdg.open.devfest.app.resources.offline_title
import pe.gdg.open.devfest.app.resources.sticker_plus
import pe.gdg.open.devfest.app.resources.sticker_ring
import pe.gdg.open.devfest.app.resources.track_empty_action
import pe.gdg.open.devfest.app.resources.track_empty_text
import pe.gdg.open.devfest.app.resources.track_empty_title
import pe.gdg.open.devfest.app.ui.components.AppHeader
import pe.gdg.open.devfest.app.ui.components.AppIcons
import pe.gdg.open.devfest.app.ui.components.BottomBarContentPadding
import pe.gdg.open.devfest.app.ui.components.BottomTabBar
import pe.gdg.open.devfest.app.ui.components.BrandTags
import pe.gdg.open.devfest.app.ui.components.BreakRow
import pe.gdg.open.devfest.app.ui.components.CollapsingHeaderState
import pe.gdg.open.devfest.app.ui.components.EmptyState
import pe.gdg.open.devfest.app.ui.components.FilterRow
import pe.gdg.open.devfest.app.ui.components.FloatingSticker
import pe.gdg.open.devfest.app.ui.components.MainTab
import pe.gdg.open.devfest.app.ui.components.NowLineRow
import pe.gdg.open.devfest.app.ui.components.StickerSpec
import pe.gdg.open.devfest.app.ui.components.TalkCard
import pe.gdg.open.devfest.app.ui.components.TimelineRow
import pe.gdg.open.devfest.app.ui.components.agendaInfoLine
import pe.gdg.open.devfest.app.ui.components.collapseFade
import pe.gdg.open.devfest.app.ui.components.collapsible
import pe.gdg.open.devfest.app.ui.components.collapsingOffset
import pe.gdg.open.devfest.app.ui.components.dotBackground
import pe.gdg.open.devfest.app.ui.components.rememberCollapsingHeaderState
import pe.gdg.open.devfest.app.ui.modal.ModalRequestEffect
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.LocalReduceMotion
import pe.gdg.open.devfest.app.ui.theme.EaseOutStrong
import pe.gdg.open.devfest.app.ui.util.navigationBarsBottom

/** Stickers de la cabecera de la Agenda (más chicos y lentos que los del Login). */
private val headerRing = StickerSpec(Res.drawable.sticker_ring, DpSize(60.dp, 60.dp), (-5).dp, 8.dp, -12f, 6200)
private val headerPlus = StickerSpec(Res.drawable.sticker_plus, DpSize(32.dp, 32.dp), 4.dp, (-9).dp, 16f, 5400, baseRotation = 18f)

/** Fracción del título ya escondida a partir de la cual salen los stickers. */
private const val STICKERS_HIDE_AT = 0.1f

/** Cambio de track: la lista vieja sale rápido y la nueva entra. */
private const val TRACK_EXIT_MILLIS = 120
private const val TRACK_ENTER_MILLIS = 220

/** Pantallas 02, 02b y 02c del prototipo (US2). */
@Composable
fun AgendaScreen(
    onTalkClick: (String) -> Unit,
    onTabSelected: (MainTab) -> Unit,
    onProfileClick: () -> Unit,
    onGemsClick: () -> Unit,
    viewModel: AgendaViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ModalRequestEffect(viewModel.modals)
    // Cada vez que se entra a la pantalla se vuelve a pedir la agenda (FR-017).
    LaunchedEffect(Unit) { viewModel.onEnter() }

    AgendaLayout(
        state = state,
        onFilter = viewModel::selectFilter,
        onProfileClick = onProfileClick,
        onGemsClick = onGemsClick,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        onTalkClick = onTalkClick,
        onToggleSaved = viewModel::toggleSaved,
        onTabSelected = onTabSelected,
    )
}

/** La lista ocupa toda la pantalla y la cabecera flota encima, escondiéndose al bajar. */
@Composable
private fun AgendaLayout(
    state: AgendaUiState,
    onFilter: (AgendaFilter) -> Unit,
    onProfileClick: () -> Unit,
    onGemsClick: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onTalkClick: (String) -> Unit,
    onToggleSaved: (String) -> Unit,
    onTabSelected: (MainTab) -> Unit,
) {
    val header = rememberCollapsingHeaderState()
    // Alto completo de la cabecera (barra de estado incluida), sin contar lo que ya se escondió.
    var headerHeightPx by remember { mutableIntStateOf(0) }
    // Otro track es otra lista: empieza desde arriba y con la cabecera entera, sin hueco encima.
    var lastFilter by remember { mutableStateOf(state.filter) }
    LaunchedEffect(state.filter) {
        if (state.filter == lastFilter) return@LaunchedEffect
        lastFilter = state.filter
        header.expand()
    }
    Box(Modifier.fillMaxSize().dotBackground()) {
        AgendaBody(
            state = state,
            header = header,
            headerHeightPx = headerHeightPx,
            onRefresh = onRefresh,
            onRetry = onRetry,
            onShowAll = { onFilter(AgendaFilter.ALL) },
            onTalkClick = onTalkClick,
            onToggleSaved = onToggleSaved,
            modifier = Modifier.fillMaxSize(),
        )
        AgendaHeader(
            state = state,
            header = header,
            onFilter = onFilter,
            onProfileClick = onProfileClick,
            onGemsClick = onGemsClick,
            modifier = Modifier.onSizeChanged { headerHeightPx = it.height },
        )
        BottomTabBar(
            selected = MainTab.AGENDA,
            savedCount = state.savedCount,
            onSelect = onTabSelected,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun AgendaHeader(
    state: AgendaUiState,
    header: CollapsingHeaderState,
    onFilter: (AgendaFilter) -> Unit,
    onProfileClick: () -> Unit,
    onGemsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        // Fija y dibujada encima: el título se esconde por debajo de ella.
        Column(
            Modifier
                .zIndex(1f)
                .background(DevFestColors.Background)
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            AppHeader(user = state.user, onGemsClick = onGemsClick, onProfileClick = onProfileClick)
        }
        // Título, filtros y línea suben juntos; solo el título y los stickers quedan ocultos.
        Column(
            Modifier
                .fillMaxWidth()
                .collapsingOffset(header)
                .background(DevFestColors.Background),
        ) {
            Box(Modifier.fillMaxWidth().collapsible(header)) {
                // Los stickers no siguen al dedo: al empezar a esconderse salen, y al volver repiten
                // la entrada de cuando carga la pantalla.
                val stickersVisible by remember { derivedStateOf { header.collapsedFraction < STICKERS_HIDE_AT } }
                FloatingSticker(
                    headerRing,
                    Modifier.align(Alignment.TopEnd).offset(x = (-5).dp, y = 0.dp),
                    visible = stickersVisible,
                )
                FloatingSticker(
                    headerPlus,
                    Modifier.align(Alignment.TopEnd).offset(x = (-109).dp, y = 10.dp),
                    delayMillis = 120,
                    visible = stickersVisible,
                )
                Text(
                    text = stringResource(Res.string.agenda_title),
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier
                        .collapseFade(header, fadeEnd = 0.6f, minScale = 0.94f, origin = TransformOrigin(0f, 1f))
                        .padding(horizontal = DevFestDimens.ScreenPadding + 4.dp)
                        .padding(bottom = if (state.showFilters) 12.dp else 0.dp)
                        .semantics { heading() },
                )
            }
            if (state.showFilters) {
                FilterRow(selected = state.filter, onSelect = onFilter)
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(DevFestDimens.Border).background(DevFestColors.Ink))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgendaBody(
    state: AgendaUiState,
    header: CollapsingHeaderState,
    headerHeightPx: Int,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onShowAll: () -> Unit,
    onTalkClick: (String) -> Unit,
    onToggleSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshState = rememberPullToRefreshState()
    // El contenido empieza bajo la cabecera; al esconderse el título, las charlas pasan por debajo de su sitio.
    val top = with(LocalDensity.current) { headerHeightPx.toDp() } + 24.dp
    val contentModifier = Modifier.fillMaxSize().nestedScroll(header.nestedScrollConnection)
    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxWidth(),
        state = refreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = refreshState,
                isRefreshing = state.refreshing,
                containerColor = DevFestColors.Card,
                color = DevFestColors.Ink,
                // Sale por debajo de la parte visible de la cabecera.
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer { translationY = headerHeightPx + header.offsetPx },
            )
        },
    ) {
        val reduceMotion = LocalReduceMotion.current
        val risePx = with(LocalDensity.current) { 12.dp.roundToPx() }
        // Al cambiar de track la lista vieja se va rápido y la nueva sube un poco al entrar.
        // Cada track tiene su propia lista, así que la nueva empieza arriba.
        AnimatedContent(
            targetState = state,
            contentKey = { it.filter },
            transitionSpec = {
                val fade = fadeIn(tween(TRACK_ENTER_MILLIS, delayMillis = TRACK_EXIT_MILLIS / 2, easing = EaseOutStrong))
                val enter = if (reduceMotion) {
                    fade
                } else {
                    fade + slideInVertically(tween(TRACK_ENTER_MILLIS, easing = EaseOutStrong)) { risePx }
                }
                (enter togetherWith fadeOut(tween(TRACK_EXIT_MILLIS, easing = EaseOutStrong))).using(null)
            },
            label = "track",
        ) { state ->
            AgendaBodyContent(state, top, contentModifier, onRetry, onShowAll, onTalkClick, onToggleSaved)
        }
    }
}

@Composable
private fun AgendaBodyContent(
    state: AgendaUiState,
    top: Dp,
    contentModifier: Modifier,
    onRetry: () -> Unit,
    onShowAll: () -> Unit,
    onTalkClick: (String) -> Unit,
    onToggleSaved: (String) -> Unit,
) {
    when {
        state.status == AgendaStatus.SKELETON -> ScrollableState(top, contentModifier) { AgendaSkeleton() }
        state.status == AgendaStatus.OFFLINE -> ScrollableState(top, contentModifier) {
            EmptyState(
                icon = AppIcons.WifiOff,
                title = stringResource(Res.string.offline_title),
                text = stringResource(Res.string.offline_text),
                actionLabel = stringResource(Res.string.action_try_again),
                actionLoading = state.retrying,
                actionLoadingLabel = stringResource(Res.string.connecting),
                onAction = onRetry,
                modifier = Modifier.padding(top = 20.dp),
            )
        }
        state.status == AgendaStatus.NO_TALKS -> ScrollableState(top, contentModifier) {
            EmptyState(
                icon = AppIcons.Calendar,
                title = stringResource(Res.string.agenda_empty_title),
                text = stringResource(Res.string.agenda_empty_text),
                modifier = Modifier.padding(top = 20.dp),
            )
        }
        state.trackEmpty -> ScrollableState(top, contentModifier) {
            EmptyState(
                icon = AppIcons.Calendar,
                title = stringResource(Res.string.track_empty_title),
                text = stringResource(Res.string.track_empty_text),
                actionLabel = stringResource(Res.string.track_empty_action),
                onAction = onShowAll,
                modifier = Modifier.padding(top = 20.dp),
            )
        }
        else -> AgendaList(state, top, onTalkClick, onToggleSaved, contentModifier)
    }
}

@Composable
private fun AgendaList(
    state: AgendaUiState,
    top: Dp,
    onTalkClick: (String) -> Unit,
    onToggleSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        state = rememberLazyListState(),
        contentPadding = PaddingValues(
            start = 24.dp,
            end = 24.dp,
            top = top,
            bottom = BottomBarContentPadding + navigationBarsBottom(),
        ),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        items(state.rows, key = { it.key }) { row ->
            when (row) {
                is AgendaRow.TalkRow -> TimelineRow(
                    time = if (row.showTime) formatHour(row.talk.startsAt) else null,
                    dimmed = row.isPast,
                ) {
                    TalkCard(
                        talk = row.talk,
                        info = agendaInfoLine(row.talk),
                        saved = row.talk.id in state.savedIds,
                        onClick = { onTalkClick(row.talk.id) },
                        onToggleSaved = { onToggleSaved(row.talk.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                is AgendaRow.BreakRow -> BreakRow(row.pause, row.showTime, row.isPast)
                is AgendaRow.NowRow -> NowLineRow(row.now)
            }
        }
    }
}

/** Contenedor desplazable para que el pull-to-refresh funcione también en los estados. */
@Composable
private fun ScrollableState(top: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = top, bottom = BottomBarContentPadding + navigationBarsBottom()),
    ) {
        content()
    }
}

@Preview
@Composable
private fun PreviewAgendaHeader(){
    AgendaLayout(
        state = AgendaUiState(),
        onFilter = {},
        onProfileClick = {},
        onGemsClick = {},
        onRefresh = {},
        onRetry = {},
        onTalkClick = {},
        onToggleSaved = {},
        onTabSelected = {},
    )
}