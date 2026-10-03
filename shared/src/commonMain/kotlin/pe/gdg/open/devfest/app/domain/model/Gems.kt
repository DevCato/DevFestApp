package pe.gdg.open.devfest.app.domain.model

/** Origen de gemas: una charla, un stand o un reto. */
enum class GemSourceType { TALK, STAND, CHALLENGE }

/** Forma de ganar gemas (informativa; la cantidad la decide el backend). */
data class EarnWay(
    val type: GemSourceType,
    val title: String,
    val description: String,
    val gems: Int,
)

/** Premio de los primeros puestos. No tiene precio en gemas. */
data class Prize(
    val id: String,
    val title: String,
    val description: String?,
    val forPositions: String?,
    val imageUrl: String?,
)

data class GemsInfo(
    val earnWays: List<EarnWay>,
    val prizes: List<Prize>,
)

data class ScanSource(
    val type: GemSourceType,
    val name: String,
)

/** Resultado de validar un QR. La app muestra los valores tal cual; nunca suma. */
sealed interface ScanResult {
    data class Awarded(
        val gemsAwarded: Int,
        val newBalance: Int,
        val source: ScanSource,
    ) : ScanResult

    data class AlreadyUsed(
        val balance: Int,
        val source: ScanSource?,
    ) : ScanResult

    data object Invalid : ScanResult
}
