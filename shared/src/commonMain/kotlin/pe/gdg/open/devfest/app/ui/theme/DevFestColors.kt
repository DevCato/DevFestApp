package pe.gdg.open.devfest.app.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Colores del prototipo (notas-para-plan.md, "Sistema de diseño"). Solo tema claro. */
object DevFestColors {
    val Background = Color(0xFFF1F1F1)
    val Card = Color(0xFFFFFFFF)
    val Ink = Color(0xFF1E1E1E)
    val Muted = Color(0xFF5F6368)
    val Soft = Color(0xFFECECEC)
    val Skeleton = Color(0xFFE2E2E2)

    val Sky = Color(0xFF57CAFF)
    val SkyTint = Color(0xFFC6ECFF)
    val Yellow = Color(0xFFFBBC04)
    val YellowTint = Color(0xFFFDE293)
    val LiveRed = Color(0xFFD93025)
    val Red = Color(0xFFC5221F)
    val RedTint = Color(0xFFFAD2CF)
    val Purple = Color(0xFFA142F4)
    val PurpleTint = Color(0xFFE9D2FD)
    val Green = Color(0xFF34A853)
    val GreenTint = Color(0xFFCEEAD6)
    val Blue = Color(0xFF4285F4)
    val Link = Color(0xFF1A73E8)

    val TrackIa = Color(0xFF4285F4)
    val TrackWeb = Color(0xFFA142F4)
    val TrackMobile = Color(0xFF34A853)
    val TrackCloud = Color(0xFFFBBC04)
    val TrackGeneral = Color(0xFF9AA0A6)
}

internal val DevFestColorScheme = lightColorScheme(
    primary = DevFestColors.Ink,
    onPrimary = DevFestColors.Card,
    secondary = DevFestColors.Sky,
    onSecondary = DevFestColors.Ink,
    background = DevFestColors.Background,
    onBackground = DevFestColors.Ink,
    surface = DevFestColors.Card,
    onSurface = DevFestColors.Ink,
    onSurfaceVariant = DevFestColors.Muted,
    outline = DevFestColors.Ink,
    error = DevFestColors.Red,
)
