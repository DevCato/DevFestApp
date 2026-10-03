package pe.gdg.open.devfest.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pe.gdg.open.devfest.app.ui.theme.EaseOutStrong
import pe.gdg.open.devfest.app.ui.theme.LocalReduceMotion

private const val PRESS_MILLIS = 120

/**
 * Progreso del "hundimiento" al presionar: 0 = en reposo, 1 = hundido sobre la sombra.
 *
 * Un toque rápido siempre se ve entero: al soltar, primero termina de hundirse y luego vuelve.
 * Sin esto, dentro de un contenedor con scroll (donde Compose retrasa el aviso de "presionado"
 * para distinguirlo de un scroll) la presión y la suelta llegan casi juntas y no se ve nada.
 * Con "reducir movimiento" se queda en 0.
 */
@Composable
internal fun rememberPressProgress(interactionSource: InteractionSource, enabled: Boolean): State<Float> {
    val reduceMotion = LocalReduceMotion.current
    val progress = remember { Animatable(0f) }
    LaunchedEffect(interactionSource, enabled, reduceMotion) {
        if (!enabled || reduceMotion) {
            progress.snapTo(0f)
            return@LaunchedEffect
        }
        var pressJob: Job? = null
        var releaseJob: Job? = null
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    releaseJob?.cancel()
                    pressJob = launch { progress.animateTo(1f, tween(PRESS_MILLIS, easing = EaseOutStrong)) }
                }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    val press = pressJob
                    releaseJob = launch {
                        press?.join()
                        progress.animateTo(0f, tween(PRESS_MILLIS, easing = EaseOutStrong))
                    }
                }
            }
        }
    }
    return progress.asState()
}
