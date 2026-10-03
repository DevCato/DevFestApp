package pe.gdg.open.devfest.app.platform

import androidx.compose.runtime.Composable

/**
 * Preferencia del sistema "reducir movimiento", observada mientras la app está en pantalla.
 * Android: escala de animaciones en 0. iOS: Reducir movimiento.
 */
@Composable
expect fun rememberSystemReduceMotion(): Boolean
