package pe.gdg.open.devfest.app.ui.screens.talk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.gdg.open.devfest.app.domain.logic.TalkPosition
import pe.gdg.open.devfest.app.domain.logic.talkPosition
import pe.gdg.open.devfest.app.domain.model.Talk
import pe.gdg.open.devfest.app.domain.repository.AgendaRepository

data class TalkDetailUiState(
    val talk: Talk? = null,
    val position: TalkPosition? = null,
    val saved: Boolean = false,
    /** La agenda está cargada pero la charla ya no está publicada. */
    val notFound: Boolean = false,
) {
    val speakerToBeConfirmed: Boolean get() = talk?.speakers?.isEmpty() == true
}

/** Pantalla 04 (US4). Lee de la agenda guardada: funciona sin conexión. */
class TalkDetailViewModel(
    private val talkId: String,
    private val agenda: AgendaRepository,
) : ViewModel() {

    val state: StateFlow<TalkDetailUiState> = combine(agenda.agenda, agenda.savedTalkIds) { agenda, savedIds ->
        val talk = agenda?.sessions?.filterIsInstance<Talk>()?.firstOrNull { it.id == talkId }
        TalkDetailUiState(
            talk = talk,
            position = agenda?.let { talkPosition(it.sessions, talkId) },
            saved = talkId in savedIds,
            notFound = agenda != null && talk == null,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TalkDetailUiState())

    fun toggleSaved() {
        viewModelScope.launch { agenda.toggleSaved(talkId) }
    }
}
