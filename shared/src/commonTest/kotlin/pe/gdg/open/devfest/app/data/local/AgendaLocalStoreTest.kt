package pe.gdg.open.devfest.app.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.gdg.open.devfest.app.data.api.FakeData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AgendaLocalStoreTest {

    private val kv = InMemoryKeyValueStore()
    private val store = AgendaLocalStore(kv)

    @Test
    fun agendaIsNullWhenNeverSaved() = runTest {
        assertNull(store.agenda.first())
    }

    @Test
    fun agendaRoundTrips() = runTest {
        store.saveAgenda(FakeData.agenda())

        assertEquals(FakeData.agenda(), store.agenda.first())
    }

    @Test
    fun eventRoundTrips() = runTest {
        store.saveEvent(FakeData.event)

        assertEquals(FakeData.event, store.event.first())
    }

    @Test
    fun agendaIsStoredUnderContractKey() = runTest {
        store.saveAgenda(FakeData.agenda())

        assertTrue(kv.observeString(AgendaLocalStore.KEY_AGENDA).first()!!.contains("\"sessions\""))
    }

    @Test
    fun corruptedAgendaCountsAsNoCache() = runTest {
        kv.putString(AgendaLocalStore.KEY_AGENDA, "{not json")

        assertNull(store.agenda.first())
    }

    @Test
    fun savedTalkIdsRoundTripAndClear() = runTest {
        assertTrue(store.savedTalkIds.first().isEmpty())

        store.setSavedTalkIds(setOf("t2", "t3"))
        assertEquals(setOf("t2", "t3"), store.savedTalkIds.first())

        store.clearSavedTalkIds()
        assertTrue(store.savedTalkIds.first().isEmpty())
    }

    @Test
    fun clearingSavedTalksKeepsAgenda() = runTest {
        store.saveAgenda(FakeData.agenda())
        store.setSavedTalkIds(setOf("t2"))

        store.clearSavedTalkIds()

        assertEquals(FakeData.agenda(), store.agenda.first())
    }
}
