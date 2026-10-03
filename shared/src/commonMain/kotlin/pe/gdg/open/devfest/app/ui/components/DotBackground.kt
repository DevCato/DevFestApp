package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import pe.gdg.open.devfest.app.ui.theme.DevFestColors

private val DotColor = DevFestColors.Ink.copy(alpha = 0.09f)

/** Fondo `#F1F1F1` con patrón de puntos cada 22 dp (prototipo). */
fun Modifier.dotBackground(): Modifier = background(DevFestColors.Background).drawBehind {
    val step = 22.dp.toPx()
    val radius = 1.2.dp.toPx()
    var y = step / 2
    while (y < size.height) {
        var x = step / 2
        while (x < size.width) {
            drawCircle(DotColor, radius, Offset(x, y))
            x += step
        }
        y += step
    }
}

@Composable
fun DotBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.dotBackground(), content = content)
}
