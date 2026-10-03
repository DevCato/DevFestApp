package pe.gdg.open.devfest.app.ui.screens.myagenda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pe.gdg.open.devfest.app.domain.logic.formatHour
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.my_agenda_all
import pe.gdg.open.devfest.app.resources.my_agenda_count
import pe.gdg.open.devfest.app.resources.my_agenda_empty_action
import pe.gdg.open.devfest.app.resources.my_agenda_empty_text
import pe.gdg.open.devfest.app.resources.my_agenda_empty_title
import pe.gdg.open.devfest.app.resources.my_agenda_title
import pe.gdg.open.devfest.app.ui.components.AppHeader
import pe.gdg.open.devfest.app.ui.components.AppIcons
import pe.gdg.open.devfest.app.ui.components.BottomBarContentPadding
import pe.gdg.open.devfest.app.ui.components.BottomTabBar
import pe.gdg.open.devfest.app.ui.components.ConflictBadge
import pe.gdg.open.devfest.app.ui.components.EmptyState
import pe.gdg.open.devfest.app.ui.components.MainTab
import pe.gdg.open.devfest.app.ui.components.TalkCard
import pe.gdg.open.devfest.app.ui.components.TimelineRow
import pe.gdg.open.devfest.app.ui.components.dotBackground
import pe.gdg.open.devfest.app.ui.components.myAgendaInfoLine
import pe.gdg.open.devfest.app.ui.modal.ModalRequestEffect
import pe.gdg.open.devfest.app.ui.screens.agenda.AgendaSkeleton
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.util.navigationBarsBottom

/** Pantalla 03 del prototipo (US3). */
@Composable
fun MyAgendaScreen(
    onTalkClick: (String) -> Unit,
    onTabSelected: (MainTab) -> Unit,
    onProfileClick: () -> Unit,
    onGemsClick: () -> Unit,
    viewModel: MyAgendaViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ModalRequestEffect(viewModel.modals)
    LaunchedEffect(Unit) { viewModel.onEnter() }

    Box(Modifier.fillMaxSize().dotBackground()) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
            AppHeader(user = state.user, onGemsClick = onGemsClick, onProfileClick = onProfileClick)
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 8.dp,
                    bottom = BottomBarContentPadding + navigationBarsBottom(),
                ),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                item(key = "title") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = pluralStringResource(Res.plurals.my_agenda_count, state.savedCount, state.savedCount),
                            fontSize = 15.sp,
                            color = DevFestColors.Muted,
                        )
                        Text(
                            text = stringResource(Res.string.my_agenda_title),
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier.semantics { heading() },
                        )
                    }
                }
                state.featured?.let { featured ->
                    item(key = "featured") {
                        FeaturedTalkCard(featured = featured, onClick = { onTalkClick(featured.talk.id) })
                    }
                }
                when {
                    state.loading -> item(key = "skeleton") { AgendaSkeleton(rows = 2) }
                    state.isEmpty -> item(key = "empty") {
                        EmptyState(
                            icon = AppIcons.Bookmark,
                            iconTint = DevFestColors.Blue,
                            title = stringResource(Res.string.my_agenda_empty_title),
                            text = stringResource(Res.string.my_agenda_empty_text),
                            actionLabel = stringResource(Res.string.my_agenda_empty_action),
                            onAction = { onTabSelected(MainTab.AGENDA) },
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                    else -> {
                        item(key = "all") {
                            Text(
                                text = stringResource(Res.string.my_agenda_all),
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                                modifier = Modifier.padding(top = 8.dp).semantics { heading() },
                            )
                        }
                        items(state.items, key = { it.talk.id }) { item ->
                            TimelineRow(
                                time = if (item.showTime) formatHour(item.talk.startsAt) else null,
                                dimmed = item.isPast,
                            ) {
                                TalkCard(
                                    talk = item.talk,
                                    info = myAgendaInfoLine(item.talk),
                                    saved = true,
                                    onClick = { onTalkClick(item.talk.id) },
                                    onToggleSaved = { viewModel.toggleSaved(item.talk.id) },
                                    modifier = Modifier.weight(1f),
                                    extraBadges = { if (item.conflict) ConflictBadge() },
                                )
                            }
                        }
                    }
                }
            }
        }
        BottomTabBar(
            selected = MainTab.MY_AGENDA,
            savedCount = state.savedCount,
            onSelect = onTabSelected,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
