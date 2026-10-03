package pe.gdg.open.devfest.app.ui.screens.myagenda

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
import kotlinx.coroutines.launch
import pe.gdg.open.devfest.app.domain.logic.FeaturedTalk
import pe.gdg.open.devfest.app.domain.logic.conflictingTalkIds
import pe.gdg.open.devfest.app.domain.logic.featuredTalk
import pe.gdg.open.devfest.app.domain.logic.isPast
import pe.gdg.open.devfest.app.domain.model.Talk
import pe.gdg.open.devfest.app.domain.model.User
import pe.gdg.open.devfest.app.domain.repository.AgendaRepository
import pe.gdg.open.devfest.app.domain.repository.DataError
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.domain.repository.UserRepository
import pe.gdg.open.devfest.app.ui.modal.ModalRequest
import pe.gdg.open.devfest.app.ui.util.NowSource

data class MyAgendaItem(
    val talk: Talk,
    val showTime: Boolean,
    val isPast: Boolean,
    val conflict: Boolean,
)

data class MyAgendaUiState(
    val items: List<MyAgendaItem> = emptyList(),
    val featured: FeaturedTalk? = null,
    val user: User? = null,
    /** Primera carga sin agenda guardada. */
    val loading: Boolean = false,
) {
    val savedCount: Int get() = items.size
    val isEmpty: Boolean get() = items.isEmpty() && !loading
}

/** Pantalla 03 (US3). */
class MyAgendaViewModel(
    private val agenda: AgendaRepository,
    private val users: UserRepository,
    now: NowSource,
) : ViewModel() {

    private val loading = MutableStateFlow(false)
    private var refreshJob: Job? = null

    private val _modals = Channel<ModalRequest>(Channel.BUFFERED)
    val modals: Flow<ModalRequest> = _modals.receiveAsFlow()

    val state: StateFlow<MyAgendaUiState> = combine(
        agenda.agenda,
        agenda.savedTalkIds,
        users.user,
        loading,
        now.ticks(),
    ) { agenda, savedIds, user, loading, now ->
        // Solo las charlas que siguen publicadas: una eliminada desaparece sin error.
        val saved = agenda?.sessions.orEmpty().filterIsInstance<Talk>().filter { it.id in savedIds }
        val conflicts = conflictingTalkIds(saved)
        var previousStart: kotlin.time.Instant? = null
        MyAgendaUiState(
            items = saved.map { talk ->
                val showTime = talk.startsAt != previousStart
                previousStart = talk.startsAt
                MyAgendaItem(talk, showTime, isPast(talk, now), talk.id in conflicts)
            },
            featured = featuredTalk(saved, now),
            user = user,
            loading = loading && agenda == null,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MyAgendaUiState(loading = true))

    /** Al entrar a Mi agenda también se recarga la agenda (FR-017). */
    fun onEnter() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            loading.value = true
            val hadCache = agenda.agenda.first() != null
            val result = agenda.refresh()
            launch { users.refresh() }
            loading.value = false
            if (result is Outcome.Failure && hadCache && result.error != DataError.SessionExpired) {
                _modals.send(ModalRequest.UpdateFailed(onRetry = ::onEnter))
            }
        }
    }

    fun toggleSaved(talkId: String) {
        viewModelScope.launch { agenda.toggleSaved(talkId) }
    }
}
