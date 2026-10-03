package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.cd_back

/**
 * Barra superior de las pantallas secundarias: "volver" a la izquierda, un contenido al centro
 * (por defecto el logo) y una acción opcional a la derecha.
 *
 */
@Composable
fun ScreenTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    center: @Composable () -> Unit = { DevFestLogo() },
    action: (@Composable () -> Unit)? = null) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScreenBackButton(onBack)
        Box(Modifier.weight(1f).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
            center()
        }
        if (action != null) action() else Box(Modifier.size(46.dp))
    }
}

/** El botón "volver" de [ScreenTopBar], suelto para poder fijarlo aparte del resto de la barra. */
@Composable
fun ScreenBackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    CircleIconButton(
        icon = AppIcons.Back,
        contentDescription = stringResource(Res.string.cd_back),
        onClick = onBack,
        modifier = modifier,
    )
}
