package pe.gdg.open.devfest.app.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.cd_profile_photo
import pe.gdg.open.devfest.app.resources.my_agenda_count
import pe.gdg.open.devfest.app.resources.profile_connected_with
import pe.gdg.open.devfest.app.resources.profile_gems_count
import pe.gdg.open.devfest.app.resources.profile_logout
import pe.gdg.open.devfest.app.resources.profile_menu_agenda
import pe.gdg.open.devfest.app.resources.profile_menu_agenda_subtitle
import pe.gdg.open.devfest.app.resources.profile_menu_gems
import pe.gdg.open.devfest.app.resources.profile_menu_my_agenda
import pe.gdg.open.devfest.app.resources.profile_menu_ranking
import pe.gdg.open.devfest.app.resources.profile_rank
import pe.gdg.open.devfest.app.resources.provider_apple
import pe.gdg.open.devfest.app.resources.provider_github
import pe.gdg.open.devfest.app.resources.provider_google
import pe.gdg.open.devfest.app.resources.sticker_asterisk
import pe.gdg.open.devfest.app.resources.sticker_plus
import pe.gdg.open.devfest.app.resources.sticker_ring_yellow
import pe.gdg.open.devfest.app.ui.components.AppButton
import pe.gdg.open.devfest.app.ui.components.AppIcons
import pe.gdg.open.devfest.app.ui.components.Avatar
import pe.gdg.open.devfest.app.ui.components.FloatingSticker
import pe.gdg.open.devfest.app.ui.components.NavCardButton
import pe.gdg.open.devfest.app.ui.components.ProfileLegalText
import pe.gdg.open.devfest.app.ui.components.ScreenBackButton
import pe.gdg.open.devfest.app.ui.components.ScreenTopBar
import pe.gdg.open.devfest.app.ui.components.StickerSpec
import pe.gdg.open.devfest.app.ui.components.dotBackground
import pe.gdg.open.devfest.app.ui.components.riseIn
import pe.gdg.open.devfest.app.ui.components.solidShadow
import pe.gdg.open.devfest.app.ui.modal.ModalRequestEffect
import pe.gdg.open.devfest.app.ui.theme.DevFestColors

private val ringSticker = StickerSpec(Res.drawable.sticker_ring_yellow, DpSize(120.dp, 120.dp), (-12).dp, 20.dp, -18f, 5100)
private val plusSticker = StickerSpec(Res.drawable.sticker_plus, DpSize(44.dp, 44.dp), 8.dp, (-18).dp, 26f, 3700, baseRotation = -14f)
private val asteriskSticker = StickerSpec(Res.drawable.sticker_asterisk, DpSize(30.dp, 30.dp), 5.dp, (-14).dp, 90f, 4400)

/** Pantalla 05 del prototipo (US8). */
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onAgendaClick: () -> Unit,
    onMyAgendaClick: () -> Unit,
    onGemsClick: () -> Unit,
    onRankingClick: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ModalRequestEffect(viewModel.modals)
    LaunchedEffect(Unit) { viewModel.onEnter() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileEvent.LoggedOut -> onLoggedOut()
            }
        }
    }

    // Alto del pie fijo: el contenido deja ese espacio al final para no quedar tapado.
    var footerHeightPx by remember { mutableIntStateOf(0) }
    val footerHeight = with(LocalDensity.current) { footerHeightPx.toDp() }

    Box(Modifier.fillMaxSize().dotBackground()) {
        // Stickers decorativos (sin hashtag en esta pantalla).
        FloatingSticker(ringSticker, Modifier.align(Alignment.TopEnd).offset(x = 44.dp, y = 120.dp), delayMillis = 80)
        FloatingSticker(plusSticker, Modifier.align(Alignment.TopStart).offset(x = 34.dp, y = 150.dp), delayMillis = 150)
        FloatingSticker(asteriskSticker, Modifier.align(Alignment.TopEnd).offset(x = (-40).dp, y = 92.dp), delayMillis = 220)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .padding(bottom = footerHeight),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            Account(state, Modifier.riseIn(40))
            Menu(state, onAgendaClick, onMyAgendaClick, onGemsClick, onRankingClick, Modifier.riseIn(120))
        }
        ScreenBackButton(
            onBack = onBack,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .padding(start = 24.dp, top = 16.dp),
        )

        // Pie fijo abajo; el contenido pasa por detrás con un degradado del fondo, como en la barra de tabs.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { footerHeightPx = it.height }
                .background(Brush.verticalGradient(0f to Color.Transparent, 0.25f to DevFestColors.Background))
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                .padding(bottom = 10.dp),
        ) {
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp).riseIn(200),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AppButton(
                    text = stringResource(Res.string.profile_logout),
                    onClick = viewModel::onLogoutClick,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = DevFestColors.Card,
                    contentColor = DevFestColors.Ink,
                    shadowColor = DevFestColors.Ink,
                    height = 56.dp,
                    textStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp),
                    leadingIcon = {
                        Box(
                            Modifier.size(30.dp).clip(CircleShape).background(DevFestColors.Soft),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(painterResource(AppIcons.Logout), contentDescription = null, tint = DevFestColors.Ink, modifier = Modifier.size(16.dp))
                        }
                    },
                )
                ProfileLegalText(Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun Account(state: ProfileUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // El mismo espacio a la izquierda que la sombra ocupa a la derecha: la foto queda centrada.
        Box(Modifier.absolutePadding(left = 6.dp).solidShadow(DevFestColors.Sky, 6.dp, CircleShape)) {
            Avatar(
                name = state.fullName,
                photoUrl = state.photoUrl,
                size = 112.dp,
                borderWidth = 3.dp,
                contentDescription = stringResource(Res.string.cd_profile_photo),
            )
        }
        Text(
            text = state.fullName,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.02).em,
            color = DevFestColors.Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp).semantics { heading() },
        )
        state.provider?.let { ProviderPill(it) }
    }
}

@Composable
private fun ProviderPill(provider: AuthProvider) {
    val (name, logo) = when (provider) {
        AuthProvider.GOOGLE -> stringResource(Res.string.provider_google) to AppIcons.LogoGoogle
        AuthProvider.APPLE -> stringResource(Res.string.provider_apple) to AppIcons.LogoApple
        AuthProvider.GITHUB -> stringResource(Res.string.provider_github) to AppIcons.LogoGithub
    }
    Row(
        modifier = Modifier
            .heightIn(min = 32.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(DevFestColors.Skeleton)
            .padding(horizontal = 14.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProviderLogo(provider, logo)
        Text(
            text = stringResource(Res.string.profile_connected_with, name),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = DevFestColors.Ink,
        )
    }
}

/** El logo de Apple es blanco: va sobre un círculo de tinta para verse en el fondo claro. */
@Composable
private fun ProviderLogo(provider: AuthProvider, logo: DrawableResource) {
    if (provider == AuthProvider.APPLE) {
        Box(Modifier.size(20.dp).clip(CircleShape).background(DevFestColors.Ink), contentAlignment = Alignment.Center) {
            Image(painterResource(logo), contentDescription = null, modifier = Modifier.size(12.dp))
        }
    } else {
        Image(painterResource(logo), contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun Menu(
    state: ProfileUiState,
    onAgendaClick: () -> Unit,
    onMyAgendaClick: () -> Unit,
    onGemsClick: () -> Unit,
    onRankingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        MenuItem(
            title = stringResource(Res.string.profile_menu_agenda),
            subtitle = stringResource(Res.string.profile_menu_agenda_subtitle),
            color = DevFestColors.Blue,
            icon = AppIcons.Calendar,
            onClick = onAgendaClick,
        )
        MenuItem(
            title = stringResource(Res.string.profile_menu_my_agenda),
            subtitle = pluralStringResource(Res.plurals.my_agenda_count, state.savedCount, state.savedCount),
            color = DevFestColors.Green,
            icon = AppIcons.Bookmark,
            onClick = onMyAgendaClick,
        )
        MenuItem(
            title = stringResource(Res.string.profile_menu_gems),
            subtitle = state.gems?.let { pluralStringResource(Res.plurals.profile_gems_count, it, it) }.orEmpty(),
            color = DevFestColors.Yellow,
            icon = null,
            onClick = onGemsClick,
        )
        MenuItem(
            title = stringResource(Res.string.profile_menu_ranking),
            subtitle = state.rank?.let { stringResource(Res.string.profile_rank, it) }.orEmpty(),
            color = DevFestColors.Purple,
            icon = AppIcons.Trophy,
            onClick = onRankingClick,
        )
    }
}

@Composable
private fun MenuItem(
    title: String,
    subtitle: String,
    color: Color,
    icon: DrawableResource?,
    onClick: () -> Unit,
) {
    NavCardButton(
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        shadowColor = color,
        iconBackground = color.copy(alpha = 0x2B / 255f),
        icon = {
            if (icon != null) {
                Icon(painterResource(icon), contentDescription = null, tint = DevFestColors.Ink, modifier = Modifier.size(22.dp))
            } else {
                Image(painterResource(AppIcons.GemFilled), contentDescription = null, modifier = Modifier.size(24.dp))
            }
        },
    )
}
