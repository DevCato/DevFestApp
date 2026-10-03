package pe.gdg.open.devfest.app.domain.logic

import pe.gdg.open.devfest.app.testutil.lima
import pe.gdg.open.devfest.app.testutil.talk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FeaturedTalkTest {

    @Test
    fun liveTalkIsInProgressRegardlessOfClock() {
        val saved = listOf(
            talk("t2", "10:30", "11:15"),
            talk("t5", "11:30", "12:15", isLive = true),
        )

        // A las 09:00 según el teléfono, pero el backend dice que t5 está en vivo.
        assertEquals(FeaturedTalk(saved[1], FeaturedKind.IN_PROGRESS), featuredTalk(saved, lima("09:00")))
    }

    @Test
    fun firstLiveTalkByStartWins() {
        val saved = listOf(
            talk("late", "14:00", "14:45", isLive = true),
            talk("early", "11:30", "12:15", isLive = true),
        ).sortedBy { it.startsAt }

        assertEquals("early", featuredTalk(saved, lima("12:00"))?.talk?.id)
    }

    @Test
    fun withoutLiveTalksTheNextOneIsFeatured() {
        val saved = listOf(
            talk("t2", "10:30", "11:15"),
            talk("t6", "14:00", "14:45"),
            talk("t8", "15:00", "15:45"),
        )

        assertEquals(FeaturedTalk(saved[1], FeaturedKind.NEXT), featuredTalk(saved, lima("11:40")))
    }

    @Test
    fun talkAlreadyStartedIsNotNext() {
        val saved = listOf(talk("t6", "14:00", "14:45"))

        assertNull(featuredTalk(saved, lima("14:10")))
    }

    @Test
    fun nothingFeaturedWithoutUpcomingTalks() {
        val saved = listOf(talk("t2", "10:30", "11:15"))

        assertNull(featuredTalk(saved, lima("18:00")))
        assertNull(featuredTalk(emptyList(), lima("09:00")))
    }
}
