package pe.gdg.open.devfest.app.ui.theme

import androidx.compose.runtime.compositionLocalOf

/**
 * `true` si el sistema pide reducir el movimiento. Toda animación DEBE leerlo y usar su variante
 * reducida: sin desplazamientos, bucles ni contadores; se conservan los fundidos (FR-060).
 */
val LocalReduceMotion = compositionLocalOf { false }
