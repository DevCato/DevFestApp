package pe.gdg.open.devfest.app.data.api.dto

import kotlinx.serialization.Serializable

/** `RankingEntry` de contracts/api.yaml. */
@Serializable
data class RankingEntryDto(
    val position: Int,
    val userId: String,
    val fullName: String,
    val photoUrl: String? = null,
    val gems: Int,
    val isMe: Boolean,
)

/** `Ranking` de contracts/api.yaml. */
@Serializable
data class RankingDto(
    val updatedAt: String,
    val totalParticipants: Int,
    /** Ordenado por posición (ya desempatado por el backend). */
    val top: List<RankingEntryDto>,
    val me: RankingEntryDto,
)
