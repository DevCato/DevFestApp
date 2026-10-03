package pe.gdg.open.devfest.app.data.mapper

import pe.gdg.open.devfest.app.data.api.ScanResponseDto
import pe.gdg.open.devfest.app.data.api.dto.AgendaDto
import pe.gdg.open.devfest.app.data.api.dto.ErrorBodyDto
import pe.gdg.open.devfest.app.data.api.dto.ErrorDto
import pe.gdg.open.devfest.app.data.api.dto.EventDto
import pe.gdg.open.devfest.app.data.api.dto.MeDto
import pe.gdg.open.devfest.app.data.api.dto.RankingDto
import pe.gdg.open.devfest.app.data.api.dto.RankingEntryDto
import pe.gdg.open.devfest.app.data.api.dto.ScanAlreadyUsedDto
import pe.gdg.open.devfest.app.data.api.dto.ScanAwardedDto
import pe.gdg.open.devfest.app.data.api.dto.ScanSourceDto
import pe.gdg.open.devfest.app.data.api.dto.SessionDto
import pe.gdg.open.devfest.app.data.api.dto.SpeakerDto
import pe.gdg.open.devfest.app.data.api.dto.TrackDto
import pe.gdg.open.devfest.app.domain.model.AuthProvider
import pe.gdg.open.devfest.app.domain.model.Break
import pe.gdg.open.devfest.app.domain.model.GemSourceType
import pe.gdg.open.devfest.app.domain.model.Level
import pe.gdg.open.devfest.app.domain.model.ScanResult
import pe.gdg.open.devfest.app.domain.model.ScanSource
import pe.gdg.open.devfest.app.domain.model.Talk
import pe.gdg.open.devfest.app.domain.model.TrackId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class MappersTest {

    private fun talk(
        id: String = "t1",
        startsAt: String = "2026-11-21T09:30:00-05:00",
        endsAt: String = "2026-11-21T10:15:00-05:00",
        trackId: String? = "ia",
        level: String? = "Intermedio",
        room: String? = "Sala 1",
        speakers: List<SpeakerDto> = emptyList(),
    ) = SessionDto(
        id = id,
        type = "talk",
        title = "Charla $id",
        startsAt = startsAt,
        endsAt = endsAt,
        room = room,
        trackId = trackId,
        level = level,
        speakers = speakers,
    )

    private fun agenda(vararg sessions: SessionDto) = AgendaDto(
        updatedAt = "2026-11-20T18:00:00-05:00",
        tracks = listOf(
            TrackDto("ia", "IA", "#4285F4"),
            TrackDto("general", "General", "#9AA0A6"),
        ),
        sessions = sessions.toList(),
    )

    @Test
    fun sessionWhoseEndIsNotAfterStartIsDiscarded() {
        val result = agenda(
            talk(id = "same", endsAt = "2026-11-21T09:30:00-05:00"),
            talk(id = "before", endsAt = "2026-11-21T09:00:00-05:00"),
            talk(id = "ok"),
        ).toDomain()

        assertEquals(listOf("ok"), result.sessions.map { it.id })
    }

    @Test
    fun sessionWithUnparseableTimeIsDiscarded() {
        val result = agenda(talk(id = "bad", startsAt = "mañana"), talk(id = "ok")).toDomain()

        assertEquals(listOf("ok"), result.sessions.map { it.id })
    }

    @Test
    fun talkWithoutTrackIdIsAssignedToGeneral() {
        val result = agenda(talk(trackId = null)).toDomain()

        assertEquals(TrackId.GENERAL, (result.sessions.single() as Talk).track.id)
    }

    @Test
    fun talkWithUnknownTrackIdIsAssignedToGeneral() {
        val result = agenda(talk(trackId = "blockchain")).toDomain()

        assertEquals(TrackId.GENERAL, (result.sessions.single() as Talk).track.id)
    }

    @Test
    fun talkReferencingTrackMissingFromResponseUsesFixedTrack() {
        val result = agenda(talk(trackId = "mobile")).toDomain()

        val track = (result.sessions.single() as Talk).track
        assertEquals(TrackId.MOBILE, track.id)
        assertEquals(0xFF34A853, track.color)
    }

    @Test
    fun talkWithoutLevelUsesTodos() {
        val result = agenda(talk(level = null)).toDomain()

        assertEquals(Level.TODOS, (result.sessions.single() as Talk).level)
    }

    @Test
    fun levelsAreMappedFromSpanishLabels() {
        val levels = listOf("Básico", "Intermedio", "Avanzado", "Todos").map { levelOf(it) }

        assertEquals(listOf(Level.BASICO, Level.INTERMEDIO, Level.AVANZADO, Level.TODOS), levels)
    }

    @Test
    fun talkWithoutRoomHasEmptyRoom() {
        val result = agenda(talk(room = null)).toDomain()

        assertEquals("", (result.sessions.single() as Talk).room)
    }

    @Test
    fun emptySpeakersMeansSpeakerToBeConfirmed() {
        val result = agenda(talk(speakers = emptyList())).toDomain()

        assertTrue((result.sessions.single() as Talk).speakers.isEmpty())
    }

    @Test
    fun speakersAreMapped() {
        val speaker = SpeakerDto("Ana Pérez", null, "Staff Engineer", "Empresa")
        val result = agenda(talk(speakers = listOf(speaker))).toDomain()

        val mapped = (result.sessions.single() as Talk).speakers.single()
        assertEquals("Ana Pérez", mapped.name)
        assertEquals("Staff Engineer", mapped.role)
        assertEquals("Empresa", mapped.company)
        assertNull(mapped.photoUrl)
    }

    @Test
    fun breaksAreMappedAndUnknownTypesDiscarded() {
        val breakDto = SessionDto(
            id = "b1",
            type = "break",
            title = "Registro y desayuno",
            startsAt = "2026-11-21T08:30:00-05:00",
            endsAt = "2026-11-21T09:30:00-05:00",
        )
        val unknown = talk(id = "x").copy(type = "workshop")

        val result = agenda(breakDto, unknown).toDomain()

        assertIs<Break>(result.sessions.single())
    }

    @Test
    fun sessionsAreSortedByStartKeepingBackendOrderForTies() {
        val result = agenda(
            talk(id = "late", startsAt = "2026-11-21T11:30:00-05:00", endsAt = "2026-11-21T12:15:00-05:00"),
            talk(id = "tieA", startsAt = "2026-11-21T10:30:00-05:00", endsAt = "2026-11-21T11:15:00-05:00"),
            talk(id = "tieB", startsAt = "2026-11-21T10:30:00-05:00", endsAt = "2026-11-21T11:15:00-05:00"),
            talk(id = "early"),
        ).toDomain()

        assertEquals(listOf("early", "tieA", "tieB", "late"), result.sessions.map { it.id })
    }

    @Test
    fun timesWithLimaOffsetAreParsedAsInstants() {
        val result = agenda(talk()).toDomain()

        assertEquals(Instant.parse("2026-11-21T14:30:00Z"), result.sessions.single().startsAt)
    }

    @Test
    fun trackColorIsConvertedToArgb() {
        val track = TrackDto("ia", "IA", "#4285F4").toDomain()

        assertEquals(0xFF4285F4, track?.color)
    }

    @Test
    fun trackWithInvalidColorUsesFixedColor() {
        val track = TrackDto("web", "Web", "purple").toDomain()

        assertEquals(0xFFA142F4, track?.color)
    }

    @Test
    fun trackWithUnknownIdIsDiscarded() {
        assertNull(TrackDto("blockchain", "Blockchain", "#000000").toDomain())
    }

    @Test
    fun eventIsMapped() {
        val event = EventDto(
            id = "devfest-lima-2026",
            name = "DevFest Lima",
            edition = "Community Edition",
            year = 2026,
            date = "2026-11-21",
            city = "Lima",
            timezone = "America/Lima",
            hashtag = "#DevFestLima26",
        ).toDomain()

        assertEquals(21, event.date.day)
        assertEquals("America/Lima", event.timeZone.id)
    }

    @Test
    fun meIsMappedWithProvider() {
        val user = MeDto("u1", "Ana", null, "github", 120, 12, 14).toDomain()

        assertEquals(AuthProvider.GITHUB, user.provider)
        assertEquals(120, user.gems)
    }

    @Test
    fun meWithUnknownProviderFails() {
        assertFailsWith<IllegalArgumentException> {
            MeDto("u1", "Ana", null, "facebook", 0, 1, 1).toDomain()
        }
    }

    @Test
    fun scanAwardedMapsExactValuesFromBackend() {
        val result = ScanResponseDto.Awarded(
            ScanAwardedDto(
                gemsAwarded = 20,
                newBalance = 140,
                source = ScanSourceDto("talk", "Agentes con la Gemini API"),
            ),
        ).toDomain()

        assertEquals(
            ScanResult.Awarded(20, 140, ScanSource(GemSourceType.TALK, "Agentes con la Gemini API")),
            result,
        )
    }

    @Test
    fun scanAlreadyUsedMapsBalance() {
        val result = ScanResponseDto.AlreadyUsed(ScanAlreadyUsedDto(balance = 120)).toDomain()

        assertEquals(ScanResult.AlreadyUsed(120, null), result)
    }

    @Test
    fun scanInvalidMapsToInvalid() {
        val result = ScanResponseDto.Invalid(
            ErrorDto(ErrorBodyDto("invalid_code", "Este QR no es del DevFest")),
        ).toDomain()

        assertEquals(ScanResult.Invalid, result)
    }

    @Test
    fun rankingKeepsBackendOrder() {
        val entries = listOf(
            RankingEntryDto(1, "b", "B", null, 100, false),
            RankingEntryDto(2, "a", "A", null, 100, false),
        )
        val ranking = RankingDto(
            updatedAt = "2026-11-21T12:00:00-05:00",
            totalParticipants = 3,
            top = entries,
            me = RankingEntryDto(3, "me", "Tú", null, 10, true),
        ).toDomain()

        assertEquals(listOf("b", "a"), ranking.top.map { it.userId })
        assertTrue(ranking.me.isMe)
    }
}
