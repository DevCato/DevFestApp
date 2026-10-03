package pe.gdg.open.devfest.app.data.api.dto

import kotlinx.serialization.Serializable

/** `Me` de contracts/api.yaml. `provider`: google | apple | github. */
@Serializable
data class MeDto(
    val id: String,
    val fullName: String,
    val photoUrl: String? = null,
    val provider: String,
    val gems: Int,
    val rank: Int,
    val totalParticipants: Int,
)
