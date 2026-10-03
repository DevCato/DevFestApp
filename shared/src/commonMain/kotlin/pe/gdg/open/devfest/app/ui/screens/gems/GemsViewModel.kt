package pe.gdg.open.devfest.app.ui.screens.gems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.gdg.open.devfest.app.domain.model.GemsInfo
import pe.gdg.open.devfest.app.domain.model.User
import pe.gdg.open.devfest.app.domain.repository.DataError
import pe.gdg.open.devfest.app.domain.repository.GemsRepository
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.domain.repository.UserRepository
import pe.gdg.open.devfest.app.ui.modal.ModalRequest

data class GemsUiState(
    val user: User? = null,
    val info: GemsInfo? = null,
    val loading: Boolean = false,
) {
    /** Esqueleto de la tarjeta de saldo mientras no hay usuario (FR-018). */
    val showBalanceSkeleton: Boolean get() = user == null
}

/** Pantalla 06 (US6). */
class GemsViewModel(
    private val gems: GemsRepository,
    private val users: UserRepository,
) : ViewModel() {

    private val info = MutableStateFlow<GemsInfo?>(null)
    private val loading = MutableStateFlow(false)
    private var loadJob: Job? = null

    private val _modals = Channel<ModalRequest>(Channel.BUFFERED)
    val modals: Flow<ModalRequest> = _modals.receiveAsFlow()

    val state: StateFlow<GemsUiState> = combine(users.user, info, loading) { user, info, loading ->
        GemsUiState(user = user, info = info, loading = loading)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, GemsUiState())

    fun onEnter() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            loading.value = true
            val userResult = users.refresh()
            val infoResult = gems.info()
            if (infoResult is Outcome.Success) info.value = infoResult.value
            loading.value = false
            val failure = listOf(userResult, infoResult).filterIsInstance<Outcome.Failure>().firstOrNull()
            if (failure != null && failure.error != DataError.SessionExpired) {
                _modals.send(ModalRequest.UpdateFailed(onRetry = ::onEnter))
            }
        }
    }
}
