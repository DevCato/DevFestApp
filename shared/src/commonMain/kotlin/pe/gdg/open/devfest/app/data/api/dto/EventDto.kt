package pe.gdg.open.devfest.app.data.api.dto

import kotlinx.serialization.Serializable

/** `Event` de contracts/api.yaml. */
@Serializable
data class EventDto(
    val id: String,
    val name: String,
    val edition: String,
    val year: Int? = null,
    /** Fecha ISO 8601 (`2026-11-21`). */
    val date: String,
    val city: String,
    val timezone: String,
    val hashtag: String,
)
