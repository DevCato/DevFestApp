package pe.gdg.open.devfest.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.jetbrainsmono_medium
import pe.gdg.open.devfest.app.resources.jetbrainsmono_semibold
import pe.gdg.open.devfest.app.resources.outfit_bold
import pe.gdg.open.devfest.app.resources.outfit_extrabold
import pe.gdg.open.devfest.app.resources.outfit_medium
import pe.gdg.open.devfest.app.resources.outfit_regular
import pe.gdg.open.devfest.app.resources.outfit_semibold

@Immutable
data class DevFestFonts(
    /** Outfit (400–800): todo el texto. */
    val outfit: FontFamily,
    /** JetBrains Mono (500–600): horas, contadores y etiquetas técnicas. */
    val mono: FontFamily,
)

val LocalDevFestFonts = staticCompositionLocalOf { DevFestFonts(FontFamily.Default, FontFamily.Monospace) }

@Composable
internal fun rememberDevFestFonts(): DevFestFonts = DevFestFonts(
    outfit = FontFamily(
        Font(Res.font.outfit_regular, FontWeight.Normal),
        Font(Res.font.outfit_medium, FontWeight.Medium),
        Font(Res.font.outfit_semibold, FontWeight.SemiBold),
        Font(Res.font.outfit_bold, FontWeight.Bold),
        Font(Res.font.outfit_extrabold, FontWeight.ExtraBold),
    ),
    mono = FontFamily(
        Font(Res.font.jetbrainsmono_medium, FontWeight.Medium),
        Font(Res.font.jetbrainsmono_semibold, FontWeight.SemiBold),
    ),
)

/** Escala tipográfica del prototipo. Los tamaños en sp respetan la letra del sistema (FR-062). */
internal fun devFestTypography(outfit: FontFamily): Typography {
    fun style(size: Int, weight: FontWeight, lineHeight: Float = 1.25f, letterSpacing: Float = 0f) =
        TextStyle(
            fontFamily = outfit,
            fontWeight = weight,
            fontSize = size.sp,
            lineHeight = (size * lineHeight).sp,
            letterSpacing = letterSpacing.em,
            color = DevFestColors.Ink,
        )
    return Typography(
        displayLarge = style(48, FontWeight.ExtraBold, 1.0f, -0.033f),
        headlineLarge = style(34, FontWeight.ExtraBold, 1.1f, -0.03f),
        headlineMedium = style(28, FontWeight.ExtraBold, 1.15f, -0.02f),
        headlineSmall = style(22, FontWeight.ExtraBold, 1.2f, -0.014f),
        titleLarge = style(20, FontWeight.ExtraBold, 1.25f),
        titleMedium = style(18, FontWeight.Bold, 1.3f),
        titleSmall = style(16, FontWeight.Bold, 1.3f),
        bodyLarge = style(16, FontWeight.Normal, 1.65f),
        bodyMedium = style(15, FontWeight.Normal, 1.5f),
        bodySmall = style(13, FontWeight.Normal, 1.45f),
        labelLarge = style(16, FontWeight.Bold, 1.2f),
        labelMedium = style(14, FontWeight.SemiBold, 1.2f),
        labelSmall = style(12, FontWeight.SemiBold, 1.2f),
    )
}
