package pe.gdg.open.devfest.app.ui.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/**
 * Alto de la barra de navegación del sistema (Android) o del indicador de inicio (iOS).
 * La app es edge to edge: el contenido desplazable pasa por detrás y suma este espacio al
 * final para que lo último no quede tapado.
 */
@Composable
fun navigationBarsBottom(): Dp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
