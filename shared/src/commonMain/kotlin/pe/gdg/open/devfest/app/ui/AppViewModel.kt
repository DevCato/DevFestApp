package pe.gdg.open.devfest.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pe.gdg.open.devfest.app.data.SessionEvents
import pe.gdg.open.devfest.app.domain.repository.AuthRepository
import pe.gdg.open.devfest.app.ui.modal.ModalRequest

sealed interface AppEvent {
    data object NavigateToLogin : AppEvent
}

/**
 * Sesión vencida (spec, Edge Cases): cuando un repositorio recibe 401, muestra "Tu sesión
 * expiró" una sola vez; al aceptar cierra la sesión y vuelve a Login. Las charlas guardadas se
 * conservan: solo el cierre de sesión voluntario las borra.
 */
class AppViewModel(
    sessionEvents: SessionEvents,
    private val auth: AuthRepository,
) : ViewModel() {

    private val _modals = Channel<ModalRequest>(Channel.BUFFERED)
    val modals: Flow<ModalRequest> = _modals.receiveAsFlow()

    private val _events = Channel<AppEvent>(Channel.BUFFERED)
    val events: Flow<AppEvent> = _events.receiveAsFlow()

    private var expiryShown = false

    init {
        viewModelScope.launch {
            sessionEvents.expired.collect {
                if (!expiryShown) {
                    expiryShown = true
                    _modals.send(ModalRequest.SessionExpired(onSignIn = ::signInAgain))
                }
            }
        }
    }

    private fun signInAgain() {
        viewModelScope.launch {
            auth.signOut()
            expiryShown = false
            _events.send(AppEvent.NavigateToLogin)
        }
    }
}
