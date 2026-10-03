package pe.gdg.open.devfest.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Vista de la cámara trasera que detecta códigos QR (research.md, R7).
 * Android: CameraX + ML Kit. iOS: AVFoundation. Puede llamar a [onCodeScanned] muchas veces
 * seguidas con el mismo código: quien la usa decide cuándo aceptarlo.
 */
@Composable
expect fun QrScannerView(onCodeScanned: (String) -> Unit, modifier: Modifier = Modifier)

/** Permiso de cámara del sistema. */
interface CameraPermissionRequester {
    fun isGranted(): Boolean

    /** Pide el permiso; el resultado llega por el callback de [rememberCameraPermissionRequester]. */
    fun request()
}

@Composable
expect fun rememberCameraPermissionRequester(onResult: (granted: Boolean) -> Unit): CameraPermissionRequester

/** Abre los ajustes de la app en el sistema (modal "Activa la cámara"). */
@Composable
expect fun rememberOpenAppSettings(): () -> Unit
