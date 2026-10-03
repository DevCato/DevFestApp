package pe.gdg.open.devfest.app.ui.screens.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.gdg.open.devfest.app.domain.logic.RankingView
import pe.gdg.open.devfest.app.domain.logic.buildRankingView
import pe.gdg.open.devfest.app.domain.repository.DataError
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.domain.repository.RankingRepository
import pe.gdg.open.devfest.app.ui.modal.ModalRequest

data class RankingUiState(
    /** `null` mientras carga por primera vez: esqueleto. */
    val view: RankingView? = null,
    /** Refresh en curso: el ícono gira, el botón se deshabilita y se lee "Actualizando…". */
    val refreshing: Boolean = false,
    /** Tras un refresh correcto: "Actualizado hace un momento". */
    val justRefreshed: Boolean = false,
)

/** Pantalla 07 (US7). */
class RankingViewModel(private val ranking: RankingRepository) : ViewModel() {

    private val _state = MutableStateFlow(RankingUiState())
    val state: StateFlow<RankingUiState> = _state.asStateFlow()

    private val _modals = Channel<ModalRequest>(Channel.BUFFERED)
    val modals: Flow<ModalRequest> = _modals.receiveAsFlow()

    private var loadJob: Job? = null

    /** Se pide el ranking al abrir la pantalla. */
    fun onEnter() = load(userRefresh = false)

    /** Botón de refresh (FR-052). Un toque mientras actualiza se ignora. */
    fun refresh() = load(userRefresh = true)

    private fun load(userRefresh: Boolean) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            if (userRefresh) _state.update { it.copy(refreshing = true) }
            val result = ranking.ranking()
            _state.update { current ->
                when (result) {
                    is Outcome.Success -> current.copy(
                        view = buildRankingView(result.value),
                        refreshing = false,
                        justRefreshed = userRefresh,
                    )
                    is Outcome.Failure -> current.copy(refreshing = false)
                }
            }
            if (result is Outcome.Failure && result.error != DataError.SessionExpired) {
                _modals.send(ModalRequest.UpdateFailed(onRetry = ::refresh))
            }
        }
    }
}
