package pe.gdg.open.devfest.app.domain.logic

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class TimeFormatTest {

    @Test
    fun hourIsShownInLimaTime() {
        assertEquals("09:30", formatHour(Instant.parse("2026-11-21T09:30:00-05:00")))
    }

    @Test
    fun hourIsLimaTimeEvenIfInstantComesFromAnotherZone() {
        // 16:30 en Madrid (UTC+1) = 10:30 en Lima.
        assertEquals("10:30", formatHour(Instant.parse("2026-11-21T16:30:00+01:00")))
    }

    @Test
    fun hourUsesTwentyFourHoursAndPadding() {
        assertEquals("17:05", formatHour(Instant.parse("2026-11-21T22:05:00Z")))
        assertEquals("08:00", formatHour(Instant.parse("2026-11-21T13:00:00Z")))
    }

    @Test
    fun durationIsInWholeMinutes() {
        val start = Instant.parse("2026-11-21T12:30:00-05:00")
        val end = Instant.parse("2026-11-21T13:45:00-05:00")

        assertEquals(75, durationMinutes(start, end))
    }

    @Test
    fun shortDateIsInSpanish() {
        assertEquals("Sáb 21 nov", formatShortDate(LocalDate(2026, 11, 21)))
        assertEquals("Lun 5 ene", formatShortDate(LocalDate(2026, 1, 5)))
    }
}
