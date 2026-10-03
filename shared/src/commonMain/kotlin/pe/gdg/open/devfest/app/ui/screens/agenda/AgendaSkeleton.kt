package pe.gdg.open.devfest.app.ui.screens.agenda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import pe.gdg.open.devfest.app.ui.components.SkeletonBlock

/** Esqueleto de la lista de charlas (FR-018): hora y tarjeta con la forma del contenido. */
@Composable
fun AgendaSkeleton(modifier: Modifier = Modifier, rows: Int = 4) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        repeat(rows) { index ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.width(50.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)).padding(top = 20.dp)) {
                    SkeletonBlock(Modifier.width(40.dp).height(14.dp), RoundedCornerShape(6.dp))
                }
                SkeletonBlock(
                    modifier = Modifier.weight(1f).height(if (index % 2 == 0) 118.dp else 96.dp),
                    shape = RoundedCornerShape(22.dp),
                )
            }
        }
    }
}
