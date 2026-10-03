package pe.gdg.open.devfest.app.data.api.dto

import kotlinx.serialization.Serializable

/** `Track` de contracts/api.yaml. `id`: ia | web | mobile | cloud | general. */
@Serializable
data class TrackDto(
    val id: String,
    val name: String,
    /** `#RRGGBB`. */
    val color: String,
)

/** `Speaker` de contracts/api.yaml. */
@Serializable
data class SpeakerDto(
    val name: String,
    val photoUrl: String? = null,
    val role: String? = null,
    val company: String? = null,
)

/** `Session` de contracts/api.yaml. */
@Serializable
data class SessionDto(
    val id: String,
    /** talk | break. */
    val type: String,
    val title: String,
    /** ISO 8601 con offset de Lima. */
    val startsAt: String,
    val endsAt: String,
    val room: String? = null,
    val trackId: String? = null,
    /** Básico | Intermedio | Avanzado | Todos. */
    val level: String? = null,
    val description: String? = null,
    val topics: List<String> = emptyList(),
    /** Vacío = "Ponente por confirmar". */
    val speakers: List<SpeakerDto> = emptyList(),
    val isLive: Boolean = false,
)

/** `Agenda` de contracts/api.yaml. */
@Serializable
data class AgendaDto(
    val updatedAt: String,
    val tracks: List<TrackDto>,
    val sessions: List<SessionDto>,
)
