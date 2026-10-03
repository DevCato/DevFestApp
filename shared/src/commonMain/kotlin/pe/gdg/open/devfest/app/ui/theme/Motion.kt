package pe.gdg.open.devfest.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing

// Curvas del prototipo: ease-out fuerte para UI, ease-in-out para movimiento en pantalla.
// Nunca ease-in.

/** Presión de botones, modales y transiciones. */
val EaseOutStrong = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

/** Movimiento en pantalla (punto que late, línea del escáner). */
val EaseInOutStrong = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)

/** Flotación de los stickers. */
val EaseFloat = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)

/** Contadores de gemas (easeOutExpo). */
val EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
