package pe.gdg.open.devfest.app.data.repository

import pe.gdg.open.devfest.app.data.SessionEvents
import pe.gdg.open.devfest.app.data.api.DevFestApi
import pe.gdg.open.devfest.app.data.api.safeApiCall
import pe.gdg.open.devfest.app.data.mapper.toDomain
import pe.gdg.open.devfest.app.domain.model.Ranking
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.domain.repository.RankingRepository

/** Quién aparece, el orden y el desempate los decide el backend (FR-050). */
class RankingRepositoryImpl(
    private val api: DevFestApi,
    private val sessionEvents: SessionEvents,
) : RankingRepository {

    override suspend fun ranking(): Outcome<Ranking> =
        safeApiCall { api.getRanking().toDomain() }.reportSessionExpiry(sessionEvents)
}
