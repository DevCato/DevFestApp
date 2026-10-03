package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sombra sólida desplazada hacia abajo a la derecha, sin desenfoque (estilo del prototipo).
 *
 * La sombra es parte del tamaño del elemento: reserva [reserve] a la derecha y abajo y se dibuja
 * dentro de ese espacio. Así, una capa con opacidad (fundidos de entrada, botones
 * deshabilitados, sesiones pasadas) la incluye; si la sombra quedara fuera de los límites, esa
 * capa la recortaría y aparecería de golpe al terminar el fundido.
 *
 * Orden en la cadena:
 * - Las capas con opacidad (`riseIn`, `graphicsLayer { alpha }`) van **antes**, para envolver
 *   también el espacio de la sombra.
 * - El tamaño visible (`size`, `height`, `heightIn`) va **después**: es el de la tarjeta, sin la
 *   sombra. `fillMaxWidth` puede ir antes: la sombra entra en el ancho disponible.
 * - `clip`, `background` y `border` van después.
 *
 * @param offset desplazamiento dibujado; puede animarse (presión de botones) sin pasar de [reserve].
 * @param reserve espacio fijo reservado; no se anima, para que el layout no salte.
 */
fun Modifier.solidShadow(
    color: Color,
    offset: Dp,
    shape: Shape,
    reserve: Dp = offset,
): Modifier {
    if (reserve <= 0.dp) return this
    return drawBehind {
        val drawn = offset.coerceIn(0.dp, reserve).toPx()
        if (drawn <= 0f) return@drawBehind
        val reserved = reserve.toPx()
        val inner = Size(size.width - reserved, size.height - reserved)
        val outline = shape.createOutline(inner, layoutDirection, this)
        translate(drawn, drawn) { drawOutline(outline, color) }
    }.absolutePadding(right = reserve, bottom = reserve)
}
