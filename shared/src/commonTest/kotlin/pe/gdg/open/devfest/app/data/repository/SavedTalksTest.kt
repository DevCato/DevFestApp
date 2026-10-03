package pe.gdg.open.devfest.app.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.gdg.open.devfest.app.data.api.FakeScenario
import pe.gdg.open.devfest.app.data.local.AgendaLocalStore
import pe.gdg.open.devfest.app.testutil.DataFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SavedTalksTest {

    private val fixture = DataFixture()
    private val repository = AgendaRepositoryImpl(fixture.api, fixture.store, fixture.sessionEvents)

    @Test
    fun toggleSavesAndRemoves() = runTest {
        repository.toggleSaved("t2")
        repository.toggleSaved("t5")
        assertEquals(setOf("t2", "t5"), repository.savedTalkIds.first())

        repository.toggleSaved("t2")
        assertEquals(setOf("t5"), repository.savedTalkIds.first())
    }

    @Test
    fun savedTalksPersistAcrossRepositoryInstances() = runTest {
        repository.toggleSaved("t2")

        val reopened = AgendaRepositoryImpl(fixture.api, AgendaLocalStore(fixture.kv), fixture.sessionEvents)

        assertEquals(setOf("t2"), reopened.savedTalkIds.first())
    }

    @Test
    fun savingWorksWithoutConnection() = runTest {
        fixture.api.scenario = FakeScenario(networkFailure = true)

        repository.toggleSaved("t2")

        assertEquals(setOf("t2"), repository.savedTalkIds.first())
    }

    @Test
    fun successfulRefreshRemovesTalksNoLongerPublished() = runTest {
        repository.toggleSaved("t2")
        repository.toggleSaved("gone")

        repository.refresh()

        assertEquals(setOf("t2"), repository.savedTalkIds.first())
    }

    @Test
    fun failedRefreshKeepsSavedTalks() = runTest {
        repository.toggleSaved("gone")
        fixture.api.scenario = FakeScenario(networkFailure = true)

        repository.refresh()

        assertEquals(setOf("gone"), repository.savedTalkIds.first())
    }

    @Test
    fun clearSavedRemovesAll() = runTest {
        repository.toggleSaved("t2")
        repository.toggleSaved("t5")

        repository.clearSaved()

        assertTrue(repository.savedTalkIds.first().isEmpty())
    }
}
