package pe.gdg.open.devfest.app.domain.logic

import kotlinx.datetime.LocalDate
import pe.gdg.open.devfest.app.testutil.lima
import pe.gdg.open.devfest.app.testutil.pause
import pe.gdg.open.devfest.app.testutil.talk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class NowLineTest {

    private val eventDate = LocalDate(2026, 11, 21)
    private val sessions = listOf(
        pause("b1", "08:30", "09:30"),
        talk("t1", "09:30", "10:15"),
        talk("t2", "10:30", "11:15"),
        talk("t3", "10:30", "11:15"),
        talk("t4", "11:30", "12:15"),
        pause("b3", "17:00", "17:30"),
    )

    @Test
    fun lineGoesBeforeFirstSessionThatHasNotStarted() {
        assertEquals(4, nowLineIndex(sessions, lima("11:00"), eventDate))
    }

    @Test
    fun lineAtExactStartGoesAfterThatSession() {
        // A las 10:30 las charlas de las 10:30 ya empezaron.
        assertEquals(4, nowLineIndex(sessions, lima("10:30"), eventDate))
    }

    @Test
    fun lineDuringLastSessionGoesAtTheEnd() {
        assertEquals(sessions.size, nowLineIndex(sessions, lima("17:10"), eventDate))
    }

    @Test
    fun noLineBeforeFirstSessionStarts() {
        assertNull(nowLineIndex(sessions, lima("08:00"), eventDate))
    }

    @Test
    fun noLineAfterLastSessionEnds() {
        assertNull(nowLineIndex(sessions, lima("17:31"), eventDate))
    }

    @Test
    fun noLineOnAnotherDay() {
        assertNull(nowLineIndex(sessions, lima("11:00", date = "2026-11-20"), eventDate))
    }

    @Test
    fun noLineWithoutSessions() {
        assertNull(nowLineIndex(emptyList(), lima("11:00"), eventDate))
    }

    @Test
    fun phoneInAnotherTimeZoneIsConvertedToLima() {
        // 17:00 en Madrid = 11:00 en Lima.
        assertEquals(4, nowLineIndex(sessions, Instant.parse("2026-11-21T17:00:00+01:00"), eventDate))
    }
}
