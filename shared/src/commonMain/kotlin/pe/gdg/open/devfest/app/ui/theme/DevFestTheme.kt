package pe.gdg.open.devfest.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import pe.gdg.open.devfest.app.platform.rememberSystemReduceMotion

/**
 * Tema único de la app. No lee el modo oscuro del sistema: la app es solo clara
 * (constitución, Principio IV).
 */
@Composable
fun DevFestTheme(content: @Composable () -> Unit) {
    val fonts = rememberDevFestFonts()
    CompositionLocalProvider(
        LocalDevFestFonts provides fonts,
        LocalReduceMotion provides rememberSystemReduceMotion(),
    ) {
        MaterialTheme(
            colorScheme = DevFestColorScheme,
            typography = devFestTypography(fonts.outfit),
            shapes = Shapes(
                small = RoundedCornerShape(DevFestDimens.RadiusS),
                medium = RoundedCornerShape(DevFestDimens.RadiusM),
                large = RoundedCornerShape(DevFestDimens.RadiusL),
            ),
        ) {
            // Estilo base de los textos que solo fijan tamaño y peso: Outfit con el alto de línea
            // natural de su tamaño (el de bodyLarge separa demasiado los textos chicos).
            CompositionLocalProvider(
                LocalTextStyle provides TextStyle(fontFamily = fonts.outfit, color = DevFestColors.Ink),
                content = content,
            )
        }
    }
}
