package pe.gdg.open.devfest.app.domain.model

enum class TrackId { IA, WEB, MOBILE, CLOUD, GENERAL }

/** @property color color ARGB (0xFFRRGGBB). */
data class Track(
    val id: TrackId,
    val name: String,
    val color: Long,
)

/**
 * Los tracks son fijos (spec, Key Entities). Se usan cuando el backend no envía un track
 * que una charla referencia.
 */
object DefaultTracks {
    val IA = Track(TrackId.IA, "IA", 0xFF4285F4)
    val WEB = Track(TrackId.WEB, "Web", 0xFFA142F4)
    val MOBILE = Track(TrackId.MOBILE, "Mobile", 0xFF34A853)
    val CLOUD = Track(TrackId.CLOUD, "Cloud", 0xFFFBBC04)
    val GENERAL = Track(TrackId.GENERAL, "General", 0xFF9AA0A6)

    val all: List<Track> = listOf(IA, WEB, MOBILE, CLOUD, GENERAL)

    fun of(id: TrackId): Track = all.first { it.id == id }
}
