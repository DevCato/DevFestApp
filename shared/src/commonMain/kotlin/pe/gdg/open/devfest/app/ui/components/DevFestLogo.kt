package pe.gdg.open.devfest.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.app_name
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts

/** Logo tipográfico pequeño "{ DevFest Lima }" de las cabeceras. */
@Composable
fun DevFestLogo(modifier: Modifier = Modifier) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = DevFestColors.Blue)) { append("{ ") }
            append(stringResource(Res.string.app_name))
            withStyle(SpanStyle(color = DevFestColors.Yellow)) { append(" }") }
        },
        style = TextStyle(
            fontFamily = LocalDevFestFonts.current.outfit,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.011).em,
            color = DevFestColors.Ink,
        ),
        // Sin límite de líneas: con letra grande pasa a dos líneas en vez de cortarse.
        modifier = modifier,
    )
}
