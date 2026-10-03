package pe.gdg.open.devfest.app.data.api

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.gdg.open.devfest.app.data.api.dto.AgendaDto
import pe.gdg.open.devfest.app.data.api.dto.ErrorBodyDto
import pe.gdg.open.devfest.app.data.api.dto.ErrorDto
import pe.gdg.open.devfest.app.data.api.dto.EventDto
import pe.gdg.open.devfest.app.data.api.dto.GemsInfoDto
import pe.gdg.open.devfest.app.data.api.dto.MeDto
import pe.gdg.open.devfest.app.data.api.dto.RankingDto
import pe.gdg.open.devfest.app.data.api.dto.RankingEntryDto
import pe.gdg.open.devfest.app.data.api.dto.ScanAlreadyUsedDto
import pe.gdg.open.devfest.app.data.api.dto.ScanAwardedDto
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.AuthSession
import kotlin.time.Clock

/**
 * Backend simulado en memoria. Hace lo que haría el backend real: valida los QR, decide las
 * gemas, lleva el saldo y calcula el ranking.
 *
 * @param identity sesión actual; nombre, foto y proveedor de `/me` salen de aquí.
 */
class FakeDevFestApi(
    private val identity: () -> AuthSession?,
    private val clock: Clock = Clock.System,
    var scenario: FakeScenario = FakeScenario(),
) : DevFestApi {

    private val mutex = Mutex()
    private var balance = FakeData.INITIAL_BALANCE
    private val usedCodes = mutableSetOf<String>()

    override suspend fun getEvent(): EventDto {
        simulate()
        return FakeData.event
    }

    override suspend fun getAgenda(): AgendaDto {
        simulate()
        return FakeData.agenda(empty = scenario.emptyAgenda)
    }

    override suspend fun getMe(): MeDto {
        val session = simulateAuthenticated()
        return mutex.withLock {
            val ranked = rankedEntries(session)
            val me = ranked.first { it.isMe }
            MeDto(
                id = session.userId,
                fullName = session.fullName,
                photoUrl = session.photoUrl,
                provider = session.provider.wireName(),
                gems = balance,
                rank = me.position,
                totalParticipants = ranked.size,
            )
        }
    }

    override suspend fun getGemsInfo(): GemsInfoDto {
        simulate()
        return FakeData.gemsInfo
    }

    override suspend fun postScan(code: String): ScanResponseDto {
        simulateAuthenticated()
        return mutex.withLock {
            val qr = FakeData.qrCodes[code]
            when {
                qr == null -> ScanResponseDto.Invalid(
                    ErrorDto(ErrorBodyDto("invalid_code", "Este QR no es del DevFest")),
                )
                code in usedCodes -> ScanResponseDto.AlreadyUsed(
                    ScanAlreadyUsedDto(balance = balance, source = qr.source),
                )
                else -> {
                    usedCodes += code
                    balance += qr.gems
                    ScanResponseDto.Awarded(
                        ScanAwardedDto(gemsAwarded = qr.gems, newBalance = balance, source = qr.source),
                    )
                }
            }
        }
    }

    override suspend fun getRanking(limit: Int): RankingDto {
        val session = simulateAuthenticated()
        return mutex.withLock {
            val ranked = rankedEntries(session)
            RankingDto(
                updatedAt = clock.now().toString(),
                totalParticipants = ranked.size,
                top = ranked.take(limit.coerceIn(3, 50)),
                me = ranked.first { it.isMe },
            )
        }
    }

    /** Ordena por gemas; con empate, el usuario queda después (como decidiría el backend). */
    private fun rankedEntries(session: AuthSession): List<RankingEntryDto> {
        val others = FakeData.participants.mapIndexed { index, (name, gems) ->
            Triple("p${index + 1}", name, gems)
        }
        val all = others + Triple(session.userId, session.fullName, balance)
        return all
            .sortedByDescending { it.third }
            .mapIndexed { index, (id, name, gems) ->
                val isMe = id == session.userId
                RankingEntryDto(
                    position = index + 1,
                    userId = id,
                    fullName = name,
                    photoUrl = if (isMe) session.photoUrl else null,
                    gems = gems,
                    isMe = isMe,
                )
            }
    }

    private suspend fun simulate() {
        val current = scenario
        if (current.latency.isPositive()) delay(current.latency)
        if (current.networkFailure) throw ApiException.Network()
        if (current.sessionExpired) {
            throw ApiException.Unauthorized(ErrorDto(ErrorBodyDto("session_expired", "Tu sesión expiró")))
        }
    }

    private suspend fun simulateAuthenticated(): AuthSession {
        simulate()
        return identity()
            ?: throw ApiException.Unauthorized(ErrorDto(ErrorBodyDto("unauthenticated", "Sin sesión")))
    }

    private fun AuthProvider.wireName(): String = when (this) {
        AuthProvider.GOOGLE -> "google"
        AuthProvider.APPLE -> "apple"
        AuthProvider.GITHUB -> "github"
    }
}
