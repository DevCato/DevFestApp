package pe.gdg.open.devfest.app.ui.screens.talk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pe.gdg.open.devfest.app.domain.model.Speaker
import pe.gdg.open.devfest.app.domain.model.Talk
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.cd_bookmark_add
import pe.gdg.open.devfest.app.resources.cd_bookmark_remove
import pe.gdg.open.devfest.app.resources.detail_about
import pe.gdg.open.devfest.app.resources.detail_add
import pe.gdg.open.devfest.app.resources.detail_duration
import pe.gdg.open.devfest.app.resources.detail_hour
import pe.gdg.open.devfest.app.resources.detail_level
import pe.gdg.open.devfest.app.resources.detail_not_found
import pe.gdg.open.devfest.app.resources.detail_position
import pe.gdg.open.devfest.app.resources.detail_room
import pe.gdg.open.devfest.app.resources.detail_saved
import pe.gdg.open.devfest.app.resources.detail_speaker
import pe.gdg.open.devfest.app.resources.detail_speaker_tbc
import pe.gdg.open.devfest.app.resources.detail_speaker_tbc_text
import pe.gdg.open.devfest.app.resources.detail_speakers
import pe.gdg.open.devfest.app.resources.detail_topics
import pe.gdg.open.devfest.app.resources.detail_track
import pe.gdg.open.devfest.app.ui.components.AppButton
import pe.gdg.open.devfest.app.ui.components.AppIcons
import pe.gdg.open.devfest.app.ui.components.Avatar
import pe.gdg.open.devfest.app.ui.components.BookmarkIcon
import pe.gdg.open.devfest.app.ui.components.CircleIconButton
import pe.gdg.open.devfest.app.ui.components.LiveBadge
import pe.gdg.open.devfest.app.ui.components.ScreenTopBar
import pe.gdg.open.devfest.app.ui.components.SkeletonBlock
import pe.gdg.open.devfest.app.ui.components.TrackChip
import pe.gdg.open.devfest.app.ui.components.composeColor
import pe.gdg.open.devfest.app.ui.components.dotBackground
import pe.gdg.open.devfest.app.ui.components.durationLabel
import pe.gdg.open.devfest.app.ui.components.levelLabel
import pe.gdg.open.devfest.app.ui.components.solidShadow
import pe.gdg.open.devfest.app.ui.components.timeRange
import pe.gdg.open.devfest.app.ui.components.tint
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts
import pe.gdg.open.devfest.app.ui.util.navigationBarsBottom
import pe.gdg.open.devfest.app.ui.util.rememberSingleTap

/** Pantalla 04 del prototipo (US4). */
@Composable
fun TalkDetailScreen(
    talkId: String,
    onBack: () -> Unit,
    viewModel: TalkDetailViewModel = koinViewModel { parametersOf(talkId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val talk = state.talk
    val toggleSaved = rememberSingleTap(viewModel::toggleSaved)

    Box(Modifier.fillMaxSize().dotBackground()) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
            ScreenTopBar(
                onBack = onBack,
                center = {
                    state.position?.let {
                        Text(
                            text = stringResource(Res.string.detail_position, it.number, it.total),
                            fontFamily = LocalDevFestFonts.current.mono,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DevFestColors.Muted,
                        )
                    }
                },
                action = talk?.let {
                    {
                        CircleIconButton(
                            icon = AppIcons.Bookmark,
                            contentDescription = stringResource(
                                if (state.saved) Res.string.cd_bookmark_remove else Res.string.cd_bookmark_add,
                            ),
                            onClick = toggleSaved,
                        ) {
                            BookmarkIcon(saved = state.saved, color = it.track.composeColor, modifier = Modifier.size(20.dp))
                        }
                    }
                },
            )
            when {
                talk != null -> TalkContent(talk, state.speakerToBeConfirmed, Modifier.weight(1f))
                state.notFound -> Text(
                    text = stringResource(Res.string.detail_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = DevFestColors.Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                )
                else -> SkeletonBlock(
                    Modifier.padding(24.dp).fillMaxWidth().heightIn(min = 260.dp),
                    RoundedCornerShape(DevFestDimens.RadiusL),
                )
            }
        }
        if (talk != null) {
            SaveButton(
                saved = state.saved,
                trackColor = talk.track.composeColor,
                onClick = toggleSaved,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TalkContent(talk: Talk, speakerToBeConfirmed: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 130.dp + navigationBarsBottom()),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        val shape = RoundedCornerShape(DevFestDimens.RadiusL)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .solidShadow(talk.track.composeColor, DevFestDimens.ShadowL, shape)
                .clip(shape)
                .background(DevFestColors.Card)
                .border(DevFestDimens.Border, DevFestColors.Ink, shape)
                .padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (talk.isLive) LiveBadge(large = true)
                TrackChip(talk.track, label = stringResource(Res.string.detail_track, talk.track.name), large = true)
            }
            Text(
                text = talk.title,
                fontSize = 30.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.027).em,
                color = DevFestColors.Ink,
                modifier = Modifier.semantics { heading() },
            )
            Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoTile(stringResource(Res.string.detail_hour), timeRange(talk), mono = true, modifier = Modifier.weight(1f))
                    InfoTile(stringResource(Res.string.detail_room), talk.room.ifBlank { "—" }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoTile(stringResource(Res.string.detail_duration), durationLabel(talk), modifier = Modifier.weight(1f))
                    InfoTile(stringResource(Res.string.detail_level), levelLabel(talk.level), modifier = Modifier.weight(1f))
                }
            }
        }

        if (talk.description.isNotBlank()) {
            Section(stringResource(Res.string.detail_about)) {
                Text(text = talk.description, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Section(stringResource(if (talk.speakers.size > 1) Res.string.detail_speakers else Res.string.detail_speaker)) {
            if (speakerToBeConfirmed) {
                SpeakerCard(
                    name = stringResource(Res.string.detail_speaker_tbc),
                    subtitle = stringResource(Res.string.detail_speaker_tbc_text),
                    photoUrl = null,
                    avatarName = "?",
                    avatarTint = talk.track.tint,
                )
            } else {
                talk.speakers.forEach { speaker ->
                    SpeakerCard(
                        name = speaker.name,
                        subtitle = speaker.subtitle(),
                        photoUrl = speaker.photoUrl,
                        avatarName = speaker.name,
                        avatarTint = talk.track.tint,
                    )
                }
            }
        }

        if (talk.topics.isNotEmpty()) {
            Section(stringResource(Res.string.detail_topics)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    talk.topics.forEach { TopicPill(it) }
                }
            }
        }
    }
}

private fun Speaker.subtitle(): String? =
    listOfNotNull(role?.takeIf { it.isNotBlank() }, company?.takeIf { it.isNotBlank() })
        .joinToString(" · ")
        .ifBlank { null }

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() },
        )
        content()
    }
}

@Composable
private fun InfoTile(label: String, value: String, modifier: Modifier = Modifier, mono: Boolean = false) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DevFestColors.Soft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = label, fontSize = 12.sp, color = DevFestColors.Muted)
        Text(
            text = value,
            fontFamily = if (mono) LocalDevFestFonts.current.mono else LocalDevFestFonts.current.outfit,
            fontSize = 15.sp,
            fontWeight = if (mono) FontWeight.SemiBold else FontWeight.Bold,
            color = DevFestColors.Ink,
        )
    }
}

@Composable
private fun SpeakerCard(
    name: String,
    subtitle: String?,
    photoUrl: String?,
    avatarName: String,
    avatarTint: Color,
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DevFestColors.Card)
            .border(DevFestDimens.Border, DevFestColors.Ink, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Avatar(
            name = avatarName,
            photoUrl = photoUrl,
            size = 54.dp,
            background = avatarTint,
            borderWidth = DevFestDimens.Border,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DevFestColors.Ink)
            if (subtitle != null) Text(text = subtitle, fontSize = 14.sp, color = DevFestColors.Muted)
        }
    }
}

@Composable
private fun TopicPill(topic: String) {
    val shape = RoundedCornerShape(percent = 50)
    Box(
        modifier = Modifier
            .heightIn(min = 34.dp)
            .clip(shape)
            .border(DevFestDimens.BorderThin, DevFestColors.Ink, shape)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = topic,
            fontFamily = LocalDevFestFonts.current.mono,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = DevFestColors.Ink,
        )
    }
}

/** Botón fijo inferior "Agregar a mi agenda" / "Guardada en tu agenda" (US4, escenario 3). */
@Composable
private fun SaveButton(saved: Boolean, trackColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(0f to Color.Transparent, 0.3f to DevFestColors.Background))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 28.dp),
    ) {
        AppButton(
            text = stringResource(if (saved) Res.string.detail_saved else Res.string.detail_add),
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = if (saved) DevFestColors.Card else DevFestColors.Ink,
            contentColor = if (saved) DevFestColors.Ink else DevFestColors.Card,
            shadowColor = DevFestColors.Sky,
            height = 58.dp,
            textStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp),
            leadingIcon = {
                BookmarkIcon(
                    saved = saved,
                    color = trackColor,
                    outline = if (saved) DevFestColors.Ink else DevFestColors.Card,
                    modifier = Modifier.size(20.dp),
                )
            },
        )
    }
}
