package pe.gdg.open.devfest.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.repository.AgendaRepository
import pe.gdg.open.devfest.app.domain.repository.AuthRepository
import pe.gdg.open.devfest.app.domain.repository.UserRepository
import pe.gdg.open.devfest.app.ui.modal.ModalRequest
import pe.gdg.open.devfest.app.ui.screens.agenda.savedCount

data class ProfileUiState(
    val fullName: String = "",
    val photoUrl: String? = null,
    val provider: AuthProvider? = null,
    val savedCount: Int = 0,
    val gems: Int? = null,
    val rank: Int? = null,
)

sealed interface ProfileEvent {
    data object LoggedOut : ProfileEvent
}

/** Pantalla 05 (US8). */
class ProfileViewModel(
    private val auth: AuthRepository,
    private val users: UserRepository,
    private val agenda: AgendaRepository,
) : ViewModel() {

    private val _modals = Channel<ModalRequest>(Channel.BUFFERED)
    val modals: Flow<ModalRequest> = _modals.receiveAsFlow()

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    val state: StateFlow<ProfileUiState> = combine(
        auth.session,
        users.user,
        agenda.agenda,
        agenda.savedTalkIds,
    ) { session, user, agenda, savedIds ->
        ProfileUiState(
            fullName = user?.fullName ?: session?.fullName.orEmpty(),
            photoUrl = user?.photoUrl ?: session?.photoUrl,
            provider = session?.provider ?: user?.provider,
            savedCount = savedCount(agenda, savedIds),
            gems = user?.gems,
            rank = user?.rank,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ProfileUiState())

    fun onEnter() {
        viewModelScope.launch { users.refresh() }
    }

    /** Modal "¿Cerrar sesión?" que avisa cuántas charlas guardadas se perderán (FR-007). */
    fun onLogoutClick() {
        viewModelScope.launch {
            _modals.send(ModalRequest.ConfirmLogout(savedTalks = state.value.savedCount, onConfirm = ::logout))
        }
    }

    private fun logout() {
        viewModelScope.launch {
            agenda.clearSaved()
            auth.signOut()
            _events.send(ProfileEvent.LoggedOut)
        }
    }
}
