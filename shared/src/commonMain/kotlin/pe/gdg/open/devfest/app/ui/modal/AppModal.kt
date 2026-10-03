package pe.gdg.open.devfest.app.ui.modal

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource
import pe.gdg.open.devfest.app.domain.model.GemSourceType
import pe.gdg.open.devfest.app.ui.theme.DevFestColors

/** Un botón del modal. El modal se cierra antes de ejecutar [onClick]. */
class ModalButton(val text: String, val onClick: () -> Unit)

/**
 * Configuración inmutable del único modal de la app (FR-065). Se crea con [Builder] donde se
 * necesita; no hay un modal por pantalla.
 */
class AppModal private constructor(
    val title: String,
    val message: String?,
    val icon: DrawableResource?,
    val iconBackground: Color,
    val iconTint: Color,
    val shadowColor: Color,
    val primaryButton: ModalButton,
    val secondaryButton: ModalButton?,
    val dismissOnOutsideTap: Boolean,
) {
    /** Si la tarjeta usa sombra de tinta, el botón principal usa sombra celeste (prototipo). */
    val primaryButtonShadowColor: Color
        get() = if (shadowColor == DevFestColors.Ink) DevFestColors.Sky else shadowColor

    class Builder {
        private var title: String? = null
        private var message: String? = null
        private var icon: DrawableResource? = null
        private var iconBackground: Color = DevFestColors.YellowTint
        private var iconTint: Color = DevFestColors.Ink
        private var shadowColor: Color = DevFestColors.Ink
        private var primaryButton: ModalButton? = null
        private var secondaryButton: ModalButton? = null
        private var dismissOnOutsideTap: Boolean = true

        fun title(title: String) = apply { this.title = title }
        fun message(message: String?) = apply { this.message = message }
        fun icon(icon: DrawableResource?) = apply { this.icon = icon }

        /** Fondo del ícono (girado -6°). */
        fun iconBackground(color: Color) = apply { iconBackground = color }
        fun iconTint(color: Color) = apply { iconTint = color }

        /** Sombra sólida de la tarjeta. */
        fun shadowColor(color: Color) = apply { shadowColor = color }

        fun primaryButton(text: String, onClick: () -> Unit) =
            apply { primaryButton = ModalButton(text, onClick) }

        /** Opcional: con él el modal tiene dos botones. Sin acción, solo cierra. */
        fun secondaryButton(text: String, onClick: () -> Unit = {}) =
            apply { secondaryButton = ModalButton(text, onClick) }

        fun dismissOnOutsideTap(dismiss: Boolean) = apply { dismissOnOutsideTap = dismiss }

        /** @throws IllegalArgumentException si falta el título o el botón principal. */
        fun build(): AppModal {
            val title = requireNotNull(title?.takeIf { it.isNotBlank() }) { "AppModal requires a title" }
            val primary = requireNotNull(primaryButton) { "AppModal requires a primary button" }
            return AppModal(
                title = title,
                message = message?.takeIf { it.isNotBlank() },
                icon = icon,
                iconBackground = iconBackground,
                iconTint = iconTint,
                shadowColor = shadowColor,
                primaryButton = primary,
                secondaryButton = secondaryButton,
                dismissOnOutsideTap = dismissOnOutsideTap,
            )
        }
    }
}

/**
 * Lo que un ViewModel pide mostrar, sin conocer la UI. [ModalHost] lo convierte en un
 * [AppModal] con los textos de `strings.xml` (ver `Modals`).
 */
sealed interface ModalRequest {
    class LoginFailed(val onRetry: () -> Unit, val onUseOtherAccount: () -> Unit) : ModalRequest
    class SessionExpired(val onSignIn: () -> Unit) : ModalRequest
    class ConfirmLogout(val savedTalks: Int, val onConfirm: () -> Unit) : ModalRequest
    class GemsAwarded(
        val gemsAwarded: Int,
        val newBalance: Int,
        val sourceType: GemSourceType,
        val sourceName: String,
        val onDone: () -> Unit,
        val onKeepScanning: () -> Unit,
    ) : ModalRequest
    class QrAlreadyUsed(val onDismiss: () -> Unit) : ModalRequest
    class QrInvalid(val onRetry: () -> Unit, val onCancel: () -> Unit) : ModalRequest
    class NoConnection(val onRetry: () -> Unit, val onClose: () -> Unit = {}) : ModalRequest
    class CameraPermission(val onOpenSettings: () -> Unit, val onNotNow: () -> Unit) : ModalRequest
    class UpdateFailed(val onRetry: () -> Unit) : ModalRequest
}
