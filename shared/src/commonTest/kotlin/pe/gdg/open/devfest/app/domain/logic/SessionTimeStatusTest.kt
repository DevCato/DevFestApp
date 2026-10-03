package pe.gdg.open.devfest.app.domain.logic

import pe.gdg.open.devfest.app.testutil.lima
import pe.gdg.open.devfest.app.testutil.talk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class SessionTimeStatusTest {

    private val session = talk("t1", "10:30", "11:15")

    @Test
    fun sessionIsPastWhenNowReachesItsEnd() {
        assertTrue(isPast(session, lima("11:15")))
        assertTrue(isPast(session, lima("16:00")))
    }

    @Test
    fun sessionInProgressOrUpcomingIsNotPast() {
        assertFalse(isPast(session, lima("10:00")))
        assertFalse(isPast(session, lima("11:00")))
    }

    @Test
    fun beforeTheEventNothingIsPast() {
        assertFalse(isPast(session, lima("20:00", date = "2026-11-20")))
    }

    @Test
    fun afterTheEventEverythingIsPast() {
        assertTrue(isPast(session, lima("08:00", date = "2026-11-22")))
    }

    @Test
    fun phoneInAnotherTimeZoneComparesTheSameInstant() {
        // 17:30 en Madrid (UTC+1) = 11:30 en Lima: la charla ya terminó.
        assertTrue(isPast(session, Instant.parse("2026-11-21T17:30:00+01:00")))
    }
}
