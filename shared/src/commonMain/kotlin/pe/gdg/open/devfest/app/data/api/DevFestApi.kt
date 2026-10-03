package pe.gdg.open.devfest.app.data.api

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import pe.gdg.open.devfest.app.data.api.dto.AgendaDto
import pe.gdg.open.devfest.app.data.api.dto.ErrorDto
import pe.gdg.open.devfest.app.data.api.dto.EventDto
import pe.gdg.open.devfest.app.data.api.dto.GemsInfoDto
import pe.gdg.open.devfest.app.data.api.dto.MeDto
import pe.gdg.open.devfest.app.data.api.dto.RankingDto
import pe.gdg.open.devfest.app.data.api.dto.ScanAlreadyUsedDto
import pe.gdg.open.devfest.app.data.api.dto.ScanAwardedDto
import pe.gdg.open.devfest.app.domain.repository.DataError
import pe.gdg.open.devfest.app.domain.repository.Outcome

/**
 * Fuente de datos remota: una operación por `operationId` de contracts/api.yaml.
 *
 * Hoy la implementa [FakeDevFestApi]; con backend, `KtorDevFestApi`. Si el backend real
 * difiere del contrato, la adaptación se hace dentro de esa implementación.
 *
 * Las operaciones lanzan [ApiException] ante fallos de transporte, 401 o estados inesperados.
 */
interface DevFestApi {
    /** `GET /event` */
    suspend fun getEvent(): EventDto

    /** `GET /agenda` */
    suspend fun getAgenda(): AgendaDto

    /** `GET /me` */
    suspend fun getMe(): MeDto

    /** `GET /gems/info` */
    suspend fun getGemsInfo(): GemsInfoDto

    /** `POST /scans`: 200 → [ScanResponseDto.Awarded], 409 → AlreadyUsed, 422 → Invalid. */
    suspend fun postScan(code: String): ScanResponseDto

    /** `GET /ranking` */
    suspend fun getRanking(limit: Int = 10): RankingDto
}

/** Respuestas esperadas de `POST /scans`. */
sealed interface ScanResponseDto {
    data class Awarded(val body: ScanAwardedDto) : ScanResponseDto
    data class AlreadyUsed(val body: ScanAlreadyUsedDto) : ScanResponseDto
    data class Invalid(val body: ErrorDto) : ScanResponseDto
}

sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** Fallo de transporte (sin conexión, timeout). */
    class Network(cause: Throwable? = null) : ApiException("Network error", cause)

    /** 401: token ausente, inválido o vencido. */
    class Unauthorized(val error: ErrorDto? = null) : ApiException("Unauthorized")

    /** Cualquier estado que el contrato no contempla para la operación. */
    class UnexpectedStatus(val status: Int, val error: ErrorDto? = null) :
        ApiException("Unexpected status $status")
}

/** Configuración de JSON compartida por el cliente y la caché local. */
val DevFestJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

/**
 * Ejecuta una llamada y traduce sus errores a [DataError] (contracts/repositories.md,
 * "Reglas comunes"). Los errores de mapeo o de formato cuentan como [DataError.Unknown].
 */
suspend fun <T> safeApiCall(block: suspend () -> T): Outcome<T> =
    try {
        Outcome.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: ApiException.Network) {
        Outcome.Failure(DataError.Offline)
    } catch (e: ApiException.Unauthorized) {
        Outcome.Failure(DataError.SessionExpired)
    } catch (e: Exception) {
        Outcome.Failure(DataError.Unknown)
    }
