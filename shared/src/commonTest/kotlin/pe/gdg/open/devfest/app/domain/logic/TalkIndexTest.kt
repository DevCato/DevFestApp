package pe.gdg.open.devfest.app.domain.logic

import pe.gdg.open.devfest.app.testutil.pause
import pe.gdg.open.devfest.app.testutil.talk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TalkIndexTest {

    private val sessions = listOf(
        pause("b1", "08:30", "09:30"),
        talk("t1", "09:30", "10:15"),
        talk("t2", "10:30", "11:15"),
        talk("t3", "10:30", "11:15"),
        pause("b2", "12:30", "13:45"),
        talk("t6", "14:00", "14:45"),
    )

    @Test
    fun positionCountsOnlyTalksByStartTime() {
        assertEquals(TalkPosition(1, 4), talkPosition(sessions, "t1"))
        assertEquals(TalkPosition(3, 4), talkPosition(sessions, "t3"))
        assertEquals(TalkPosition(4, 4), talkPosition(sessions, "t6"))
    }

    @Test
    fun breaksAndUnknownIdsHaveNoPosition() {
        assertNull(talkPosition(sessions, "b1"))
        assertNull(talkPosition(sessions, "nope"))
    }

    @Test
    fun unsortedInputIsOrderedByStart() {
        val shuffled = listOf(sessions[5], sessions[1], sessions[2])

        assertEquals(TalkPosition(3, 3), talkPosition(shuffled, "t6"))
    }
}
