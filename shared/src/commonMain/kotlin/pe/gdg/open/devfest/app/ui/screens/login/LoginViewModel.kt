package pe.gdg.open.devfest.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.repository.AuthRepository
import pe.gdg.open.devfest.app.domain.repository.SignInOutcome
import pe.gdg.open.devfest.app.ui.modal.ModalRequest

data class LoginUiState(
    /** Proveedor con el inicio de sesión en curso; `null` si no hay ninguno. */
    val connecting: AuthProvider? = null,
) {
    val busy: Boolean get() = connecting != null

    /** El botón tocado muestra "Conectando…". */
    fun isConnecting(provider: AuthProvider): Boolean = connecting == provider

    /** Mientras hay un inicio de sesión en curso, los demás botones se deshabilitan. */
    fun isEnabled(provider: AuthProvider): Boolean = connecting == null || connecting == provider
}

sealed interface LoginEvent {
    data object SignedIn : LoginEvent
}

class LoginViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    private val _modals = Channel<ModalRequest>(Channel.BUFFERED)
    val modals: Flow<ModalRequest> = _modals.receiveAsFlow()

    /** Un segundo toque mientras hay un inicio de sesión en curso se ignora. */
    fun signIn(provider: AuthProvider) {
        if (_state.value.busy) return
        _state.value = LoginUiState(connecting = provider)
        viewModelScope.launch {
            val result = auth.signIn(provider)
            _state.value = LoginUiState()
            when (result) {
                is SignInOutcome.Success -> _events.send(LoginEvent.SignedIn)
                is SignInOutcome.AccountExists -> _modals.send(
                    ModalRequest.AccountExists(
                        existingProvider = result.existingProvider,
                        onSignInWithExisting = { result.existingProvider?.let(::signIn) },
                    ),
                )
                SignInOutcome.Failed -> _modals.send(
                    ModalRequest.LoginFailed(
                        onRetry = { signIn(provider) },
                        onUseOtherAccount = ::useOtherAccount,
                    ),
                )
            }
        }
    }

    /** Cierra la cuenta que quedó en el proveedor para que el usuario elija otra. */
    private fun useOtherAccount() {
        viewModelScope.launch { auth.signOut() }
    }
}
