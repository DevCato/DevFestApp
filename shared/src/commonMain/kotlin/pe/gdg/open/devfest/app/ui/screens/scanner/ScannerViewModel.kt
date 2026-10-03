package pe.gdg.open.devfest.app.ui.screens.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.gdg.open.devfest.app.domain.model.ScanResult
import pe.gdg.open.devfest.app.domain.repository.DataError
import pe.gdg.open.devfest.app.domain.repository.GemsRepository
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.ui.modal.ModalRequest

data class ScannerUiState(
    /** Hay un código enviándose al backend. */
    val validating: Boolean = false,
    /** Se mostró un resultado: no se leen más códigos hasta que el usuario siga. */
    val paused: Boolean = false,
) {
    val acceptingCodes: Boolean get() = !validating && !paused
}

sealed interface ScannerEvent {
    data object Close : ScannerEvent
    data object OpenSettings : ScannerEvent
}

/**
 * Escáner de QR (US5). Cada código se valida en el backend; la app muestra exactamente las
 * gemas y el saldo que devuelve (FR-041, FR-042). Mientras valida o muestra un resultado ignora
 * nuevas lecturas, así un QR frente a la cámara no se envía dos veces.
 */
class ScannerViewModel(private val gems: GemsRepository) : ViewModel() {

    private val _state = MutableStateFlow(ScannerUiState())
    val state: StateFlow<ScannerUiState> = _state.asStateFlow()

    private val _modals = Channel<ModalRequest>(Channel.BUFFERED)
    val modals: Flow<ModalRequest> = _modals.receiveAsFlow()

    private val _events = Channel<ScannerEvent>(Channel.BUFFERED)
    val events: Flow<ScannerEvent> = _events.receiveAsFlow()

    fun onCodeScanned(code: String) {
        if (!_state.value.acceptingCodes) return
        _state.value = ScannerUiState(validating = true)
        viewModelScope.launch {
            val result = gems.scan(code)
            _state.value = ScannerUiState(paused = true)
            val modal = when (result) {
                is Outcome.Success -> when (val scan = result.value) {
                    is ScanResult.Awarded -> ModalRequest.GemsAwarded(
                        gemsAwarded = scan.gemsAwarded,
                        newBalance = scan.newBalance,
                        sourceType = scan.source.type,
                        sourceName = scan.source.name,
                        onDone = ::close,
                        onKeepScanning = ::resume,
                    )
                    is ScanResult.AlreadyUsed -> ModalRequest.QrAlreadyUsed(onDismiss = ::resume)
                    ScanResult.Invalid -> ModalRequest.QrInvalid(onRetry = ::resume, onCancel = ::close)
                }
                // La app muestra "Tu sesión expiró" y vuelve a Login.
                is Outcome.Failure -> if (result.error == DataError.SessionExpired) {
                    null
                } else {
                    ModalRequest.NoConnection(onRetry = { retry(code) }, onClose = ::resume)
                }
            }
            modal?.let { _modals.send(it) }
        }
    }

    /** Sin permiso de cámara: "Activa la cámara" con "Abrir configuración" y "Ahora no". */
    fun onPermissionDenied() {
        viewModelScope.launch {
            _modals.send(
                ModalRequest.CameraPermission(
                    onOpenSettings = { sendEvent(ScannerEvent.OpenSettings) },
                    onNotNow = ::close,
                ),
            )
        }
    }

    fun resume() {
        _state.update { it.copy(paused = false) }
    }

    private fun retry(code: String) {
        resume()
        onCodeScanned(code)
    }

    private fun close() = sendEvent(ScannerEvent.Close)

    private fun sendEvent(event: ScannerEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}
