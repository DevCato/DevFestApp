package pe.gdg.open.devfest.app.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.app_name
import pe.gdg.open.devfest.app.resources.connecting
import pe.gdg.open.devfest.app.resources.event_hashtag
import pe.gdg.open.devfest.app.resources.login_apple
import pe.gdg.open.devfest.app.resources.login_card_text
import pe.gdg.open.devfest.app.resources.login_card_title
import pe.gdg.open.devfest.app.resources.login_date
import pe.gdg.open.devfest.app.resources.login_github
import pe.gdg.open.devfest.app.resources.login_google
import pe.gdg.open.devfest.app.resources.login_intro
import pe.gdg.open.devfest.app.ui.components.AppButton
import pe.gdg.open.devfest.app.ui.components.AppIcons
import pe.gdg.open.devfest.app.ui.components.BrandTags
import pe.gdg.open.devfest.app.ui.components.FloatingSticker
import pe.gdg.open.devfest.app.ui.components.LoginLegalText
import pe.gdg.open.devfest.app.ui.components.Stickers
import pe.gdg.open.devfest.app.ui.components.dotBackground
import pe.gdg.open.devfest.app.ui.components.riseIn
import pe.gdg.open.devfest.app.ui.components.solidShadow
import pe.gdg.open.devfest.app.ui.modal.ModalRequestEffect
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts

/** Pantalla 01 del prototipo (US1). */
@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ModalRequestEffect(viewModel.modals)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.SignedIn -> onSignedIn()
            }
        }
    }
    LoginContent(state = state, onSignIn = viewModel::signIn)
}

@Composable
private fun LoginContent(state: LoginUiState, onSignIn: (AuthProvider) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().dotBackground()) {
        FloatingSticker(Stickers.Ring, Modifier.align(Alignment.TopEnd).offset(40.dp, (-38).dp), delayMillis = 70)
        FloatingSticker(Stickers.Asterisk, Modifier.align(Alignment.TopEnd).offset((-96).dp, 36.dp), delayMillis = 140)
        FloatingSticker(Stickers.Plus, Modifier.align(Alignment.TopEnd).offset((-26).dp, 232.dp), delayMillis = 210)
        FloatingSticker(Stickers.Arc, Modifier.align(Alignment.BottomStart).offset((-62).dp, 70.dp), delayMillis = 280)
        FloatingSticker(Stickers.Cross, Modifier.align(Alignment.BottomStart).offset(120.dp, (-22).dp), delayMillis = 350)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(start = 24.dp, end = 24.dp, top = 44.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Header(Modifier.riseIn(60))
                SignInCard(state, onSignIn, Modifier.padding(top = 30.dp).riseIn(180))
                LoginLegalText(Modifier.fillMaxWidth().padding(top = 24.dp).riseIn(240))
            }
            Hashtag(Modifier.align(Alignment.End).padding(top = 24.dp).riseIn(300))
        }
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    val fonts = LocalDevFestFonts.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Lockup "{ DevFest Lima }": un solo texto para que se ajuste con letra grande.
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = DevFestColors.Blue, fontSize = 52.sp, fontWeight = FontWeight.Bold)) {
                    append("{ ")
                }
                append(stringResource(Res.string.app_name))
                withStyle(SpanStyle(color = DevFestColors.Yellow, fontSize = 52.sp, fontWeight = FontWeight.Bold)) {
                    append(" }")
                }
            },
            style = TextStyle(
                fontFamily = fonts.outfit,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.03).em,
                lineHeight = 52.sp,
                color = DevFestColors.Ink,
            ),
            modifier = Modifier.semantics { heading() },
        )
        BrandTags()
        Text(
            text = stringResource(Res.string.login_intro),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 25.5.sp),
            color = DevFestColors.Muted,
            modifier = Modifier.padding(top = 4.dp).widthIn(max = 290.dp),
        )
        DatePill()
    }
}

@Composable
private fun DatePill() {
    Row(
        modifier = Modifier
            .heightIn(min = 30.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(DevFestColors.Skeleton)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(AppIcons.Calendar),
            contentDescription = null,
            tint = DevFestColors.Ink,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = stringResource(Res.string.login_date),
            fontFamily = LocalDevFestFonts.current.mono,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = DevFestColors.Ink,
        )
    }
}

private data class ProviderStyle(
    val provider: AuthProvider,
    val label: StringResource,
    val logo: DrawableResource,
    val logoSize: Int,
    val container: Color,
    val content: Color,
    val shadow: Color,
)

private val providers = listOf(
    ProviderStyle(AuthProvider.GOOGLE, Res.string.login_google, AppIcons.LogoGoogle, 22, DevFestColors.Card, DevFestColors.Ink, DevFestColors.Ink),
    // Apple: botón negro, según sus guías.
    ProviderStyle(AuthProvider.APPLE, Res.string.login_apple, AppIcons.LogoApple, 22, DevFestColors.Ink, DevFestColors.Card, DevFestColors.Sky),
    ProviderStyle(AuthProvider.GITHUB, Res.string.login_github, AppIcons.LogoGithub, 24, DevFestColors.Card, DevFestColors.Ink, DevFestColors.Ink),
)

@Composable
private fun SignInCard(
    state: LoginUiState,
    onSignIn: (AuthProvider) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(DevFestDimens.RadiusL)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .solidShadow(DevFestColors.Sky, DevFestDimens.ShadowL, shape)
            .clip(shape)
            .background(DevFestColors.Card)
            .border(DevFestDimens.Border, DevFestColors.Ink, shape)
            .padding(horizontal = 22.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.login_card_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            Row(
                modifier = Modifier.clearAndSetSemantics { },
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                repeat(3) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(DevFestColors.Yellow)
                            .border(DevFestDimens.BorderThin, DevFestColors.Ink, CircleShape),
                    )
                }
            }
        }
        Text(
            text = stringResource(Res.string.login_card_text),
            style = MaterialTheme.typography.bodyMedium,
            color = DevFestColors.Muted,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        providers.forEach { style ->
            AppButton(
                text = stringResource(style.label),
                onClick = { onSignIn(style.provider) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isEnabled(style.provider),
                loading = state.isConnecting(style.provider),
                loadingText = stringResource(Res.string.connecting),
                shadowColor = style.shadow,
                containerColor = style.container,
                contentColor = style.content,
                height = 56.dp,
                textStyle = TextStyle(
                    fontFamily = LocalDevFestFonts.current.outfit,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                leadingIcon = {
                    Image(
                        painter = painterResource(style.logo),
                        contentDescription = null,
                        modifier = Modifier.size(style.logoSize.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun Hashtag(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 32.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(DevFestColors.Ink)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.event_hashtag),
            fontFamily = LocalDevFestFonts.current.mono,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = DevFestColors.Card,
        )
    }
}
