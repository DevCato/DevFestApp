package pe.gdg.open.devfest.app.data.api.dto

import kotlinx.serialization.Serializable

/** `Error` de contracts/api.yaml. */
@Serializable
data class ErrorDto(
    val error: ErrorBodyDto,
)

/**
 * `code`: unauthenticated | session_expired | invalid_code | already_used | not_found | internal.
 */
@Serializable
data class ErrorBodyDto(
    val code: String,
    val message: String,
)
