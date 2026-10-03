package pe.gdg.open.devfest.app.domain.logic

import pe.gdg.open.devfest.app.domain.model.Ranking
import pe.gdg.open.devfest.app.domain.model.RankingEntry
import pe.gdg.open.devfest.app.testutil.lima
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RankingListTest {

    private fun entry(position: Int, isMe: Boolean = false) =
        RankingEntry(position, if (isMe) "me" else "u$position", "Persona $position", null, 500 - position * 10, isMe)

    private fun ranking(total: Int, mePosition: Int, topSize: Int = minOf(10, total)): Ranking {
        val top = (1..topSize).map { entry(it, isMe = it == mePosition) }
        return Ranking(lima("12:00"), total, top, entry(mePosition, isMe = true))
    }

    private fun rows(view: RankingView) = view.list.map {
        when (it) {
            is RankingListItem.Row -> it.entry.position.toString()
            RankingListItem.Gap -> "gap"
        }
    }

    @Test
    fun podiumHasFirstThreeAndListFourToTen() {
        val view = buildRankingView(ranking(total = 14, mePosition = 5))

        assertEquals(listOf(1, 2, 3), view.podium.map { it.position })
        assertEquals((4..10).map { it.toString() }, rows(view))
    }

    @Test
    fun meOutsideTopTenAddsGapAndOwnRow() {
        val view = buildRankingView(ranking(total = 14, mePosition = 12))

        assertEquals((4..10).map { it.toString() } + listOf("gap", "12"), rows(view))
        assertTrue((view.list.last() as RankingListItem.Row).entry.isMe)
    }

    @Test
    fun meExactlyEleventhHasNoGap() {
        val view = buildRankingView(ranking(total = 14, mePosition = 11))

        assertEquals((4..10).map { it.toString() } + listOf("11"), rows(view))
    }

    @Test
    fun meInsideTopTenIsHighlightedInPlace() {
        val view = buildRankingView(ranking(total = 14, mePosition = 7))

        assertEquals((4..10).map { it.toString() }, rows(view))
        val mine = view.list.filterIsInstance<RankingListItem.Row>().single { it.entry.isMe }
        assertEquals(7, mine.entry.position)
    }

    @Test
    fun meOnPodiumIsHighlightedThere() {
        val view = buildRankingView(ranking(total = 14, mePosition = 2))

        assertTrue(view.podium.single { it.position == 2 }.isMe)
    }

    @Test
    fun fewerThanThreeParticipants() {
        val view = buildRankingView(ranking(total = 2, mePosition = 2))

        assertEquals(listOf(1, 2), view.podium.map { it.position })
        assertTrue(view.list.isEmpty())
    }

    @Test
    fun backendOrderIsKept() {
        // Empate a 100 gemas: el backend ya decidió el orden.
        val top = listOf(
            RankingEntry(1, "b", "B", null, 100, false),
            RankingEntry(2, "a", "A", null, 100, false),
            RankingEntry(3, "c", "C", null, 90, false),
        )
        val view = buildRankingView(Ranking(lima("12:00"), 3, top, top[2].copy(isMe = true)))

        assertEquals(listOf("b", "a", "c"), view.podium.map { it.userId })
    }
}
