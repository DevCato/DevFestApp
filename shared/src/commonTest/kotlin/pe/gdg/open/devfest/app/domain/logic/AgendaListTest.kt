package pe.gdg.open.devfest.app.domain.logic

import kotlinx.datetime.LocalDate
import pe.gdg.open.devfest.app.domain.model.DefaultTracks
import pe.gdg.open.devfest.app.testutil.lima
import pe.gdg.open.devfest.app.testutil.pause
import pe.gdg.open.devfest.app.testutil.talk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AgendaListTest {

    private val eventDate = LocalDate(2026, 11, 21)
    private val sessions = listOf(
        pause("b1", "08:30", "09:30"),
        talk("t1", "09:30", "10:15", DefaultTracks.GENERAL),
        talk("t2", "10:30", "11:15", DefaultTracks.MOBILE),
        talk("t3", "10:30", "11:15", DefaultTracks.WEB),
        talk("t4", "11:30", "12:15", DefaultTracks.CLOUD),
        talk("t5", "11:30", "12:15", DefaultTracks.IA),
        pause("b2", "12:30", "13:45"),
    )

    private fun ids(rows: List<AgendaRow>) = rows.map {
        when (it) {
            is AgendaRow.TalkRow -> it.talk.id
            is AgendaRow.BreakRow -> it.pause.id
            is AgendaRow.NowRow -> "now"
        }
    }

    @Test
    fun allFilterIncludesBreaksAndNowLine() {
        val rows = buildAgendaRows(sessions, AgendaFilter.ALL, lima("11:20"), eventDate)

        assertEquals(listOf("b1", "t1", "t2", "t3", "now", "t4", "t5", "b2"), ids(rows))
    }

    @Test
    fun trackFilterShowsOnlyThatTrackWithoutBreaksOrNowLine() {
        val rows = buildAgendaRows(sessions, AgendaFilter.WEB, lima("11:20"), eventDate)

        assertEquals(listOf("t3"), ids(rows))
    }

    @Test
    fun trackWithoutTalksIsEmpty() {
        val onlyIa = listOf(talk("t5", "11:30", "12:15", DefaultTracks.IA))

        assertTrue(buildAgendaRows(onlyIa, AgendaFilter.MOBILE, lima("11:20"), eventDate).isEmpty())
    }

    @Test
    fun timeIsShownOnlyOnFirstSessionOfEachGroup() {
        val rows = buildAgendaRows(sessions, AgendaFilter.ALL, lima("07:00"), eventDate)

        val shown = rows.map {
            when (it) {
                is AgendaRow.TalkRow -> it.showTime
                is AgendaRow.BreakRow -> it.showTime
                is AgendaRow.NowRow -> null
            }
        }
        assertEquals(listOf(true, true, true, false, true, false, true), shown)
    }

    @Test
    fun pastSessionsAreMarked() {
        val rows = buildAgendaRows(sessions, AgendaFilter.ALL, lima("11:20"), eventDate)

        val t2 = rows.first { it is AgendaRow.TalkRow && it.talk.id == "t2" }
        val t4 = rows.first { it is AgendaRow.TalkRow && it.talk.id == "t4" }
        assertTrue(assertIs<AgendaRow.TalkRow>(t2).isPast)
        assertTrue(!assertIs<AgendaRow.TalkRow>(t4).isPast)
    }

    @Test
    fun nowRowCarriesTheCurrentInstant() {
        val now = lima("11:20")
        val row = buildAgendaRows(sessions, AgendaFilter.ALL, now, eventDate).filterIsInstance<AgendaRow.NowRow>().single()

        assertEquals(now, row.now)
    }
}
