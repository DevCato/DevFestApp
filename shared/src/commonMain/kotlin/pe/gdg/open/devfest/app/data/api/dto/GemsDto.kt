package pe.gdg.open.devfest.app.data.api.dto

import kotlinx.serialization.Serializable

/** `EarnWay` de contracts/api.yaml. `type`: talk | stand | challenge. */
@Serializable
data class EarnWayDto(
    val type: String,
    val title: String,
    val description: String,
    val gems: Int,
)

/** `Prize` de contracts/api.yaml. */
@Serializable
data class PrizeDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val forPositions: String? = null,
    val imageUrl: String? = null,
)

/** `GemsInfo` de contracts/api.yaml. */
@Serializable
data class GemsInfoDto(
    val earnWays: List<EarnWayDto>,
    val prizes: List<PrizeDto>,
)

/** Cuerpo de `POST /scans`. */
@Serializable
data class ScanRequestDto(
    val code: String,
)

/** `ScanSource` de contracts/api.yaml. `type`: talk | stand | challenge. */
@Serializable
data class ScanSourceDto(
    val type: String,
    val name: String,
)

/** `ScanAwarded` de contracts/api.yaml (200). */
@Serializable
data class ScanAwardedDto(
    val result: String = "awarded",
    val gemsAwarded: Int,
    val newBalance: Int,
    val source: ScanSourceDto,
)

/** `ScanAlreadyUsed` de contracts/api.yaml (409). */
@Serializable
data class ScanAlreadyUsedDto(
    val result: String = "already_used",
    val balance: Int,
    val source: ScanSourceDto? = null,
)
