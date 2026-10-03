package pe.gdg.open.devfest.app.ui.modal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.painterResource
import pe.gdg.open.devfest.app.ui.components.AppButton
import pe.gdg.open.devfest.app.ui.components.AppButtonStyle
import pe.gdg.open.devfest.app.ui.components.solidShadow
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.EaseOutStrong
import pe.gdg.open.devfest.app.ui.theme.LocalReduceMotion

/**
 * Cola de modales de la app: se muestra uno a la vez, en orden de llegada. Hay una sola
 * instancia, en la raíz ([LocalModalController]).
 */
@Stable
class ModalController {
    private sealed interface Content {
        class Request(val request: ModalRequest) : Content
        class Custom(val modal: AppModal) : Content
    }

    private class Entry(val id: Long, val content: Content)

    private val queue = mutableStateListOf<Entry>()
    private var nextId = 0L

    val isShowing: Boolean get() = queue.isNotEmpty()

    /**
     * Encola un modal. Si ya hay uno del mismo tipo visible o en cola, se ignora: un doble toque
     * (p. ej. en "Cerrar sesión") no abre el mismo modal dos veces.
     */
    fun show(request: ModalRequest) {
        val duplicate = queue.any { (it.content as? Content.Request)?.request?.let { queued -> queued::class == request::class } == true }
        if (duplicate) return
        queue += Entry(nextId++, Content.Request(request))
    }

    fun show(modal: AppModal) {
        queue += Entry(nextId++, Content.Custom(modal))
    }

    /** Cierra el modal visible; si hay otro en cola, aparece el siguiente. */
    fun dismiss() {
        if (queue.isNotEmpty()) queue.removeAt(0)
    }

    @Composable
    internal fun CurrentModal() {
        val entry = queue.firstOrNull() ?: return
        key(entry.id) {
            val modal = when (val content = entry.content) {
                is Content.Request -> content.request.toAppModal()
                is Content.Custom -> content.modal
            }
            ModalDialog(modal = modal, onDismiss = ::dismiss)
        }
    }
}

val LocalModalController = staticCompositionLocalOf<ModalController> {
    error("ModalController not provided: wrap the UI in ModalHost")
}

/** Muestra en el host los modales que pide un ViewModel. */
@Composable
fun ModalRequestEffect(requests: Flow<ModalRequest>) {
    val controller = LocalModalController.current
    LaunchedEffect(requests, controller) {
        requests.collect { controller.show(it) }
    }
}

/** Monta el único host de modales. Va en la raíz de la app. */
@Composable
fun ModalHost(
    controller: ModalController = remember { ModalController() },
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalModalController provides controller) {
        content()
        controller.CurrentModal()
    }
}

/**
 * El modal genérico (pantalla 08 del prototipo). Aparece con fundido del fondo y escala
 * 0.95 → 1 en 220 ms; con "reducir movimiento", solo fundido (FR-067). El diálogo de la
 * plataforma lo anuncia como tal a los lectores de pantalla.
 */
@Composable
private fun ModalDialog(modal: AppModal, onDismiss: () -> Unit) {
    val reduceMotion = LocalReduceMotion.current
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 220, easing = EaseOutStrong))
    }

    Dialog(
        onDismissRequest = { if (modal.dismissOnOutsideTap) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = modal.dismissOnOutsideTap,
            dismissOnClickOutside = modal.dismissOnOutsideTap,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .graphicsLayer {
                    alpha = progress.value
                    val scale = if (reduceMotion) 1f else 0.95f + 0.05f * progress.value
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            ModalCard(
                modal = modal,
                onAction = { action ->
                    onDismiss()
                    action()
                },
            )
        }
    }
}

@Composable
private fun ModalCard(modal: AppModal, onAction: (() -> Unit) -> Unit) {
    val shape = RoundedCornerShape(DevFestDimens.RadiusL)
    Column(
        modifier = Modifier
            .widthIn(max = 342.dp)
            .fillMaxWidth()
            .semantics { paneTitle = modal.title }
            .solidShadow(modal.shadowColor, DevFestDimens.ShadowL, shape)
            .clip(shape)
            .background(DevFestColors.Card)
            .border(DevFestDimens.Border, DevFestColors.Ink, shape)
            .verticalScroll(rememberScrollState())
            .padding(start = 22.dp, end = 22.dp, top = 28.dp, bottom = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        modal.icon?.let { icon ->
            val iconShape = RoundedCornerShape(DevFestDimens.RadiusIcon)
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .rotate(-6f)
                    .solidShadow(DevFestColors.Ink, DevFestDimens.ShadowS, iconShape)
                    .size(72.dp)
                    .clip(iconShape)
                    .background(modal.iconBackground)
                    .border(DevFestDimens.Border, DevFestColors.Ink, iconShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = modal.iconTint,
                    modifier = Modifier.size(34.dp),
                )
            }
        }
        Text(
            text = modal.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        modal.message?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = DevFestColors.Muted,
                textAlign = TextAlign.Center,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppButton(
                text = modal.primaryButton.text,
                onClick = { onAction(modal.primaryButton.onClick) },
                modifier = Modifier.fillMaxWidth(),
                shadowColor = modal.primaryButtonShadowColor,
            )
            modal.secondaryButton?.let { secondary ->
                AppButton(
                    text = secondary.text,
                    onClick = { onAction(secondary.onClick) },
                    modifier = Modifier.fillMaxWidth(),
                    style = AppButtonStyle.Secondary,
                )
            }
        }
    }
}
