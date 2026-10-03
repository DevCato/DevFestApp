package pe.gdg.open.devfest.app.ui.modal

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.GemSourceType
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.action_cancel
import pe.gdg.open.devfest.app.resources.action_close
import pe.gdg.open.devfest.app.resources.action_retry
import pe.gdg.open.devfest.app.resources.action_try_again
import pe.gdg.open.devfest.app.resources.modal_account_exists_primary
import pe.gdg.open.devfest.app.resources.modal_account_exists_primary_unknown
import pe.gdg.open.devfest.app.resources.modal_account_exists_text
import pe.gdg.open.devfest.app.resources.modal_account_exists_text_unknown
import pe.gdg.open.devfest.app.resources.modal_account_exists_title
import pe.gdg.open.devfest.app.resources.modal_account_exists_title_unknown
import pe.gdg.open.devfest.app.resources.modal_camera_primary
import pe.gdg.open.devfest.app.resources.modal_camera_secondary
import pe.gdg.open.devfest.app.resources.modal_camera_text
import pe.gdg.open.devfest.app.resources.modal_camera_title
import pe.gdg.open.devfest.app.resources.modal_gems_awarded_primary
import pe.gdg.open.devfest.app.resources.modal_gems_awarded_secondary
import pe.gdg.open.devfest.app.resources.modal_gems_awarded_text_challenge
import pe.gdg.open.devfest.app.resources.modal_gems_awarded_text_stand
import pe.gdg.open.devfest.app.resources.modal_gems_awarded_text_talk
import pe.gdg.open.devfest.app.resources.modal_gems_awarded_title
import pe.gdg.open.devfest.app.resources.modal_login_failed_primary
import pe.gdg.open.devfest.app.resources.modal_login_failed_secondary
import pe.gdg.open.devfest.app.resources.modal_login_failed_text
import pe.gdg.open.devfest.app.resources.modal_login_failed_title
import pe.gdg.open.devfest.app.resources.modal_logout_primary
import pe.gdg.open.devfest.app.resources.modal_logout_text_empty
import pe.gdg.open.devfest.app.resources.modal_logout_text_saved
import pe.gdg.open.devfest.app.resources.modal_logout_title
import pe.gdg.open.devfest.app.resources.modal_offline_text
import pe.gdg.open.devfest.app.resources.modal_offline_title
import pe.gdg.open.devfest.app.resources.modal_qr_invalid_text
import pe.gdg.open.devfest.app.resources.modal_qr_invalid_title
import pe.gdg.open.devfest.app.resources.modal_qr_used_primary
import pe.gdg.open.devfest.app.resources.modal_qr_used_text
import pe.gdg.open.devfest.app.resources.modal_qr_used_title
import pe.gdg.open.devfest.app.resources.modal_session_expired_primary
import pe.gdg.open.devfest.app.resources.modal_session_expired_text
import pe.gdg.open.devfest.app.resources.modal_session_expired_title
import pe.gdg.open.devfest.app.resources.modal_update_failed_text
import pe.gdg.open.devfest.app.resources.modal_update_failed_title
import pe.gdg.open.devfest.app.resources.provider_apple
import pe.gdg.open.devfest.app.resources.provider_github
import pe.gdg.open.devfest.app.resources.provider_google
import pe.gdg.open.devfest.app.ui.components.AppIcons
import pe.gdg.open.devfest.app.ui.theme.DevFestColors

/**
 * Los casos del catálogo (FR-066, pantalla 08) más el de cuenta existente, todos construidos con [AppModal.Builder].
 * Íconos y colores del prototipo.
 */
object Modals {

    @Composable
    fun loginFailed(onRetry: () -> Unit, onUseOtherAccount: () -> Unit): AppModal = AppModal.Builder()
        .title(stringResource(Res.string.modal_login_failed_title))
        .message(stringResource(Res.string.modal_login_failed_text))
        .icon(AppIcons.Lock)
        .iconBackground(DevFestColors.RedTint)
        .iconTint(DevFestColors.Red)
        .shadowColor(DevFestColors.Ink)
        .primaryButton(stringResource(Res.string.modal_login_failed_primary), onRetry)
        .secondaryButton(stringResource(Res.string.modal_login_failed_secondary), onUseOtherAccount)
        .build()

    /**
     * El email ya tiene cuenta con otro proveedor. Si se sabe cuál, el botón principal entra con
     * ese; si no, solo se avisa.
     */
    @Composable
    fun accountExists(existingProvider: AuthProvider?, onSignInWithExisting: () -> Unit): AppModal {
        val builder = AppModal.Builder()
            .icon(AppIcons.Lock)
            .iconBackground(DevFestColors.SkyTint)
            .iconTint(DevFestColors.Ink)
            .shadowColor(DevFestColors.Sky)
        if (existingProvider == null) {
            return builder
                .title(stringResource(Res.string.modal_account_exists_title_unknown))
                .message(stringResource(Res.string.modal_account_exists_text_unknown))
                .primaryButton(stringResource(Res.string.modal_account_exists_primary_unknown)) {}
                .build()
        }
        val name = stringResource(
            when (existingProvider) {
                AuthProvider.GOOGLE -> Res.string.provider_google
                AuthProvider.APPLE -> Res.string.provider_apple
                AuthProvider.GITHUB -> Res.string.provider_github
            },
        )
        return builder
            .title(stringResource(Res.string.modal_account_exists_title, name))
            .message(stringResource(Res.string.modal_account_exists_text, name))
            .primaryButton(stringResource(Res.string.modal_account_exists_primary, name), onSignInWithExisting)
            .secondaryButton(stringResource(Res.string.action_close))
            .build()
    }

    /** No se puede cerrar tocando fuera: la única salida es volver a iniciar sesión. */
    @Composable
    fun sessionExpired(onSignIn: () -> Unit): AppModal = AppModal.Builder()
        .title(stringResource(Res.string.modal_session_expired_title))
        .message(stringResource(Res.string.modal_session_expired_text))
        .icon(AppIcons.Clock)
        .iconBackground(DevFestColors.YellowTint)
        .iconTint(DevFestColors.Ink)
        .shadowColor(DevFestColors.Yellow)
        .primaryButton(stringResource(Res.string.modal_session_expired_primary), onSignIn)
        .dismissOnOutsideTap(false)
        .build()

    @Composable
    fun confirmLogout(savedTalks: Int, onConfirm: () -> Unit): AppModal = AppModal.Builder()
        .title(stringResource(Res.string.modal_logout_title))
        .message(
            if (savedTalks > 0) {
                pluralStringResource(Res.plurals.modal_logout_text_saved, savedTalks, savedTalks)
            } else {
                stringResource(Res.string.modal_logout_text_empty)
            },
        )
        .icon(AppIcons.Logout)
        .iconBackground(DevFestColors.YellowTint)
        .iconTint(DevFestColors.Ink)
        .shadowColor(DevFestColors.Yellow)
        .primaryButton(stringResource(Res.string.modal_logout_primary), onConfirm)
        .secondaryButton(stringResource(Res.string.action_cancel))
        .build()

    /** Muestra exactamente la cantidad y el saldo que devolvió el backend. */
    @Composable
    fun gemsAwarded(
        gemsAwarded: Int,
        newBalance: Int,
        sourceType: GemSourceType,
        sourceName: String,
        onDone: () -> Unit,
        onKeepScanning: () -> Unit,
    ): AppModal {
        val text = when (sourceType) {
            GemSourceType.TALK -> Res.string.modal_gems_awarded_text_talk
            GemSourceType.STAND -> Res.string.modal_gems_awarded_text_stand
            GemSourceType.CHALLENGE -> Res.string.modal_gems_awarded_text_challenge
        }
        return AppModal.Builder()
            .title(pluralStringResource(Res.plurals.modal_gems_awarded_title, gemsAwarded, gemsAwarded))
            .message(stringResource(text, sourceName, newBalance))
            .icon(AppIcons.Gem)
            .iconBackground(DevFestColors.YellowTint)
            .iconTint(DevFestColors.Ink)
            .shadowColor(DevFestColors.Yellow)
            .primaryButton(stringResource(Res.string.modal_gems_awarded_primary), onDone)
            .secondaryButton(stringResource(Res.string.modal_gems_awarded_secondary), onKeepScanning)
            .dismissOnOutsideTap(false)
            .build()
    }

    @Composable
    fun qrAlreadyUsed(onDismiss: () -> Unit): AppModal = AppModal.Builder()
        .title(stringResource(Res.string.modal_qr_used_title))
        .message(stringResource(Res.string.modal_qr_used_text))
        .icon(AppIcons.CheckCircle)
        .iconBackground(DevFestColors.SkyTint)
        .iconTint(DevFestColors.Ink)
        .shadowColor(DevFestColors.Sky)
        .primaryButton(stringResource(Res.string.modal_qr_used_primary), onDismiss)
        .dismissOnOutsideTap(false)
        .build()

    @Composable
    fun qrInvalid(onRetry: () -> Unit, onCancel: () -> Unit): AppModal = AppModal.Builder()
        .title(stringResource(Res.string.modal_qr_invalid_title))
        .message(stringResource(Res.string.modal_qr_invalid_text))
        .icon(AppIcons.QrInvalid)
        .iconBackground(DevFestColors.RedTint)
        .iconTint(DevFestColors.Red)
        .shadowColor(DevFestColors.Ink)
        .primaryButton(stringResource(Res.string.action_try_again), onRetry)
        .secondaryButton(stringResource(Res.string.action_cancel), onCancel)
        .dismissOnOutsideTap(false)
        .build()

    @Composable
    fun noConnection(onRetry: () -> Unit, onClose: () -> Unit = {}): AppModal = AppModal.Builder()
        .title(stringResource(Res.string.modal_offline_title))
        .message(stringResource(Res.string.modal_offline_text))
        .icon(AppIcons.WifiOff)
        .iconBackground(DevFestColors.PurpleTint)
        .iconTint(DevFestColors.Ink)
        .shadowColor(DevFestColors.Purple)
        .primaryButton(stringResource(Res.string.action_retry), onRetry)
        .secondaryButton(stringResource(Res.string.action_close), onClose)
        .dismissOnOutsideTap(false)
        .build()

    @Composable
    fun cameraPermission(onOpenSettings: () -> Unit, onNotNow: () -> Unit): AppModal = AppModal.Builder()
        .title(stringResource(Res.string.modal_camera_title))
        .message(stringResource(Res.string.modal_camera_text))
        .icon(AppIcons.Camera)
        .iconBackground(DevFestColors.GreenTint)
        .iconTint(DevFestColors.Ink)
        .shadowColor(DevFestColors.Green)
        .primaryButton(stringResource(Res.string.modal_camera_primary), onOpenSettings)
        .secondaryButton(stringResource(Res.string.modal_camera_secondary), onNotNow)
        .dismissOnOutsideTap(false)
        .build()

    @Composable
    fun updateFailed(onRetry: () -> Unit): AppModal = AppModal.Builder()
        .title(stringResource(Res.string.modal_update_failed_title))
        .message(stringResource(Res.string.modal_update_failed_text))
        .icon(AppIcons.Refresh)
        .iconBackground(DevFestColors.PurpleTint)
        .iconTint(DevFestColors.Ink)
        .shadowColor(DevFestColors.Purple)
        .primaryButton(stringResource(Res.string.action_try_again), onRetry)
        .secondaryButton(stringResource(Res.string.action_close))
        .build()
}

/** Convierte lo que pidió un ViewModel en el modal correspondiente. */
@Composable
fun ModalRequest.toAppModal(): AppModal = when (this) {
    is ModalRequest.LoginFailed -> Modals.loginFailed(onRetry, onUseOtherAccount)
    is ModalRequest.AccountExists -> Modals.accountExists(existingProvider, onSignInWithExisting)
    is ModalRequest.SessionExpired -> Modals.sessionExpired(onSignIn)
    is ModalRequest.ConfirmLogout -> Modals.confirmLogout(savedTalks, onConfirm)
    is ModalRequest.GemsAwarded ->
        Modals.gemsAwarded(gemsAwarded, newBalance, sourceType, sourceName, onDone, onKeepScanning)
    is ModalRequest.QrAlreadyUsed -> Modals.qrAlreadyUsed(onDismiss)
    is ModalRequest.QrInvalid -> Modals.qrInvalid(onRetry, onCancel)
    is ModalRequest.NoConnection -> Modals.noConnection(onRetry, onClose)
    is ModalRequest.CameraPermission -> Modals.cameraPermission(onOpenSettings, onNotNow)
    is ModalRequest.UpdateFailed -> Modals.updateFailed(onRetry)
}
