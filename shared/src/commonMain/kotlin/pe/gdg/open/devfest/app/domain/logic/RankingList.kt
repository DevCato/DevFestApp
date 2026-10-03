package pe.gdg.open.devfest.app.domain.logic

import pe.gdg.open.devfest.app.domain.model.Ranking
import pe.gdg.open.devfest.app.domain.model.RankingEntry
import kotlin.time.Instant

sealed interface RankingListItem {
    data class Row(val entry: RankingEntry) : RankingListItem

    /** Fila "• • •" entre el puesto 10 y el del usuario. */
    data object Gap : RankingListItem
}

data class RankingView(
    /** Puestos 1–3, en orden de posición. */
    val podium: List<RankingEntry>,
    /** Puestos 4–10 y, si el usuario está más abajo, "• • •" y su fila. */
    val list: List<RankingListItem>,
    val me: RankingEntry,
    val totalParticipants: Int,
    val updatedAt: Instant,
)

/**
 * Arma el Ranking (FR-051, data-model.md "Lista del Ranking"). El orden es el del backend: aquí
 * no se reordena nada.
 * - Si `me.position > 10`, se añade la fila propia al final; si `> 11`, precedida de "• • •".
 * - La entrada con `isMe` se resalta donde aparezca.
 */
fun buildRankingView(ranking: Ranking): RankingView {
    val podium = ranking.top.filter { it.position in 1..3 }
    val rows = ranking.top.filter { it.position in 4..10 }.map { RankingListItem.Row(it) }
    val tail = when {
        ranking.me.position <= 10 -> emptyList()
        ranking.me.position == 11 -> listOf(RankingListItem.Row(ranking.me))
        else -> listOf(RankingListItem.Gap, RankingListItem.Row(ranking.me))
    }
    return RankingView(
        podium = podium,
        list = rows + tail,
        me = ranking.me,
        totalParticipants = ranking.totalParticipants,
        updatedAt = ranking.updatedAt,
    )
}
