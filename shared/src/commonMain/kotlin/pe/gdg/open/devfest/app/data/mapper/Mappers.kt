package pe.gdg.open.devfest.app.data.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import pe.gdg.open.devfest.app.data.api.ScanResponseDto
import pe.gdg.open.devfest.app.data.api.dto.AgendaDto
import pe.gdg.open.devfest.app.data.api.dto.EarnWayDto
import pe.gdg.open.devfest.app.data.api.dto.EventDto
import pe.gdg.open.devfest.app.data.api.dto.GemsInfoDto
import pe.gdg.open.devfest.app.data.api.dto.MeDto
import pe.gdg.open.devfest.app.data.api.dto.PrizeDto
import pe.gdg.open.devfest.app.data.api.dto.RankingDto
import pe.gdg.open.devfest.app.data.api.dto.RankingEntryDto
import pe.gdg.open.devfest.app.data.api.dto.ScanSourceDto
import pe.gdg.open.devfest.app.data.api.dto.SessionDto
import pe.gdg.open.devfest.app.data.api.dto.SpeakerDto
import pe.gdg.open.devfest.app.data.api.dto.TrackDto
import pe.gdg.open.devfest.app.domain.model.Agenda
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.Break
import pe.gdg.open.devfest.app.domain.model.DefaultTracks
import pe.gdg.open.devfest.app.domain.model.EarnWay
import pe.gdg.open.devfest.app.domain.model.Event
import pe.gdg.open.devfest.app.domain.model.GemSourceType
import pe.gdg.open.devfest.app.domain.model.GemsInfo
import pe.gdg.open.devfest.app.domain.model.Level
import pe.gdg.open.devfest.app.domain.model.Prize
import pe.gdg.open.devfest.app.domain.model.Ranking
import pe.gdg.open.devfest.app.domain.model.RankingEntry
import pe.gdg.open.devfest.app.domain.model.ScanResult
import pe.gdg.open.devfest.app.domain.model.ScanSource
import pe.gdg.open.devfest.app.domain.model.Session
import pe.gdg.open.devfest.app.domain.model.Speaker
import pe.gdg.open.devfest.app.domain.model.Talk
import pe.gdg.open.devfest.app.domain.model.Track
import pe.gdg.open.devfest.app.domain.model.TrackId
import pe.gdg.open.devfest.app.domain.model.User
import kotlin.time.Instant

// Mapeo DTO → dominio. Reglas: data-model.md.
// Un valor que el contrato no permite lanza IllegalArgumentException; safeApiCall lo
// convierte en DataError.Unknown.

fun EventDto.toDomain(): Event = Event(
    id = id,
    name = name,
    edition = edition,
    date = LocalDate.parse(date),
    city = city,
    timeZone = TimeZone.of(timezone),
    hashtag = hashtag,
)

/** Un track con id desconocido se descarta. */
fun TrackDto.toDomain(): Track? {
    val trackId = trackIdOf(id) ?: return null
    return Track(
        id = trackId,
        name = name,
        color = parseColor(color) ?: DefaultTracks.of(trackId).color,
    )
}

fun AgendaDto.toDomain(): Agenda {
    val tracksById = tracks.mapNotNull { it.toDomain() }.associateBy { it.id }
    return Agenda(
        updatedAt = Instant.parse(updatedAt),
        tracks = tracksById.values.toList(),
        // sortedBy es estable: a igual hora se conserva el orden del backend.
        sessions = sessions.mapNotNull { it.toDomain(tracksById) }.sortedBy { it.startsAt },
    )
}

/**
 * - `endsAt` DEBE ser posterior a `startsAt`; una sesión que no lo cumple se descarta.
 * - Una `talk` sin `trackId` o con un track desconocido se asigna a `GENERAL`.
 * - Una `talk` sin `level` usa `TODOS`; sin `room` muestra la sala vacía.
 * - `speakers` vacío significa "Ponente por confirmar".
 */
fun SessionDto.toDomain(tracksById: Map<TrackId, Track>): Session? {
    val start = runCatching { Instant.parse(startsAt) }.getOrNull() ?: return null
    val end = runCatching { Instant.parse(endsAt) }.getOrNull() ?: return null
    if (end <= start) return null
    return when (type) {
        "break" -> Break(id = id, title = title, startsAt = start, endsAt = end)
        "talk" -> {
            val trackId = trackIdOf(trackId) ?: TrackId.GENERAL
            Talk(
                id = id,
                title = title,
                startsAt = start,
                endsAt = end,
                room = room.orEmpty(),
                track = tracksById[trackId] ?: DefaultTracks.of(trackId),
                level = levelOf(level),
                description = description.orEmpty(),
                topics = topics,
                speakers = speakers.map { it.toDomain() },
                isLive = isLive,
            )
        }
        else -> null
    }
}

fun SpeakerDto.toDomain(): Speaker = Speaker(
    name = name,
    photoUrl = photoUrl,
    role = role,
    company = company,
)

fun MeDto.toDomain(): User = User(
    id = id,
    fullName = fullName,
    photoUrl = photoUrl,
    provider = providerOf(provider),
    gems = gems,
    rank = rank,
    totalParticipants = totalParticipants,
)

/** Una forma de ganar con tipo desconocido se descarta. */
fun GemsInfoDto.toDomain(): GemsInfo = GemsInfo(
    earnWays = earnWays.mapNotNull { it.toDomain() },
    prizes = prizes.map { it.toDomain() },
)

fun EarnWayDto.toDomain(): EarnWay? {
    val sourceType = gemSourceTypeOf(type) ?: return null
    return EarnWay(type = sourceType, title = title, description = description, gems = gems)
}

fun PrizeDto.toDomain(): Prize = Prize(
    id = id,
    title = title,
    description = description,
    forPositions = forPositions,
    imageUrl = imageUrl,
)

fun ScanSourceDto.toDomain(): ScanSource = ScanSource(
    type = requireNotNull(gemSourceTypeOf(type)) { "Unknown scan source type: $type" },
    name = name,
)

/** 200 → Awarded, 409 → AlreadyUsed, 422 → Invalid. */
fun ScanResponseDto.toDomain(): ScanResult = when (this) {
    is ScanResponseDto.Awarded -> ScanResult.Awarded(
        gemsAwarded = body.gemsAwarded,
        newBalance = body.newBalance,
        source = body.source.toDomain(),
    )
    is ScanResponseDto.AlreadyUsed -> ScanResult.AlreadyUsed(
        balance = body.balance,
        source = body.source?.toDomain(),
    )
    is ScanResponseDto.Invalid -> ScanResult.Invalid
}

/** `top` se conserva en el orden del backend, sin reordenar. */
fun RankingDto.toDomain(): Ranking = Ranking(
    updatedAt = Instant.parse(updatedAt),
    totalParticipants = totalParticipants,
    top = top.map { it.toDomain() },
    me = me.toDomain(),
)

fun RankingEntryDto.toDomain(): RankingEntry = RankingEntry(
    position = position,
    userId = userId,
    fullName = fullName,
    photoUrl = photoUrl,
    gems = gems,
    isMe = isMe,
)

internal fun trackIdOf(id: String?): TrackId? = when (id) {
    "ia" -> TrackId.IA
    "web" -> TrackId.WEB
    "mobile" -> TrackId.MOBILE
    "cloud" -> TrackId.CLOUD
    "general" -> TrackId.GENERAL
    else -> null
}

internal fun levelOf(level: String?): Level = when (level) {
    "Básico" -> Level.BASICO
    "Intermedio" -> Level.INTERMEDIO
    "Avanzado" -> Level.AVANZADO
    else -> Level.TODOS
}

internal fun providerOf(provider: String): AuthProvider = when (provider) {
    "google" -> AuthProvider.GOOGLE
    "apple" -> AuthProvider.APPLE
    "github" -> AuthProvider.GITHUB
    else -> throw IllegalArgumentException("Unknown provider: $provider")
}

internal fun gemSourceTypeOf(type: String): GemSourceType? = when (type) {
    "talk" -> GemSourceType.TALK
    "stand" -> GemSourceType.STAND
    "challenge" -> GemSourceType.CHALLENGE
    else -> null
}

/** `#RRGGBB` → ARGB opaco; `null` si el formato no es válido. */
internal fun parseColor(hex: String): Long? {
    if (hex.length != 7 || hex[0] != '#') return null
    val rgb = hex.substring(1).toLongOrNull(16) ?: return null
    return 0xFF000000 or rgb
}
