package pe.gdg.open.devfest.app.data.repository

import pe.gdg.open.devfest.app.data.SessionEvents
import pe.gdg.open.devfest.app.data.api.DevFestApi
import pe.gdg.open.devfest.app.data.api.safeApiCall
import pe.gdg.open.devfest.app.data.mapper.toDomain
import pe.gdg.open.devfest.app.domain.model.GemsInfo
import pe.gdg.open.devfest.app.domain.model.ScanResult
import pe.gdg.open.devfest.app.domain.repository.GemsRepository
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.domain.repository.UserRepository

/**
 * Gemas. La app nunca calcula cantidades ni saldos: muestra lo que devuelve el backend
 * (FR-041, SC-005).
 */
class GemsRepositoryImpl(
    private val api: DevFestApi,
    private val users: UserRepository,
    private val sessionEvents: SessionEvents,
) : GemsRepository {

    override suspend fun info(): Outcome<GemsInfo> =
        safeApiCall { api.getGemsInfo().toDomain() }.reportSessionExpiry(sessionEvents)

    override suspend fun scan(code: String): Outcome<ScanResult> {
        val result = safeApiCall { api.postScan(code).toDomain() }.reportSessionExpiry(sessionEvents)
        if (result is Outcome.Success) {
            when (val scan = result.value) {
                is ScanResult.Awarded -> {
                    users.setBalance(scan.newBalance)
                    // La posición en el ranking también la decide el backend.
                    users.refresh()
                }
                is ScanResult.AlreadyUsed -> users.setBalance(scan.balance)
                ScanResult.Invalid -> Unit
            }
        }
        return result
    }
}
