package pe.gdg.open.devfest.app.domain.repository

import pe.gdg.open.devfest.app.domain.model.Ranking

interface RankingRepository {
    /** Conserva el orden del backend. */
    suspend fun ranking(): Outcome<Ranking>
}
