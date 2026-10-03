package pe.gdg.open.devfest.app.domain.model

import kotlin.time.Instant

data class RankingEntry(
    val position: Int,
    val userId: String,
    val fullName: String,
    val photoUrl: String?,
    val gems: Int,
    val isMe: Boolean,
)

data class Ranking(
    val updatedAt: Instant,
    val totalParticipants: Int,
    /** En el orden del backend (ya desempatado); la app no lo reordena. */
    val top: List<RankingEntry>,
    val me: RankingEntry,
)
