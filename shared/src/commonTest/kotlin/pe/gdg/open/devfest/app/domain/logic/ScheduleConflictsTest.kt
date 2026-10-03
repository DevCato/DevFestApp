package pe.gdg.open.devfest.app.domain.logic

import pe.gdg.open.devfest.app.testutil.talk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduleConflictsTest {

    @Test
    fun talksAtTheSameTimeConflict() {
        val talks = listOf(talk("t2", "10:30", "11:15"), talk("t3", "10:30", "11:15"))

        assertEquals(setOf("t2", "t3"), conflictingTalkIds(talks))
    }

    @Test
    fun partialOverlapConflicts() {
        val talks = listOf(talk("a", "10:00", "11:00"), talk("b", "10:45", "11:30"))

        assertEquals(setOf("a", "b"), conflictingTalkIds(talks))
    }

    @Test
    fun contiguousTalksDoNotConflict() {
        val talks = listOf(talk("a", "10:30", "11:15"), talk("b", "11:15", "12:00"))

        assertTrue(conflictingTalkIds(talks).isEmpty())
    }

    @Test
    fun onlyOverlappingTalksAreMarkedAmongThree() {
        val talks = listOf(
            talk("t1", "09:30", "10:15"),
            talk("t2", "10:30", "11:15"),
            talk("t3", "10:30", "11:15"),
        )

        assertEquals(setOf("t2", "t3"), conflictingTalkIds(talks))
    }

    @Test
    fun singleOrNoTalksHaveNoConflicts() {
        assertTrue(conflictingTalkIds(emptyList()).isEmpty())
        assertTrue(conflictingTalkIds(listOf(talk("a", "10:00", "11:00"))).isEmpty())
    }
}
