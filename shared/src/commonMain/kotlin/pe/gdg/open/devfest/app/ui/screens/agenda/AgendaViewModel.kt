package pe.gdg.open.devfest.app.ui.screens.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.gdg.open.devfest.app.domain.logic.AgendaFilter
import pe.gdg.open.devfest.app.domain.logic.AgendaRow
import pe.gdg.open.devfest.app.domain.logic.buildAgendaRows
import pe.gdg.open.devfest.app.domain.model.Agenda
import pe.gdg.open.devfest.app.domain.model.Talk
import pe.gdg.open.devfest.app.domain.model.User
import pe.gdg.open.devfest.app.domain.repository.AgendaRepository
import pe.gdg.open.devfest.app.domain.repository.DataError
import pe.gdg.open.devfest.app.domain.repository.EventRepository
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.domain.repository.UserRepository
import pe.gdg.open.devfest.app.ui.modal.ModalRequest
import pe.gdg.open.devfest.app.ui.util.NowSource

enum class AgendaStatus {
    /** Primera carga sin agenda guardada: esqueletos. */
    SKELETON,

    /** Sin agenda guardada y sin conexión: estado "Sin conexión" (pantalla 02c). */
    OFFLINE,

    /** No hay charlas publicadas: "Aún no hay charlas", sin filtros. */
    NO_TALKS,

    CONTENT,
}

data class AgendaUiState(
    val status: AgendaStatus = AgendaStatus.SKELETON,
    val filter: AgendaFilter = AgendaFilter.ALL,
    val rows: List<AgendaRow> = emptyList(),
    val savedIds: Set<String> = emptySet(),
    val savedCount: Int = 0,
    val user: User? = null,
    /** Pull-to-refresh en curso. */
    val refreshing: Boolean = false,
    /** "Intentar de nuevo" en curso: el botón muestra "Conectando…". */
    val retrying: Boolean = false,
) {
    val showFilters: Boolean get() = status == AgendaStatus.CONTENT

    /** Filtro de track sin charlas: "Nada en este track". */
    val trackEmpty: Boolean get() = status == AgendaStatus.CONTENT && rows.isEmpty()
}

/** Pantalla 02 (US2) y guardado de charlas desde la lista (US3). */
class AgendaViewModel(
    private val agenda: AgendaRepository,
    private val events: EventRepository,
    private val users: UserRepository,
    now: NowSource,
) : ViewModel() {

    private data class LoadState(
        val filter: AgendaFilter = AgendaFilter.ALL,
        val failedWithoutCache: Boolean = false,
        val refreshing: Boolean = false,
        val retrying: Boolean = false,
    )

    private val load = MutableStateFlow(LoadState())
    private var refreshJob: Job? = null

    private val _modals = Channel<ModalRequest>(Channel.BUFFERED)
    val modals: Flow<ModalRequest> = _modals.receiveAsFlow()

    val state: StateFlow<AgendaUiState> = combine(
        agenda.agenda,
        agenda.savedTalkIds,
        events.event,
        users.user,
        combine(load, now.ticks()) { load, instant -> load to instant },
    ) { agenda, savedIds, event, user, (load, now) ->
        AgendaUiState(
            status = statusOf(agenda, load),
            filter = load.filter,
            rows = agenda?.let { buildAgendaRows(it.sessions, load.filter, now, event?.date) }.orEmpty(),
            savedIds = savedIds,
            savedCount = savedCount(agenda, savedIds),
            user = user,
            refreshing = load.refreshing,
            retrying = load.retrying,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AgendaUiState())

    /** Al entrar a la pantalla se vuelve a pedir la agenda (FR-017). */
    fun onEnter() = reload(Trigger.ENTER)

    /** Pull-to-refresh. */
    fun refresh() = reload(Trigger.PULL)

    /** "Intentar de nuevo" del estado "Sin conexión". */
    fun retry() = reload(Trigger.RETRY)

    fun selectFilter(filter: AgendaFilter) {
        load.update { it.copy(filter = filter) }
    }

    fun toggleSaved(talkId: String) {
        viewModelScope.launch { agenda.toggleSaved(talkId) }
    }

    private enum class Trigger { ENTER, PULL, RETRY }

    /** Una recarga a la vez: los toques repetidos se ignoran. */
    private fun reload(trigger: Trigger) {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            load.update {
                it.copy(refreshing = trigger == Trigger.PULL, retrying = trigger == Trigger.RETRY)
            }
            val hadCache = agenda.agenda.first() != null
            val result = agenda.refresh()
            // Cabecera (gemas, foto) y evento: sus fallos no interrumpen la agenda.
            launch { users.refresh() }
            launch { events.refresh() }
            load.update {
                it.copy(
                    failedWithoutCache = result is Outcome.Failure && !hadCache,
                    refreshing = false,
                    retrying = false,
                )
            }
            if (result is Outcome.Failure && hadCache && result.error != DataError.SessionExpired) {
                _modals.send(ModalRequest.UpdateFailed(onRetry = ::refresh))
            }
        }
    }

    private fun statusOf(agenda: Agenda?, load: LoadState): AgendaStatus = when {
        agenda == null -> if (load.failedWithoutCache) AgendaStatus.OFFLINE else AgendaStatus.SKELETON
        agenda.sessions.none { it is Talk } -> AgendaStatus.NO_TALKS
        else -> AgendaStatus.CONTENT
    }
}

/** Contador de la pestaña "Mi agenda": solo charlas que siguen en la agenda publicada. */
internal fun savedCount(agenda: Agenda?, savedIds: Set<String>): Int =
    agenda?.sessions?.count { it is Talk && it.id in savedIds } ?: savedIds.size
