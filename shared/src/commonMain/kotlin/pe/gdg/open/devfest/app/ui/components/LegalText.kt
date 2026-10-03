package pe.gdg.open.devfest.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.AppConfig
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.legal_login
import pe.gdg.open.devfest.app.resources.legal_privacy
import pe.gdg.open.devfest.app.resources.legal_profile
import pe.gdg.open.devfest.app.resources.legal_terms
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts

/** Un enlace dentro de un texto legal. */
data class LegalLink(val text: String, val url: String)

/**
 * "Al continuar aceptas los Términos y la Política de privacidad" (FR-005): gris pequeño,
 * centrado, con los enlaces en el mismo gris subrayados finos. "Política de privacidad" no se
 * parte entre líneas.
 */
@Composable
fun LoginLegalText(modifier: Modifier = Modifier) {
    val terms = stringResource(Res.string.legal_terms).nonBreaking()
    val privacy = stringResource(Res.string.legal_privacy).nonBreaking()
    LegalLinksText(
        text = stringResource(Res.string.legal_login, terms, privacy),
        links = listOf(LegalLink(terms, AppConfig.TERMS_URL), LegalLink(privacy, AppConfig.PRIVACY_URL)),
        modifier = modifier,
    )
}

/** "Términos · Política de privacidad" del Perfil (FR-005), con el mismo estilo. */
@Composable
fun ProfileLegalText(modifier: Modifier = Modifier) {
    val terms = stringResource(Res.string.legal_terms).nonBreaking()
    val privacy = stringResource(Res.string.legal_privacy).nonBreaking()
    LegalLinksText(
        text = stringResource(Res.string.legal_profile, terms, privacy),
        links = listOf(LegalLink(terms, AppConfig.TERMS_URL), LegalLink(privacy, AppConfig.PRIVACY_URL)),
        modifier = modifier,
    )
}

/** Texto legal con enlaces. Una URL vacía (aún no definida) no abre nada. */
@Composable
fun LegalLinksText(
    text: String,
    links: List<LegalLink>,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val annotated = remember(text, links) { annotateLinks(text, links, uriHandler) }
    Text(
        text = annotated,
        modifier = modifier,
        style = TextStyle(
            fontFamily = LocalDevFestFonts.current.outfit,
            fontSize = 12.5.sp,
            lineHeight = 18.75.sp,
            color = DevFestColors.Muted,
            textAlign = TextAlign.Center,
        ),
    )
}

private fun annotateLinks(text: String, links: List<LegalLink>, uriHandler: UriHandler): AnnotatedString =
    buildAnnotatedString {
        append(text)
        val styles = TextLinkStyles(
            style = SpanStyle(color = DevFestColors.Muted, textDecoration = TextDecoration.Underline),
        )
        links.forEach { link ->
            val start = text.indexOf(link.text)
            if (start < 0) return@forEach
            addLink(
                LinkAnnotation.Clickable(tag = link.text, styles = styles) {
                    if (link.url.isNotBlank()) uriHandler.openUri(link.url)
                },
                start,
                start + link.text.length,
            )
        }
    }

/** Espacios no separables: el texto no se parte entre líneas. */
fun String.nonBreaking(): String = replace(' ', ' ')
